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

    int n;

    public Nes() {
        frame = new NesFrame(this);
        n = 0;
    }

    public void process() throws Exception {
        if (n % 3 == 0)
            cpu.process();
        ppu.process();
        n++;
        // Thread.sleep(100);
        _debug(0, false);
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
    }

    public void _debug(int tool, boolean create) {
        // Debugger state machine?

        if (debug != null) {
            // Process
            debug.update();
            create = create && (tool != debug.tool);

            if(create)
            debug.dispose();
            
            if (debug != null && !debug.isDisplayable()) {
                debug = null;
                System.gc();
            }

        }
        if (create)
            this.debug = new Debugger(this, tool);
    }

}
