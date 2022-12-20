package net.lullsea.NesMini;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        Nes nes = new Nes();
        nes.load("C:/Users/user/downloads/smb.nes");
        nes.frame.left.Draw(nes.ppu.current[0]);
        nes.frame.right.Draw(nes.ppu.current[1]);
        while(true){
        nes.process();
        Thread.sleep(1000);

        }
    }

}
