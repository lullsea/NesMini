package net.lullsea.NesMini;

import java.awt.*;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.image.BufferedImage;

public class NesFrame extends JFrame {
    Canvas graphic;

    NesFrame() {
        super("Nes Mini - by Lull");

        graphic = new Canvas(15,50, 256 * 3, 240 * 3, 256, 240);

        setSize(820, 940);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        getContentPane().setBackground(new Color(0x505050));

        
        add(graphic);
        setVisible(true);
    }
}