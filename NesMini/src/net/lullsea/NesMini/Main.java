package net.lullsea.NesMini;


import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {

        RomLoader rom = new RomLoader(new File("C:/Users/user/Downloads/ice.nes"));

        System.out.println(rom.toString());
    }

}
