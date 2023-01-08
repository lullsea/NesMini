package net.lullsea.NesMini;

import java.io.File;

public class Main {
    int test[] = {1, 2, 3, 4};
    public static void main(String[] args) throws Exception {
        Nes nes = new Nes();
        nes.load("C:/Users/user/downloads/smb.nes");
        while (true) {
            while (!nes.ppu.complete)
                nes.process();
            nes.ppu.complete = false;
            Thread.sleep(100);
            nes.frame.graphic.draw(nes.ppu.frame);

        }
    }
}
