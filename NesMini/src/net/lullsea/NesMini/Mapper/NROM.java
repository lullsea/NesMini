package net.lullsea.NesMini.Mapper;

public class NROM extends Mapper{

    public NROM(){
    }

    @Override
    public int read(int addr) {
        addr &= 0xffff;
        if(addr >= 0x8000 && addr <= 0xffff)
            return nes.rom.rom[addr & (nes.rom.prgCount > 1 ? 0x7fff : 0x3fff)];
        if(addr <= 0x2000)
            return nes.cpu.ram[addr & 0x7ff];
        System.out.print("ASD");
        return 0;
    }

    @Override
    public void write(int addr, int data) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int reset() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int readROM(int addr) {
        // TODO Auto-generated method stub
        return 0;
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
