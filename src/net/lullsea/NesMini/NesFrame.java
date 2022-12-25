package net.lullsea.NesMini;

import java.awt.*;

import javax.swing.*;
import java.awt.event.*;

public class NesFrame extends JFrame {
    Canvas graphic;
    Nes nes;

    NesFrame(Nes nes) {
        super("Nes Mini - by Lull");
        this.nes = nes;

        /* -------------------------------- Menu Bar -------------------------------- */
        JMenuBar menuBar = new JMenuBar();
        JMenu debugMenu = new JMenu("Debug");
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.white));

        JMenuItem pal = new JMenuItem("Sprites");
        pal.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                nes._debug(0, true);
            }
        });

        JMenuItem mem = new JMenuItem("Memory");
        mem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                nes._debug(1, true);
            }
        });


        debugMenu.add(pal);
        debugMenu.add(mem);

        menuBar.add(debugMenu);

        graphic = new Canvas(0,5, 256 * 3, 240 * 3, 256, 240);

        setLocation(1920 / 3, 100);
        setSize(256 * 3 + 15, 240 * 3 + 70);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        getContentPane().setBackground(Color.black);

        
        setJMenuBar(menuBar);
        add(graphic);
        setVisible(true);
    }
}