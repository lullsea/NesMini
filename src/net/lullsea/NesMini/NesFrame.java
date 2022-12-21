package net.lullsea.NesMini;

import java.awt.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;

public class NesFrame extends JFrame {
    Canvas graphic;

    NesFrame() {
        super("Nes Mini - by Lull");

        graphic = new Canvas(0,0, 256 * 3, 240 * 3, 256, 240);

        setLocation(1920 / 3, 100);
        setSize(256 * 3, 240 * 3);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        getContentPane().setBackground(Color.black);

        
        add(graphic);
        setVisible(true);
    }
}