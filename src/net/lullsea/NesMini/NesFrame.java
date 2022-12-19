package net.lullsea.NesMini;

import java.awt.Color;

import javax.swing.JFrame;

public class NesFrame extends JFrame {

    NesFrame() {
        super("Nes Mini - by Lull");
        setSize(800, 900);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        getContentPane().setBackground(new Color(0x76583D));
        setVisible(true);
    }

    void test(int x){
        super.setBackground(new Color(x));
    }
}
