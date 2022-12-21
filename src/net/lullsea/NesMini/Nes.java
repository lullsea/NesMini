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
        debug = new Debugger(0);
        frame = new NesFrame();
        debug.setVisible(true);
    }

    public void process() throws Exception {
        // cpu.process();
        for (int i = 0; i < 256 * 240; i++)
            ppu.process();
        Thread.sleep(30);
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
        if (debug.pal != null) {
            debug.left.draw(ppu._current[0]);
            debug.right.draw(ppu._current[1]);
            debug.pal.draw(ppu.paletteTable);
        }
    }

}