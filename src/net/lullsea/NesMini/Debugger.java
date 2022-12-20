package net.lullsea.NesMini;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;

public class Debugger extends JFrame{
    Canvas left, right, pal;
    Debugger(){
        super("Debug");
        
        left = new Canvas(50, 25, 350, 350, 128, 128);
        right = new Canvas(420, 25, 350, 350, 128, 128 );

        pal = new Canvas(50, 380, 720, 100, 16, 4);

        add(left);
        add(right);
        add(pal);

        setSize(800, 560);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        // setResizable(false);
        getContentPane().setBackground(new Color(0x802525));

        setVisible(true);


    }

}

