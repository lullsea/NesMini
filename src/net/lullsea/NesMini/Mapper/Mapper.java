package net.lullsea.NesMini.Mapper;

import net.lullsea.NesMini.Nes;

public abstract class Mapper {

    public static Mapper load(int id){
        // switch(id){
        //     case 0:
        //         return new NROM();
        //     default:
        //         return null;
        // }
        return new NROM();
    }

    public Nes nes;

    // Cpu read and write
    public abstract int read(int addr);
    public abstract void write(int addr, int data);

    // PPU Registers ($2000 - $2007) read and write
    public abstract int readPpuRegister(int addr);
    public abstract void writePpuRegister(int addr, int data);
    
    // ROM read and write
    public abstract int readROM(int addr);
    public abstract int readVROM(int addr);
}
