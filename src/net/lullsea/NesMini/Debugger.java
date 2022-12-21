package net.lullsea.NesMini;

import java.awt.*;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Debugger extends JFrame{
    Canvas left, right, pal;
    Debugger(int tool){
        super("Debug");

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(null);
        // setResizable(false);
        getContentPane().setBackground(new Color(0x802525));
        setResizable(false);

        switch(tool){
            case 0 -> _pattern();
        }

    }

    private void _pattern(){
        setTitle("PPU viewer");

        JPanel back = new JPanel();
        JPanel bar = new JPanel();
        JLabel title = new JLabel("Pattern Table");
        JPanel bar2 = new JPanel();
        JPanel back2 = new JPanel();
        JLabel title2 = new JLabel("Palette Table");

        title.setForeground(Color.WHITE);
        title.setFont(new Font(Font.MONOSPACED, Font.PLAIN,  12));

        title2.setForeground(Color.WHITE);
        title2.setFont(new Font(Font.MONOSPACED, Font.PLAIN,  12));

        int size = 350;
        int x = 50;
        int y = 40;

        left = new Canvas(x, y, size, size, 128, 128);
        right = new Canvas(x + size + 10, y, size, size, 128, 128 );
        pal = new Canvas(50, 420, (size * 2) + 10, 100, 16, 2);


        back.setBounds(x + 15, 15, 720,  350);
        back.setBackground(new Color(0x602020));

        back2.setBounds(x + 15, 395, 720,  100);
        back2.setBackground(new Color(0x602020));

        bar.setBounds(x + 20, 15,100, 20);
        bar.setBackground(new Color(0x602020));

        bar2.setBounds(x + 20, 395,100, 20);
        bar2.setBackground(new Color(0x602020));

        add(left);
        add(right);
        add(pal);
        bar.add(title);
        bar2.add(title2);

        add(bar);
        add(back);
        add(bar2);
        add(back2);

        setSize(830, 600);
    }

}

