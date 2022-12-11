package net.lullsea.NesMini;

import net.lullsea.NesMini.Mapper.Mapper;

public class Nes {

    public Mapper mapper;
    public Cpu cpu;

    public Nes(){
        cpu = new Cpu(this);
    }

}