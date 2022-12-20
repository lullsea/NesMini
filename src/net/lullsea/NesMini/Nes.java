package net.lullsea.NesMini;

import java.io.File;

import net.lullsea.NesMini.Mapper.Mapper;

public class Nes {

    public RomLoader rom;
    public Mapper mapper;
    public Cpu cpu;
    public Ppu ppu;
    // public NesFrame frame;
    public Debugger debug;
    public NesFrame frame;

    public Nes() {
        frame = new NesFrame();
        debug = new Debugger();
    }

    public void process() {
        cpu.process();
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

        // Debugging
        debug.left.draw(ppu.current[0]);
        debug.right.draw(ppu.current[1]);
        debug.pal.draw(ppu.palette);
    }

}