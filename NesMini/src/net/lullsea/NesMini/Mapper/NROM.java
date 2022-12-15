package net.lullsea.NesMini.Mapper;

public class NROM extends Mapper{

    public NROM(){
    }

    @Override
    public int read(int addr) {
        addr &= 0xffff;
        if(addr <= 0x1fff)
        // 6502 2kb internal memory
            readROM(addr);
        else if(addr >= 0x2000 && addr <= 0x3fff)
        // PPU register mirrored to $3fff
            return readPpuRegister(addr);
        else if(addr >= 0x8000 && addr <= 0xffff)
        // Program ROM / Game Logic
            return nes.rom.rom[addr & (nes.rom.prgCount > 1 ? 0x7fff : 0x3fff)];
        System.out.print("ASD");
        return 0;
    }

    @Override
    public void write(int addr, int data) {
        data &= 0xff;
        if(addr <= 0x1fff)
            nes.cpu.ram[addr] = data;
        else if(addr >= 0x2000 && addr <= 0x3fff)
            writePpuRegister(addr, data);
        
    }

    @Override
    public int readPpuRegister(int addr){
        addr &= 0x7;
        int val = 0;
        switch(addr){
            // Control is unreadable
            case 0x0:
                break;
            // Mask is unreadable
            case 0x1:
                break;
            case 0x2:
                // TODO: PPU Status
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
        switch(addr){
            case 0x0:
                nes.ppu.control.set(data);
                break;
            case 0x1:
                nes.ppu.mask.set(data);
                break;
            // PPU Status is unwritable
            case 0x2:
                break;
            case 0x3:
                break;
            case 0x4:
                break;
            case 0x5:
                break;
            case 0x6:
                break;
            case 0x7:
                break;

        }
    }

    @Override
    public int reset() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int readROM(int addr) {
        return nes.cpu.ram[addr & 0x7ff];
    }

    @Override
    public int readVROM(int addr) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String toString(){
        return "NROM";
    }
}
