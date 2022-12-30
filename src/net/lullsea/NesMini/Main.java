package net.lullsea.NesMini;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        Nes nes = new Nes();
        nes.load("C:/Users/user/downloads/dk.nes");
        while (true) {
            while (!nes.ppu.complete)
                nes.process();
            nes.ppu.complete = false;
            Thread.sleep(60);
            nes.frame.graphic.draw(nes.ppu.frame);

        }
    }

}