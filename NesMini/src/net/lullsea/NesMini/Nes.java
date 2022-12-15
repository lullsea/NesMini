package net.lullsea.NesMini;

import java.io.File;

import net.lullsea.NesMini.Mapper.Mapper;

public class Nes {

    public RomLoader rom;
    public Mapper mapper;
    public Cpu cpu;
    public Ppu ppu;

    public Nes() {

    }

    public void update() {
        cpu.update();
    }

    public void load(String filePath) throws Exception {
        rom = new RomLoader(this, new File(filePath));
        cpu = new Cpu(this);
        ppu = new Ppu(this);

        this.mapper = rom.mapper;
        startup();
    }

    public void startup(){
        cpu.reset();
        ppu.reset();
    }

}