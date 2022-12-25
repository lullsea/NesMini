package net.lullsea.NesMini;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        Nes nes = new Nes();
        nes.load("C:/Users/user/downloads/smb.nes");
        while(true){

        for(int i = 0; i < 256 * 240; i++)
            nes.process();
        nes.frame.graphic.draw(nes.ppu.frame);

        }
    }

}