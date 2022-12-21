package net.lullsea.NesMini.Mapper;

public class NROM extends Mapper {

    public NROM() {
    }

    @Override
    public int read(int addr) {
        addr &= 0xffff;
        if (addr <= 0x1fff)
            // 6502 2kb internal memory
            return nes.cpu.ram[addr & 0x7ff];
        else if (addr >= 0x2000 && addr <= 0x3fff)
            // PPU register mirrored to $3fff
            return readPpuRegister(addr);
        else if (addr >= 0x8000 && addr <= 0xffff)
            // Program ROM / Game Logic
            return readROM(addr);
        return 0;
    }

    @Override
    public void write(int addr, int data) {
        data &= 0xff;
        if (addr <= 0x1fff)
            nes.cpu.ram[addr] = data;
        else if (addr >= 0x2000 && addr <= 0x3fff)
            writePpuRegister(addr, data);
    }

    @Override
    public int readPpuRegister(int addr) {
        addr &= 0x7;
        int val = 0;
        switch (addr) {
            // Control is unreadable
            case 0x0:
                break;
            // Mask is unreadable
            case 0x1:
                break;
            case 0x2:
                // TODO: CHECK AGAIN
                val = nes.ppu.status.get();
                // firstwrite: <- 0
                nes.ppu.firstwrite = false;
                break;
            case 0x3:
                // OAM Address is unreadable
                break;
            case 0x4:
                // TODO: OAM Data
                break;
            case 0x5:
                // Scroll is unreadable
                break;
            case 0x6:
                // PPU Address is unreadable
                break;
            case 0x7:
                // TODO: PPU Data
                break;

        }
        return val & 0xff;
    }

    @Override
    public void writePpuRegister(int addr, int data) {
        addr &= 0x7;
        data &= 0xff;
        switch (addr) {
            case 0x0:
                nes.ppu.control.set(data);
                // tram_addr: ...GH.. ........ <- data: ......GH
                nes.ppu.tramAddr.select = data & 0x3;
                break;
            case 0x1:
                nes.ppu.mask.set(data);
                break;
            // PPU Status is unwritable
            case 0x2:
                break;
            case 0x3:
                // TODO
                break;
            case 0x4:
                // TODO
                break;
            case 0x5:
                // This register is written to twice
                // TODO
                if (nes.ppu.firstwrite) {
                    // The second write contains Y offset
                    // tram_addr: ....... ...ABCDE <- data: ABCDE...
                    // fineX: FGH <- d: .....FGH
                    nes.ppu.tramAddr.fineY = data & 0x7;
                    nes.ppu.tramAddr.coarseY = data >> 3;
                } else {
                    // The first write contains X offset
                    // tram_addr : FGH..AB CDE..... <- data: ABCDEFGH
                    nes.ppu.fineX = data & 0x7;
                    nes.ppu.tramAddr.coarseX = data >> 3;

                }
                nes.ppu.firstwrite = !nes.ppu.firstwrite;
                break;
            case 0x6:
                // This register is written to twice
                if (nes.ppu.firstwrite) {
                    // The second write to this register latches the low byte to the address
                    // tram_addr: ....... ABCDEFGH <- data: ABCDEFGH
                    nes.ppu.tramAddr.set((nes.ppu.tramAddr.get() & 0xff00) | data);
                    // vram_addr: <...all bits...> <- tram_addr: <...all bits...>
                    nes.ppu.vramAddr = nes.ppu.tramAddr;
                } else {
                    // The first write to this register latches the high byte to the address
                    // tram_addr: .CDEFGH ........ <- data: ..CDEFGH
                    nes.ppu.tramAddr.set((nes.ppu.tramAddr.get() | (data << 8)) & 0x3fff);
                }
                nes.ppu.firstwrite = !nes.ppu.firstwrite;
                break;
            case 0x7:
                // All writes from this register increments the address
                // Depending on the control registers increment mode 32 : 1
                nes.ppu.write(nes.ppu.vramAddr.get(), data);
                nes.ppu.vramAddr.set(nes.ppu.vramAddr.get() + (((nes.ppu.control.get() & 0x4) > 0) ? 32 : 1));
                break;

        }
    }

    @Override
    public int readROM(int addr) {
        return nes.rom.rom[addr & (nes.rom.prgCount > 1 ? 0x7fff : 0x3fff)];
    }

    @Override
    public int readVROM(int addr) {
        return nes.rom.vrom[addr & 0x1fff];
    }

    @Override
    public String toString() {
        return "NROM";
    }
}
