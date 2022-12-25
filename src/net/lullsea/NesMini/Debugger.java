package net.lullsea.NesMini;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Debugger extends JFrame {
    // Pattern
    Canvas left, right, pal;
    Nes nes;
    // Memory
    StringBuilder cpuMap, ppuMap;
    JLabel cpuLabel, ppuLabel;
    int offset = 0, offset2 = 0;

    StringBuilder console, console2;
    JLabel consoleLabel, consoleLabel2;
    boolean isSkip = false, isSkip2 = false;

    int tool;
    private int counter;

    private Font f = new Font(Font.MONOSPACED, Font.BOLD, 12);

    Debugger(Nes nes, int tool) {
        super("Debug");
        this.nes = nes;
        this.tool = tool;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(null);
        setResizable(false);

        switch (tool) {
            case 0 -> _pattern();
            case 1 -> _memory();
        }
        setVisible(true);
    }

    private final int x = 50, y = 40;

    private void _pattern() {
        int size = 350;
        Color shadow = new Color(0x501010);
        setTitle("PPU Viewer");

        getContentPane().setBackground(new Color(0x802525));

        JPanel back = new JPanel();
        JPanel bar = new JPanel();
        JLabel title = new JLabel("Spritesheet");
        JPanel bar2 = new JPanel();
        JPanel back2 = new JPanel();
        JLabel title2 = new JLabel("Palette Table");

        title.setForeground(Color.WHITE);
        title.setFont(f);

        title2.setForeground(Color.WHITE);
        title2.setFont(f);

        left = new Canvas(x, y, size, size, 128, 128);
        right = new Canvas(x + size + 10, y, size, size, 128, 128);
        pal = new Canvas(50, 420, (size * 2) + 10, 100, 16, 2);

        back.setBounds(x + 15, 15, 720, 350);
        back.setBackground(shadow);

        back2.setBounds(x + 15, 395, 720, 100);
        back2.setBackground(shadow);

        bar.setBounds(x + 20, 15, 90, 20);
        bar.setBackground(shadow);

        bar2.setBounds(x + 20, 395, 100, 20);
        bar2.setBackground(shadow);

        left.draw(nes.ppu._current[0]);
        right.draw(nes.ppu._current[1]);
        pal.draw(nes.ppu.paletteTable);

        add(left);
        add(right);
        add(pal);
        bar.add(title);
        bar2.add(title2);

        add(bar);
        add(back);
        add(bar2);
        add(back2);

        setSize((size * 2) + 130, 600);
    }

    private void _memory() {
        Font F = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        setTitle("Memory Viewer");

        Color shadow = new Color(0x000030);
        int size = 475;
        offset = 0x0000;
        offset2 = 0x2000;

        console = new StringBuilder();
        console2 = new StringBuilder();

        JPanel back = new JPanel();
        JPanel screen = new JPanel();
        JPanel bar = new JPanel();
        JLabel title = new JLabel("Cpu Memory");
        JButton left = new JButton("<");
        JButton right = new JButton(">");
        JButton skip = new JButton("v");

        JPanel back2 = new JPanel();
        JPanel screen2 = new JPanel();
        JPanel bar2 = new JPanel();
        JLabel title2 = new JLabel("Ppu Memory");
        JButton left2 = new JButton("<");
        JButton right2 = new JButton(">");
        JButton skip2 = new JButton("v");

        JPanel screen3 = new JPanel();
        JPanel screen4 = new JPanel();

        JPanel back3 = new JPanel();
        JPanel back4 = new JPanel();

        cpuLabel = new JLabel();
        cpuLabel.setForeground(Color.white);
        cpuLabel.setFont(F);
        ppuLabel = new JLabel();
        ppuLabel.setForeground(Color.white);
        ppuLabel.setFont(F);
        consoleLabel = new JLabel();
        consoleLabel.setForeground(Color.white);
        consoleLabel.setFont(F);
        consoleLabel2 = new JLabel();
        consoleLabel2.setForeground(Color.white);
        consoleLabel2.setFont(F);

        title.setForeground(Color.WHITE);
        title.setFont(f);
        back.setBounds(x + 15, 15, size + 15, size - 120);
        back.setBackground(shadow);
        bar.setBounds(x + 20, 15, 90, 20);
        bar.setBackground(shadow);
        screen.setBounds(x, y, size, size - 120);
        screen.setBackground(Color.BLACK);

        left.setBounds(x + (size / 2) - 75, y + (size) - 120, 50, 50);
        left.setForeground(Color.white);
        left.setBorder(null);
        left.setBorderPainted(false);
        left.setFocusPainted(false);
        left.setContentAreaFilled(false);

        left.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isSkip)
                    offset = (offset - 0x1000) & 0xffff;
                else
                    offset = (offset - 0x100) & 0xffff;
            }
        });

        skip.setBounds(x + (size / 2) - 25, y + (size) - 120, 50, 50);
        skip.setForeground(Color.white);
        skip.setBorder(null);
        skip.setBorderPainted(false);
        skip.setFocusPainted(false);
        skip.setContentAreaFilled(false);
        skip.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isSkip = !isSkip;
                if (isSkip)
                    skip.setText("^");
                else
                    skip.setText("v");
            }
        });

        right.setBounds(x + (size / 2) + 25, y + (size) - 120, 50, 50);
        right.setForeground(Color.white);
        right.setBorder(null);
        right.setBorderPainted(false);
        right.setFocusPainted(false);
        right.setContentAreaFilled(false);

        right.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isSkip)
                    offset = (offset + 0x1000) & 0xffff;
                else
                    offset = (offset + 0x100) & 0xffff;
            }
        });

        title2.setForeground(Color.WHITE);
        title2.setFont(f);
        back2.setBounds(x + 535, 15, size + 15, size - 120);
        back2.setBackground(shadow);
        bar2.setBounds(x + 540, 15, 90, 20);
        bar2.setBackground(shadow);
        screen2.setBounds(x + 520, y, size, size - 120);
        screen2.setBackground(Color.BLACK);

        left2.setBounds((x * 7) + size - 100, y + (size) - 120, 50, 50);
        left2.setForeground(Color.white);
        left2.setBorder(null);
        left2.setBorderPainted(false);
        left2.setFocusPainted(false);
        left2.setContentAreaFilled(false);

        left2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isSkip2)
                    offset2 = (offset2 - 0x1000) & 0x3fff;
                else
                    offset2 = (offset2 - 0x100) & 0x3fff;
            }
        });

        skip2.setBounds(x * 7 + (size) - 50, y + (size) - 120, 50, 50);
        skip2.setForeground(Color.white);
        skip2.setBorder(null);
        skip2.setBorderPainted(false);
        skip2.setFocusPainted(false);
        skip2.setContentAreaFilled(false);
        skip2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isSkip2 = !isSkip2;
                if (isSkip2)
                    skip2.setText("^");
                else
                    skip2.setText("v");
            }
        });

        right2.setBounds((x * 7) + (size), y + (size) - 120, 50, 50);
        right2.setForeground(Color.white);
        right2.setBorder(null);
        right2.setBorderPainted(false);
        right2.setFocusPainted(false);
        right2.setContentAreaFilled(false);

        right2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isSkip2)
                    offset2 = (offset2 + 0x1000) & 0x3fff;
                else
                    offset2 = (offset2 + 0x100) & 0x3fff;
            }
        });

        back3.setBounds(x + size - 130, y + size - 70, size / 3, size - 205);
        back3.setBackground(shadow);

        screen3.setBounds(x + size - 160, y + size - 50, size / 3, size - 205);
        screen3.setBackground(Color.BLACK);

        back4.setBounds(size * 2 - x + 15, y + size - 70, size / 3, size - 205);
        back4.setBackground(shadow);

        screen4.setBounds(size * 2 - x - 15, y + size - 50, size / 3, size - 205);
        screen4.setBackground(Color.BLACK);

        bar.add(title);
        screen.add(cpuLabel);
        add(bar);
        add(screen);
        add(back);
        add(left);
        add(skip);
        add(right);
        add(left2);
        add(skip2);
        add(right2);

        bar2.add(title2);
        screen2.add(ppuLabel);
        add(bar2);
        add(screen2);
        add(back2);

        screen3.add(consoleLabel);
        screen4.add(consoleLabel2);

        add(screen3);
        add(screen4);
        add(back3);
        add(back4);
        getContentPane().setBackground(new Color(0x000060));

        setSize((size * 2) + 170, 800);
    }

    public void update() {
        switch (tool) {
            case 0 -> {
                int tmp[] = new int[32];
                for(int i = 0; i < 32; i++)
                    tmp[i] = nes.ppu.palette[nes.ppu.paletteTable[i]];
                pal.draw(tmp);
            }
            case 1 -> {
                String s;

                if (nes.cpu._debug != null)
                    console.append(nes.cpu._debug);
                if (nes.ppu._debug != null)
                    console2.append(nes.ppu._debug);

                if (counter > 13) {
                    console = new StringBuilder(console.substring(console.indexOf("\n") + 1).trim() + "\n");
                    console2 = new StringBuilder(console2.substring(console2.indexOf("\n") + 1).trim() + "\n");
                } else
                    counter++;

                cpuMap = new StringBuilder(
                        "<font color=red>MEM </font>| <b><font color=yellow>00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F</font></b>");
                ppuMap = new StringBuilder(
                        "<font color=red>VMEM</font>| <b><font color=yellow>00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F</font></b>");
                for (int i = 0; i <= 0xFF; i++) {
                    if (i % 16 == 0) {
                        s = Integer.toHexString(offset + i);
                        if (s.length() < 4)
                            s = ("0".repeat(4 - s.length())) + s.toUpperCase();
                        cpuMap.append("\n");
                        cpuMap.append("<b><font color=yellow>" + s + "</font></b>| ");
                        s = Integer.toHexString(offset2 + i);
                        if (s.length() < 4)
                            s = ("0".repeat(4 - s.length())) + s.toUpperCase();
                        ppuMap.append("\n");
                        ppuMap.append("<b><font color=yellow>" + s + "</font></b>| ");
                    }
                    s = "00";
                    // Special case for PPUSTATUS so to not clear vblank
                    if((offset + i) >= 0x2000 && (offset + i) <= 0x3fff && (((offset + i) & 0x7) == 2 ))
                        s = Integer.toHexString(nes.ppu.status.get());
                    if((offset + i) >= 0x2000 && (offset + i) <= 0x3fff && (((offset + i) & 0x7) == 7 ))
                        s = Integer.toHexString(nes.ppu.buffer);
                    else
                    s = Integer.toHexString(nes.cpu.read(offset + i));

                    if (s.length() == 1)
                        s = "0" + s;
                    cpuMap.append(s + "\t");
                    s = Integer.toHexString(nes.ppu.read(offset2 + i));
                    if (s.length() == 1)
                        s = "0" + s;
                    ppuMap.append(s + "\t");
                }
                cpuLabel.setText(format(cpuMap));
                ppuLabel.setText(format(ppuMap));
                consoleLabel.setText(format(console));
                consoleLabel2.setText(format(console2));
            }

        }

    }

    private String format(StringBuilder raw) {
        String s = raw.toString();

        return "<html>" + s.toString().replace("\n", "<br>") + "</html>";
    }
}