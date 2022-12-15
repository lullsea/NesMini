package net.lullsea.NesMini;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        Nes nes = new Nes();
        nes.load("C:/Users/user/downloads/smb.nes");
        while(true){
            nes.update();
                System.out.println(nes.cpu.toString());
                Thread.sleep(1000);

        }
    }

}
