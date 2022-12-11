package net.lullsea.NesMini;

import java.util.Arrays;

public class Cpu {

    Nes nes;
    /*
     * opcode = read(pc++) -> AddressingMode, cycles
     * Execute instruction from opcode
     * Fetch data, set address and/or relative address
     * Add to cycles and wait
     * complete
     */
    int opcode, cycles, add, fetched, address, relAddress;
    // Program counter and registers
    int pc, a, x, y, ptr;
    private int status;

    // TODO: memory stuff
    // 2KB internal ram
    private int[] ram;

    Cpu(Nes nes) {
        this.nes = nes;
        reset();
    }

    public void reset() {
	// Register default values on startup
        status = 0x34;
        a = x = y = 0;
        ptr = 0xfd;

        // Internal memory
        ram = new int[0x2000];
        Arrays.fill(ram, 0);


        cycles = 0;


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

        pc = readWord(0xfffc);
        cycles = 0;
    }

    public void update() {
        if(cycles == 0){

        }
    }

    /* --------------------------------- Cpu I/O -------------------------------- */

    public int read(int addr) {
        // $0800 - $1fff: Mirrors of $0000-$07FF
        if (addr <= 0x1fff)
            return ram[addr & 0x7ff];
        else
            return nes.mapper.read(addr);
    }

    public int readWord(int addr) {
        return read(addr) | (read(addr + 1) << 8);
    }

    public void write(int addr, int data) {
        data &= 0xff;
        if (addr <= 0x1fff)
            ram[addr & 0x7ff] = data;
        else
            nes.mapper.write(addr, data);
    }

    // Stack implemented using a 256-byte array whose location is hardcoded at page
    // $01 ($0100-$01FF), using the S register for a stack pointer.
    public void pushStack(int data) {
        write(0x100 + (ptr-- & 0xff), data);
    }

    public int popStack() {
        return read(0x100 + (++ptr & 0xff));
    }

    /* -------------------------- StatusFlag functions -------------------------- */
    public void setFlag(StatusFlag flag, boolean b) {
        status = b ? status | flag.bit : status & ~flag.bit;
    }

    public boolean getFlag(StatusFlag flag) {
        return (status & flag.bit) != 0;
    }

    public int getStatus() {
        return status & 0xff;
    }

    /* ------------------------------- Interrupts ------------------------------- */
    private void irq() {
        // If InterruptDisable flag is false
        if (!getFlag(StatusFlag.Interrupt)) {
            // Push PC to stack
            pushStack((pc >> 8) & 0xFF);
            pushStack(pc & 0xFF);

            // Set Appropriate Status Flags
            setFlag(StatusFlag.Break, false);
            setFlag(StatusFlag.Unused, true);
            setFlag(StatusFlag.Interrupt, true);

            // Push status register to stack
            pushStack(getStatus());

            // Read new PC from fixed addr
            pc = readWord(0xfffe);

            // Add some cycles to stop
            cycles = 6;
        }
    }

    private void nmi() {
        // TODO: Check if PPUCTRL bit 7 is true

        // Push PC to stack
        pushStack((pc >> 8) & 0xFF);
        pushStack(pc & 0xFF);

        // Set Appropriate Status Flags
        setFlag(StatusFlag.Break, false);
        setFlag(StatusFlag.Unused, true);
        setFlag(StatusFlag.Interrupt, true);

        // Push status register to stack
        pushStack(getStatus());

        // Read new PC from fixed addr
        pc = readWord(0xfffa);

        // Add some cycles to stop
        cycles = 6;
    }

}

enum StatusFlag {
    Carry(1 << 0),
    Zero(1 << 1),
    Interrupt(1 << 2),
    Decimal(1 << 3),
    Break(1 << 4),
    Unused(1 << 5),
    Overflow(1 << 6),
    Negative(1 << 7);

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
    INDIRECT_INDEXED,
    INDEXED_INDIRECT,
    ABSOLUTE_X,
    ABSOLUTE_Y
}
