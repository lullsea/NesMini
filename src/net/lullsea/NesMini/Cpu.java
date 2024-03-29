package net.lullsea.NesMini;

import java.io.FileWriter;

enum StatusFlag {
    CARRY(1 << 0),
    ZERO(1 << 1),
    INTERRUPT(1 << 2),
    DECIMAL(1 << 3),
    BREAK(1 << 4),
    UNUSED(1 << 5),
    OVERFLOW(1 << 6),
    NEGATIVE(1 << 7);

    final int bit;

    private StatusFlag(int bit) {
        this.bit = bit;
    }
}

enum AddressingMode {
    ZERO_PAGE,
    ABSOLUTE,
    IMPLIED,
    ACCUMULATOR,
    IMMEDIATE,
    RELATIVE,
    INDIRECT,
    ZERO_PAGE_X,
    ZERO_PAGE_Y,
    INDEXED_INDIRECT,
    INDIRECT_INDEXED,
    ABSOLUTE_X,
    ABSOLUTE_Y
}

public class Cpu {
    Nes nes;
    /*
     * opcode = read(pc++) -> AddressingMode, cycles
     * Execute instruction from opcode
     * Fetch data, set address and/or relative address
     * Add to cycles and wait
     * complete
     */

    public int[] ram; // 2KB internal ram

    public int cycles;
    private int pc, a, x, y, ptr, status; // Program counter and registers
    public int opcode;
    private AddressingMode mode;
    private int addr;

    public String _debug;

    public int requestInterrupt;

    FileWriter file;

    Cpu(Nes nes) {
        this.nes = nes;
        try{
        file = new FileWriter("test.txt");
        }catch(Exception e){}
    }

    // Reset the nes CPU (clear memory, reset vectors)
    public void reset() {
        // Define register startup values
        status = 0x34;
        a = x = y = 0;
        ptr = 0xfd;
        cycles = 0;

        // Internal memory
        ram = new int[0x2000];

        /*
         * TODO:
         * $4017 = $00 (frame irq enabled)
         * $4015 = $00 (all channels disabled)
         * $4000-$400F = $00
         * $4010-$4013 = $00 [4]
         * All 15 bits of noise channel LFSR = $0000[5]. The first time the LFSR is
         * clocked from the all-0s state, it will shift in a 1.
         * 2A03G: APU Frame Counter reset. (but 2A03letterless: APU frame counter powers
         * up at a value equivalent to 15)
         */

        // Program Counter reset vector at $fffc
        pc = readWord(0xfffc);

        cycles = 0;
        requestInterrupt = 0;
    }

    // Perform current instruction
    public void process() {
        switch (requestInterrupt) {
            case 1 -> irq();
            case 2 -> nmi();
        }
        requestInterrupt = 0;

        if (cycles == 0) {
            opcode = read(pc++);


            setFlag(StatusFlag.UNUSED, true);

            mode = (AddressingMode) lookup[opcode][0];
            cycles = (Integer) lookup[opcode][1];

            // 6th bit of the status flag always set to true
            setFlag(StatusFlag.UNUSED, true);
            parseAddressingMode(mode);

            try{
                file.write("addr: " + Integer.toHexString(addr) + " op: " + Integer.toHexString(opcode) + " C: " + getFlagBit(StatusFlag.CARRY) + "\n");
            }catch(Exception e){}

            parseInstruction(opcode);
        }
        cycles--;
    }

    // Parse the addressing mode of the current instruction
    public void parseAddressingMode(AddressingMode mode) {
        // Increment PC everytime it's read
        // Increment PC twice if it reads a word
        switch (mode) {
            case ZERO_PAGE:
                addr = read(pc++);
                break;
            case ABSOLUTE:
                addr = readWord(pc);
                pc += 2;
                break;
            case IMPLIED:
                addr = 0;
                // write(addr, a);
                break;
            case ACCUMULATOR:
                addr = a;
                break;
            case IMMEDIATE:
                addr = pc++;
                break;
            case RELATIVE:
                addr = read(pc++);
                if (addr < 0x80)
                    addr += pc;
                else
                    // Turns negative and wraps back
                    addr += pc - 256;
                break;
            case INDIRECT:
                int tmp = readWord(pc);
                pc += 2;
                // Check the low byte first
                if ((tmp & 0xff) == 0xff)
                    // addr = (read(addr + 1) << 8) | read(addr);
                    addr = (read(tmp & 0xff00) << 8) | read(tmp);
                else
                    // addr = (read(addr + 1) << 8) | read(addr);
                    addr = (read(tmp + 1) << 8) | read(tmp);
                break;
            case ZERO_PAGE_X:
                addr = read(pc++ + x);
                break;
            case ZERO_PAGE_Y:
                addr = read(pc++ + y);
                break;
            case INDEXED_INDIRECT:
                addr = readWord(read(pc++) + x);
                break;
            case INDIRECT_INDEXED:
                // addr = read(pc++);
                addr = readWord(read(pc++)) + y;
                cycles += (addr & 0xff00) != (read(pc - 1) << 8) ? 1 : 0;
                break;
            case ABSOLUTE_X:
                addr = readWord(pc);
                pc += 2;
                addr = (addr + x) & 0xffff;
                // Add additional clock cycle if overflow occured
                if ((addr & 0xff00) != (read(pc) << 8))
                    cycles += 1;
                break;
            case ABSOLUTE_Y:
                addr = readWord(pc);
                pc += 2;
                addr = (addr + y) & 0xffff;
                // Add additional clock cycle if overflow occured
                if ((addr & 0xff00) != (read(pc) << 8))
                    cycles += 1;
                break;
        }
        addr &= 0xffff;
    }

    /* --------------------------------- Cpu I/O -------------------------------- */

    // Fetch a byte from memory
    public int read(int addr) {
        int val = 0;
        // $0800 - $1fff: Mirrors of $0000-$07FF
        if (addr <= 0x1fff)
            val = ram[addr & 0x7ff];
        else
            val = nes.mapper.read(addr);
        // _createLog(addr, val, true);
        return val & 0xff;
    }

    // Fetch a word from memory
    public int readWord(int addr) {
        return read(addr) | (read(addr + 1) << 8);
    }

    // Writes a byte to memory
    public void write(int addr, int data) {
        data &= 0xff;
        if (addr <= 0x1fff)
            ram[addr & 0x7ff] = data;
        else
            nes.mapper.write(addr, data);
        // _createLog(addr, data, false);
    }

    // Stack implemented using a 256-byte array whose location is hardcoded at page
    // $01 ($0100-$01FF), using the S register for a stack pointer.
    public void pushStack(int data) {
        write(0x100 + ptr--, data);
        ptr &= 0xff;
    }

    // Fetch from the 256-byte array (stack)
    public int popStack() {
        ptr = (ptr + 1) & 0xff;
        return read(0x100 | ptr);
    }

    // Int to boolean
    private boolean ib(int a) {
        return (a != 0);
    }

    // Boolean to int
    private int bi(boolean a) {
        return a ? 1 : 0;
    }

    /* -------------------------- StatusFlag functions -------------------------- */

    // Clear or set a flag (bit) on the status regsiter
    private void setFlag(StatusFlag flag, boolean b) {
        status = b ? status | flag.bit : status & ~flag.bit;
    }

    // Fetch the value of a flag (bit) on the status regsiter
    private boolean getFlag(StatusFlag flag) {
        return (status & flag.bit) != 0;
    }

    // Same as getFlag() but returns a 1 or 0 instead of a boolean
    private int getFlagBit(StatusFlag flag) {
        return bi(getFlag(flag));
    }

    // Fetch the status register
    public int getStatus() {
        return status & 0xff;
    }

    /* ------------------------------- Interrupts ------------------------------- */

    // Cpu interrupt
    private void irq() {
        // If InterruptDisable flag is false
        if (!getFlag(StatusFlag.INTERRUPT)) {
            // Push PC to stack
            pushStack((pc >> 8) & 0xFF);
            pushStack(pc & 0xFF);

            // Set Appropriate Status Flags
            setFlag(StatusFlag.BREAK, false);
            setFlag(StatusFlag.UNUSED, true);
            setFlag(StatusFlag.INTERRUPT, true);

            // Push status register to stack
            pushStack(getStatus());

            // Read new PC from fixed addr
            pc = readWord(0xfffe);

            // Add some cycles to stop
            cycles = 6;
        }
    }

    // Non-maskable interrupt
    private void nmi() {
        // Push PC to stack
        pushStack((pc >> 8) & 0xFF);
        pushStack(pc & 0xFF);

        // Set Appropriate Status Flags
        setFlag(StatusFlag.BREAK, false);
        setFlag(StatusFlag.UNUSED, true);
        setFlag(StatusFlag.INTERRUPT, true);

        // Push status register to stack
        pushStack(getStatus());

        // Read new PC from fixed addr
        pc = readWord(0xfffa);
        // Add some cycles to stop
        cycles = 6;
    }

    /* ------------------ Addressing Modes, Cycles and opcodes ------------------ */

    // Lookup the current opcodes Addressing Mode and Cycles required
    final Object[][] lookup = {
            { AddressingMode.IMMEDIATE, 7 }, { AddressingMode.INDEXED_INDIRECT, 6 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMPLIED, 8 }, { AddressingMode.IMPLIED, 3 }, { AddressingMode.ZERO_PAGE, 3 },
            { AddressingMode.ZERO_PAGE, 5 }, { AddressingMode.IMPLIED, 5 }, { AddressingMode.IMPLIED, 3 },
            { AddressingMode.IMMEDIATE, 2 }, { AddressingMode.IMPLIED, 2 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMPLIED, 4 }, { AddressingMode.ABSOLUTE, 4 }, { AddressingMode.ABSOLUTE, 6 },
            { AddressingMode.IMPLIED, 6 },
            { AddressingMode.RELATIVE, 2 }, { AddressingMode.INDIRECT_INDEXED, 5 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMPLIED, 8 }, { AddressingMode.IMPLIED, 4 }, { AddressingMode.ZERO_PAGE_X, 4 },
            { AddressingMode.ZERO_PAGE_X, 6 }, { AddressingMode.IMPLIED, 6 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.ABSOLUTE_Y, 4 }, { AddressingMode.IMPLIED, 2 }, { AddressingMode.IMPLIED, 7 },
            { AddressingMode.IMPLIED, 4 }, { AddressingMode.ABSOLUTE_X, 4 }, { AddressingMode.ABSOLUTE_X, 7 },
            { AddressingMode.IMPLIED, 7 },
            { AddressingMode.ABSOLUTE, 6 }, { AddressingMode.INDEXED_INDIRECT, 6 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMPLIED, 8 }, { AddressingMode.ZERO_PAGE, 3 }, { AddressingMode.ZERO_PAGE, 3 },
            { AddressingMode.ZERO_PAGE, 5 }, { AddressingMode.IMPLIED, 5 }, { AddressingMode.IMPLIED, 4 },
            { AddressingMode.IMMEDIATE, 2 }, { AddressingMode.IMPLIED, 2 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.ABSOLUTE, 4 }, { AddressingMode.ABSOLUTE, 4 }, { AddressingMode.ABSOLUTE, 6 },
            { AddressingMode.IMPLIED, 6 },
            { AddressingMode.RELATIVE, 2 }, { AddressingMode.INDIRECT_INDEXED, 5 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMPLIED, 8 }, { AddressingMode.IMPLIED, 4 }, { AddressingMode.ZERO_PAGE_X, 4 },
            { AddressingMode.ZERO_PAGE_X, 6 }, { AddressingMode.IMPLIED, 6 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.ABSOLUTE_Y, 4 }, { AddressingMode.IMPLIED, 2 }, { AddressingMode.IMPLIED, 7 },
            { AddressingMode.IMPLIED, 4 }, { AddressingMode.ABSOLUTE_X, 4 }, { AddressingMode.ABSOLUTE_X, 7 },
            { AddressingMode.IMPLIED, 7 },
            { AddressingMode.IMPLIED, 6 }, { AddressingMode.INDEXED_INDIRECT, 6 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMPLIED, 8 }, { AddressingMode.IMPLIED, 3 }, { AddressingMode.ZERO_PAGE, 3 },
            { AddressingMode.ZERO_PAGE, 5 }, { AddressingMode.IMPLIED, 5 }, { AddressingMode.IMPLIED, 3 },
            { AddressingMode.IMMEDIATE, 2 }, { AddressingMode.IMPLIED, 2 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.ABSOLUTE, 3 }, { AddressingMode.ABSOLUTE, 4 }, { AddressingMode.ABSOLUTE, 6 },
            { AddressingMode.IMPLIED, 6 },
            { AddressingMode.RELATIVE, 2 }, { AddressingMode.INDIRECT_INDEXED, 5 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMPLIED, 8 }, { AddressingMode.IMPLIED, 4 }, { AddressingMode.ZERO_PAGE_X, 4 },
            { AddressingMode.ZERO_PAGE_X, 6 }, { AddressingMode.IMPLIED, 6 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.ABSOLUTE_Y, 4 }, { AddressingMode.IMPLIED, 2 }, { AddressingMode.IMPLIED, 7 },
            { AddressingMode.IMPLIED, 4 }, { AddressingMode.ABSOLUTE_X, 4 }, { AddressingMode.ABSOLUTE_X, 7 },
            { AddressingMode.IMPLIED, 7 },
            { AddressingMode.IMPLIED, 6 }, { AddressingMode.INDEXED_INDIRECT, 6 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMPLIED, 8 }, { AddressingMode.IMPLIED, 3 }, { AddressingMode.ZERO_PAGE, 3 },
            { AddressingMode.ZERO_PAGE, 5 }, { AddressingMode.IMPLIED, 5 }, { AddressingMode.IMPLIED, 4 },
            { AddressingMode.IMMEDIATE, 2 }, { AddressingMode.IMPLIED, 2 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.INDIRECT, 5 }, { AddressingMode.ABSOLUTE, 4 }, { AddressingMode.ABSOLUTE, 6 },
            { AddressingMode.IMPLIED, 6 },
            { AddressingMode.RELATIVE, 2 }, { AddressingMode.INDIRECT_INDEXED, 5 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMPLIED, 8 }, { AddressingMode.IMPLIED, 4 }, { AddressingMode.ZERO_PAGE_X, 4 },
            { AddressingMode.ZERO_PAGE_X, 6 }, { AddressingMode.IMPLIED, 6 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.ABSOLUTE_Y, 4 }, { AddressingMode.IMPLIED, 2 }, { AddressingMode.IMPLIED, 7 },
            { AddressingMode.IMPLIED, 4 }, { AddressingMode.ABSOLUTE_X, 4 }, { AddressingMode.ABSOLUTE_X, 7 },
            { AddressingMode.IMPLIED, 7 },
            { AddressingMode.IMPLIED, 2 }, { AddressingMode.INDEXED_INDIRECT, 6 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMPLIED, 6 }, { AddressingMode.ZERO_PAGE, 3 }, { AddressingMode.ZERO_PAGE, 3 },
            { AddressingMode.ZERO_PAGE, 3 }, { AddressingMode.IMPLIED, 3 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMPLIED, 2 }, { AddressingMode.IMPLIED, 2 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.ABSOLUTE, 4 }, { AddressingMode.ABSOLUTE, 4 }, { AddressingMode.ABSOLUTE, 4 },
            { AddressingMode.IMPLIED, 4 },
            { AddressingMode.RELATIVE, 2 }, { AddressingMode.INDIRECT_INDEXED, 6 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMPLIED, 6 }, { AddressingMode.ZERO_PAGE_X, 4 }, { AddressingMode.ZERO_PAGE_X, 4 },
            { AddressingMode.ZERO_PAGE_Y, 4 }, { AddressingMode.IMPLIED, 4 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.ABSOLUTE_Y, 5 }, { AddressingMode.IMPLIED, 2 }, { AddressingMode.IMPLIED, 5 },
            { AddressingMode.IMPLIED, 5 }, { AddressingMode.ABSOLUTE_X, 5 }, { AddressingMode.IMPLIED, 5 },
            { AddressingMode.IMPLIED, 5 },
            { AddressingMode.IMMEDIATE, 2 }, { AddressingMode.INDEXED_INDIRECT, 6 }, { AddressingMode.IMMEDIATE, 2 },
            { AddressingMode.IMPLIED, 6 }, { AddressingMode.ZERO_PAGE, 3 }, { AddressingMode.ZERO_PAGE, 3 },
            { AddressingMode.ZERO_PAGE, 3 }, { AddressingMode.IMPLIED, 3 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMMEDIATE, 2 }, { AddressingMode.IMPLIED, 2 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.ABSOLUTE, 4 }, { AddressingMode.ABSOLUTE, 4 }, { AddressingMode.ABSOLUTE, 4 },
            { AddressingMode.IMPLIED, 4 },
            { AddressingMode.RELATIVE, 2 }, { AddressingMode.INDIRECT_INDEXED, 5 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMPLIED, 5 }, { AddressingMode.ZERO_PAGE_X, 4 }, { AddressingMode.ZERO_PAGE_X, 4 },
            { AddressingMode.ZERO_PAGE_Y, 4 }, { AddressingMode.IMPLIED, 4 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.ABSOLUTE_Y, 4 }, { AddressingMode.IMPLIED, 2 }, { AddressingMode.IMPLIED, 4 },
            { AddressingMode.ABSOLUTE_X, 4 }, { AddressingMode.ABSOLUTE_X, 4 }, { AddressingMode.ABSOLUTE_Y, 4 },
            { AddressingMode.IMPLIED, 4 },
            { AddressingMode.IMMEDIATE, 2 }, { AddressingMode.INDEXED_INDIRECT, 6 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMPLIED, 8 }, { AddressingMode.ZERO_PAGE, 3 }, { AddressingMode.ZERO_PAGE, 3 },
            { AddressingMode.ZERO_PAGE, 5 }, { AddressingMode.IMPLIED, 5 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMMEDIATE, 2 }, { AddressingMode.IMPLIED, 2 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.ABSOLUTE, 4 }, { AddressingMode.ABSOLUTE, 4 }, { AddressingMode.ABSOLUTE, 6 },
            { AddressingMode.IMPLIED, 6 },
            { AddressingMode.RELATIVE, 2 }, { AddressingMode.INDIRECT_INDEXED, 5 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMPLIED, 8 }, { AddressingMode.IMPLIED, 4 }, { AddressingMode.ZERO_PAGE_X, 4 },
            { AddressingMode.ZERO_PAGE_X, 6 }, { AddressingMode.IMPLIED, 6 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.ABSOLUTE_Y, 4 }, { AddressingMode.IMPLIED, 2 }, { AddressingMode.IMPLIED, 7 },
            { AddressingMode.IMPLIED, 4 }, { AddressingMode.ABSOLUTE_X, 4 }, { AddressingMode.ABSOLUTE_X, 7 },
            { AddressingMode.IMPLIED, 7 },
            { AddressingMode.IMMEDIATE, 2 }, { AddressingMode.INDEXED_INDIRECT, 6 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMPLIED, 8 }, { AddressingMode.ZERO_PAGE, 3 }, { AddressingMode.ZERO_PAGE, 3 },
            { AddressingMode.ZERO_PAGE, 5 }, { AddressingMode.IMPLIED, 5 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMMEDIATE, 2 }, { AddressingMode.IMPLIED, 2 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.ABSOLUTE, 4 }, { AddressingMode.ABSOLUTE, 4 }, { AddressingMode.ABSOLUTE, 6 },
            { AddressingMode.IMPLIED, 6 },
            { AddressingMode.RELATIVE, 2 }, { AddressingMode.INDIRECT_INDEXED, 5 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.IMPLIED, 8 }, { AddressingMode.IMPLIED, 4 }, { AddressingMode.ZERO_PAGE_X, 4 },
            { AddressingMode.ZERO_PAGE_X, 6 }, { AddressingMode.IMPLIED, 6 }, { AddressingMode.IMPLIED, 2 },
            { AddressingMode.ABSOLUTE_Y, 4 }, { AddressingMode.IMPLIED, 2 }, { AddressingMode.IMPLIED, 7 },
            { AddressingMode.IMPLIED, 4 }, { AddressingMode.ABSOLUTE_X, 4 }, { AddressingMode.ABSOLUTE_X, 7 },
            { AddressingMode.IMPLIED, 7 }
    }; // Typing this out gave me carpal tunnel

    // Parse the current opcode and execute the instruction
    private void parseInstruction(int op) {
        switch (op) {
            case 0x61, 0x65, 0x69, 0x6d, 0x75, 0x79, 0x7d, 0x71 -> adc();
            case 0x21, 0x25, 0x29, 0x2d, 0x31, 0x35, 0x39, 0x3d -> and();
            case 0x06, 0x0a, 0x0e, 0x16, 0x1e -> asl();
            case 0x90 -> bcc();
            case 0xb0 -> bcs();
            case 0xf0 -> beq();
            case 0x24, 0x2c -> bit();
            case 0x30 -> bmi();
            case 0xd0 -> bne();
            case 0x10 -> bpl();
            case 0x00 -> brk();
            case 0x50 -> bvc();
            case 0x70 -> bvs();
            case 0x18 -> clc();
            case 0xd8 -> cld();
            case 0x58 -> cli();
            case 0xb8 -> clv();
            case 0xc1, 0xc5, 0xc9, 0xcd, 0xd1, 0xd5, 0xd9, 0xdd -> cmp();
            case 0xe0, 0xe4, 0xec -> cpx();
            case 0xc0, 0xc4, 0xcc -> cpy();
            case 0xc6, 0xce, 0xd6, 0xde -> dec();
            case 0xca -> dex();
            case 0x88 -> dey();
            case 0x41, 0x45, 0x49, 0x4d, 0x51, 0x55, 0x59, 0x5d -> eor();
            case 0xe6, 0xee, 0xf6, 0xfe -> inc();
            case 0xe8 -> inx();
            case 0xc8 -> iny();
            case 0x4c, 0x6c -> jmp();
            case 0x20 -> jsr();
            case 0xa1, 0xa5, 0xa9, 0xad, 0xb1, 0xb5, 0xb9, 0xbd -> lda();
            case 0xa2, 0xa6, 0xae, 0xb6, 0xbe -> ldx();
            case 0xa0, 0xa4, 0xac, 0xb4, 0xbc -> ldy();
            case 0x46, 0x4a, 0x4e, 0x56, 0x5e -> lsr();
            case 0x01, 0x05, 0x09, 0x0d, 0x11, 0x15, 0x19, 0x1d -> ora();
            case 0x48 -> pha();
            case 0x08 -> php();
            case 0x68 -> pla();
            case 0x28 -> plp();
            case 0x26, 0x2a, 0x2e, 0x36, 0x3e -> rol();
            case 0x66, 0x6a, 0x6e, 0x7e -> ror();
            case 0x40 -> rti();
            case 0x60 -> rts();
            case 0xe1, 0xe5, 0xe9, 0xed, 0xf1, 0xf5, 0xf9, 0xfd -> sbc();
            case 0x38 -> sec();
            case 0xf8 -> sed();
            case 0x78 -> sei();
            case 0x81, 0x85, 0x8d, 0x91, 0x95, 0x9d, 0x99 -> sta();
            case 0x86, 0x8e, 0x96 -> stx();
            case 0x84, 0x8c, 0x94 -> sty();
            case 0xaa -> tax();
            case 0xa8 -> tay();
            case 0xba -> tsx();
            case 0x8a -> txa();
            case 0x9a -> txs();
            case 0x98 -> tya();
            default -> nop();
        }
    }

    /* ------------------------------ Instructions ------------------------------ */

    private void adc() {

        int j = (a + read(addr) + getFlagBit(StatusFlag.CARRY));

        setFlag(StatusFlag.CARRY, j > 0xff);

        setFlag(StatusFlag.ZERO, (j & 0xff) == 0);

        // setFlag(StatusFlag.OVERFLOW,
        // ((a ^ read(addr)) & 0x80) == 0 &&
        // ((a ^ j) & 0x80) != 0);

        setFlag(StatusFlag.OVERFLOW, ib(~(a ^ read(addr)) & (a ^ j) & 0x80));

        setFlag(StatusFlag.NEGATIVE, ib(j & 0x80));
        a = j & 0xff;
        // System.out.println("A: " + a + " X: " + x + " D: " +
        // Integer.toHexString(read(addr)) + " C: " + getFlagBit(StatusFlag.CARRY));
    }

    private void and() {
        a &= read(addr);
        setFlag(StatusFlag.ZERO, a == 0);
        setFlag(StatusFlag.NEGATIVE, ib(a & 0x80));
    }

    private void asl() {
        int j;
        if (mode == AddressingMode.IMPLIED) {
            j = a << 1;
            setFlag(StatusFlag.CARRY, ib(a & 0x80));
            a = j & 0xff;
        } else {
            j = read(addr);
            setFlag(StatusFlag.CARRY, ib(j & 0x80));
            j <<= 1;
            write(addr, j);
        }
        setFlag(StatusFlag.ZERO, (j & 0xff) == 0);
        setFlag(StatusFlag.NEGATIVE, ib(j & 0x80));
    }

    private void branch() {
        if ((pc & 0xff00) != (addr & 0xff00))
            cycles++;
        pc = addr;
    }

    private void bcc() {
        if (!getFlag(StatusFlag.CARRY))
            branch();
    }

    private void bcs() {
        if (getFlag(StatusFlag.CARRY))
            branch();
    }

    private void beq() {

        if (getFlag(StatusFlag.ZERO))
            branch();
    }

    private void bit() {
        int j = read(addr);
        setFlag(StatusFlag.NEGATIVE, ib(addr & 0x80));
        setFlag(StatusFlag.OVERFLOW, !ib(addr & 0x40));
        j &= a;
        setFlag(StatusFlag.ZERO, j == 0);
    }

    private void bmi() {
        if (getFlag(StatusFlag.NEGATIVE))
            branch();
    }

    private void bne() {
        if (!getFlag(StatusFlag.ZERO))
            branch();
    }

    private void bpl() {
        if (!getFlag(StatusFlag.NEGATIVE))
            branch();
    }

    private void brk() {
        pc++;
        setFlag(StatusFlag.INTERRUPT, true);
        pushStack((pc >> 8) & 0xff);
        pushStack(pc & 0xff);

        pushStack(getStatus() | 0x10);
        setFlag(StatusFlag.BREAK, false);

        // reset vector
        pc = readWord(0xfffe);
    }

    private void bvc() {
        if (!getFlag(StatusFlag.OVERFLOW))
            branch();
    }

    private void bvs() {
        if (getFlag(StatusFlag.OVERFLOW))
            branch();
    }

    private void clc() {
        setFlag(StatusFlag.CARRY, false);
    }

    private void cld() {
        setFlag(StatusFlag.DECIMAL, false);
    }

    private void cli() {
        setFlag(StatusFlag.INTERRUPT, false);
    }

    private void clv() {
        setFlag(StatusFlag.OVERFLOW, false);
    }

    private void cmp() {

        int j = a - read(addr);

        setFlag(StatusFlag.CARRY, j >= 0);
        setFlag(StatusFlag.ZERO, (j & 0xff) == 0);
        setFlag(StatusFlag.NEGATIVE, ib(j & 0x80));
    }

    private void cpx() {
        int j = x - read(addr);

        setFlag(StatusFlag.CARRY, j >= 0);
        setFlag(StatusFlag.ZERO, (j & 0xff) == 0);
        setFlag(StatusFlag.NEGATIVE, ib(j & 0x80));
    }

    private void cpy() {
        int j = y - read(addr);

        setFlag(StatusFlag.CARRY, j >= 0);
        setFlag(StatusFlag.ZERO, (j & 0xff) == 0);
        setFlag(StatusFlag.NEGATIVE, ib(j & 0x80));
    }

    private void dec() {
        int j = (read(addr) - 1) & 0xff;
        write(addr, j);
        setFlag(StatusFlag.ZERO, j == 0);
        setFlag(StatusFlag.NEGATIVE, ib(j & 0x80));
    }

    private void dex() {
        x = (x - 1) & 0xff;
        setFlag(StatusFlag.ZERO, x == 0);
        setFlag(StatusFlag.NEGATIVE, ib(x & 0x80));
    }

    private void dey() {
        y = (y - 1) & 0xff;
        setFlag(StatusFlag.ZERO, y == 0);
        setFlag(StatusFlag.NEGATIVE, ib(y & 0x80));
    }

    private void eor() {
        a = (read(addr) ^ a) & 0xff;
        setFlag(StatusFlag.ZERO, a == 0);
        setFlag(StatusFlag.NEGATIVE, ib(a & 0x80));
    }

    private void inc() {
        int j = (read(addr) + 1) & 0xff;
        write(addr, j);
        setFlag(StatusFlag.ZERO, j == 0);
        setFlag(StatusFlag.NEGATIVE, ib(j & 0x80));
    }

    private void inx() {
        x = (x + 1) & 0xff;
        setFlag(StatusFlag.ZERO, x == 0);
        setFlag(StatusFlag.NEGATIVE, ib(x & 0x80));
    }

    private void iny() {
        y = (y + 1) & 0xff;
        setFlag(StatusFlag.ZERO, y == 0);
        setFlag(StatusFlag.NEGATIVE, ib(y & 0x80));
    }

    private void jmp() {
        pc = addr;
    }

    private void jsr() {
        pc--;
        pushStack((pc >> 8) & 0xff);
        pushStack(pc & 0xff);
        pc = addr;

    }

    private void lda() {
        a = read(addr);

        setFlag(StatusFlag.ZERO, a == 0);
        setFlag(StatusFlag.NEGATIVE, ib(a & 0x80));
    }

    private void ldx() {
        x = read(addr);
        setFlag(StatusFlag.ZERO, x == 0);
        setFlag(StatusFlag.NEGATIVE, ib(x & 0x80));
    }

    private void ldy() {
        y = read(addr);
        setFlag(StatusFlag.ZERO, y == 0);
        setFlag(StatusFlag.NEGATIVE, ib(y & 0x80));
    }

    private void lsr() {
        int j;

        if (mode == AddressingMode.IMPLIED) {
            j = a >> 1;
            setFlag(StatusFlag.CARRY, ib(a & 0x1));
            a = j & 0xff;
        } else {
            j = read(addr);
            setFlag(StatusFlag.CARRY, ib(j & 0x1));
            j >>= 1;
            write(addr, j);
        }

        setFlag(StatusFlag.ZERO, j == 0);
        setFlag(StatusFlag.NEGATIVE, false);
    }

    private void ora() {
        a = (a | read(addr)) & 0xff;
        setFlag(StatusFlag.ZERO, a == 0);
        setFlag(StatusFlag.NEGATIVE, ib(a & 0x80));
    }

    private void pha() {
        pushStack(a);
    }

    private void php() {
        setFlag(StatusFlag.BREAK, true);
        setFlag(StatusFlag.UNUSED, true);
        pushStack(getStatus());
    }

    private void pla() {
        a = popStack();
        setFlag(StatusFlag.ZERO, a == 0);
        setFlag(StatusFlag.NEGATIVE, ib(a & 0x80));
    }

    private void plp() {
        status = popStack();
        setFlag(StatusFlag.UNUSED, true);
    }

    private void rol() {
        int j;
        if (mode == AddressingMode.IMPLIED) {
            j = a;
            j = (a << 1) + getFlagBit(StatusFlag.CARRY);
            setFlag(StatusFlag.CARRY, ib(a & 0x80));
            a = j & 0xff;
        } else {
            j = read(addr);
            boolean add = getFlag(StatusFlag.CARRY);
            setFlag(StatusFlag.CARRY, ib(j & 0x80));
            j = ((j << 1) & 0xff) | bi(add);
            write(addr, j);
        }
        setFlag(StatusFlag.ZERO, (j & 0xff) == 0);
        setFlag(StatusFlag.NEGATIVE, ib(j & 0x80));
    }

    private void ror() {
        int j;

        if (mode == AddressingMode.IMPLIED) {
            j = a;
            j = (a >> 1) + (getFlagBit(StatusFlag.CARRY) << 7);
            setFlag(StatusFlag.CARRY, ib(a & 0x1));
            a = j & 0xff;
        } else {
            j = read(addr);
            boolean add = getFlag(StatusFlag.CARRY);
            setFlag(StatusFlag.CARRY, ib(j & 0x1));
            j = (j >> 1) | (bi(add) << 7);
            write(addr, j);
        }

        setFlag(StatusFlag.ZERO, (j & 0xff) == 0);
        setFlag(StatusFlag.NEGATIVE, ib(j & 0x80));

    }

    private void rti() {
        status = popStack();
        pc = popStack();
        pc |= popStack() << 8;

        setFlag(StatusFlag.BREAK, true);
        setFlag(StatusFlag.UNUSED, true);

        pc--;
    }

    private void rts() {
        pc = popStack();
        pc |= popStack() << 8;

        pc++;
    }

    private void sbc() {
        // Invert low byte
        int j = a - read(addr) - (1 - getFlagBit(StatusFlag.CARRY));

        setFlag(StatusFlag.OVERFLOW, ib((j ^ a) & (j ^ j) & 0x80));
        setFlag(StatusFlag.NEGATIVE, ib(j & 0x80));
        setFlag(StatusFlag.CARRY, j >= 0);
        setFlag(StatusFlag.ZERO, (j & 0xff) == 0);
        a = j & 0xff;
    }

    private void sec() {
        setFlag(StatusFlag.CARRY, true);
    }

    private void sed() {
        setFlag(StatusFlag.DECIMAL, true);
    }

    private void sei() {
        setFlag(StatusFlag.INTERRUPT, true);
    }

    private void sta() {
        write(addr, a);
    }

    private void stx() {
        write(addr, x);
    }

    private void sty() {
        write(addr, y);
    }

    private void tax() {
        x = a;
        setFlag(StatusFlag.ZERO, x == 0);
        setFlag(StatusFlag.NEGATIVE, ib(x & 0x80));
    }

    private void tay() {
        y = a;
        setFlag(StatusFlag.ZERO, y == 0);
        setFlag(StatusFlag.NEGATIVE, ib(y & 0x80));
    }

    private void tsx() {
        x = (ptr - 0x100) & 0xff;
        setFlag(StatusFlag.ZERO, x == 0);
        setFlag(StatusFlag.NEGATIVE, ib(x & 0x80));
    }

    private void txa() {
        a = x;
        setFlag(StatusFlag.ZERO, a == 0);
        setFlag(StatusFlag.NEGATIVE, ib(a & 0x80));
    }

    private void txs() {
        ptr = (x + 0x100) & 0xff;
    }

    private void tya() {
        a = y;
        setFlag(StatusFlag.ZERO, a == 0);
        setFlag(StatusFlag.NEGATIVE, ib(a & 0x80));
    }

    private void nop() {
        cycles += 1;
        return;
    }

    @Override
    public String toString() {
        return "-CPU: " + '\n' +
                "A:" + (a) + " " + "X:" + (x) + " " + "Y:" + (y) + "\n" +
                "P:" + Integer.toBinaryString(status) + " " + "PC:$" + Integer.toHexString(pc) + "\n" +
                "S:$" + Integer.toHexString(ptr) + "\n" +
                "-INSTRUCTION: " + '\n' +
                "Op:$" + Integer.toHexString(opcode) + "\n" +
                "Mode:" + lookup[opcode][0] + "\n";
    }

    // Debugging
    private void _createLog(int addr, int data, Boolean isRead) {
        // Only if the memory viewer is active
        if (nes.debug != null && nes.debug.tool == 1) {
            String _addr = Integer.toHexString(addr);
            String _val = Integer.toHexString(data);
            String symbol = isRead ? "-)" : "(-";
            String color = isRead ? "#51c94b" : "red";
            if (_addr.length() < 4)
                _addr = "0".repeat(4 - _addr.length()) + _addr;
            if (_val.length() < 2)
                _val = "0" + _val;
            _debug = "<font color=" + color + ">$" + _addr + " " + symbol + " " + "0x" + _val + "</font>\n";
        }
    }
}
