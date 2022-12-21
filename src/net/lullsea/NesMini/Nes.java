package net.lullsea.NesMini;

import java.io.File;

import net.lullsea.NesMini.Mapper.Mapper;

public class Nes {

    public RomLoader rom;
    public Mapper mapper;
    public Cpu cpu;
    public Ppu ppu;

    public Debugger debug;
    public NesFrame frame;

    public Nes() {
        frame = new NesFrame();
    }

    public void process() throws Exception {
        cpu.process();
        for (int i = 0; i < 256 * 240; i++)
            ppu.process();
        Thread.sleep(30);
        debug.update();
        frame.graphic.draw(ppu.frame);
    }

    public void load(String filePath) throws Exception {
        rom = new RomLoader(this, new File(filePath));
        cpu = new Cpu(this);
        ppu = new Ppu(this);

        this.mapper = rom.mapper;
        startup();
    }

    public void startup() {
        cpu.reset();
        ppu.reset();

        // Debugging
        debug = new Debugger(this, 1);
    }

}