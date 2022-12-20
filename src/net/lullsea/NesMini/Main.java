package net.lullsea.NesMini;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        Nes nes = new Nes();
        nes.load("C:/Users/user/downloads/dk.nes");
        while(true){
        nes.process();
        Thread.sleep(1000);

        }
    }

}
