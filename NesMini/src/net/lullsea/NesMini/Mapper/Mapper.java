package net.lullsea.NesMini.Mapper;

import net.lullsea.NesMini.Nes;

public abstract class Mapper {

    public static Mapper getMapper(int id){
        switch(id){
            case 0:
                return new NROM();
            default:
                return null;
        }
    }

    public Nes nes;

    public abstract int read(int addr);
    public abstract void write(int addr, int data);
    public abstract int reset();

    public abstract int readROM(int addr);
    public abstract int readVROM(int addr);

}
