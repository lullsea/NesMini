package net.lullsea.NesMini;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        Nes nes = new Nes();
        nes.load("C:/Users/user/downloads/smb.nes");
        while(true){
            nes.process();
                System.out.println(nes.cpu.toString());
                nes.frame.test(0x101010);
                Thread.sleep(1000);

        }
    }

}
