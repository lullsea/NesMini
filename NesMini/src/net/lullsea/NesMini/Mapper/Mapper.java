package net.lullsea.NesMini.Mapper;

public abstract class Mapper {

    public static Mapper getMapper(int id) throws Exception {
        return new NROM();
    }

    public abstract int read(int addr);
    public abstract void write(int addr, int data);
    public abstract int reset();

    public abstract int readROM(int addr);
    public abstract int readVROM(int addr);

}
