package net.lullsea.NesMini;

import java.awt.*;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Debugger extends JFrame {
    // Pattern
    Canvas left, right, pal;
    Nes nes;
    // Memory
    StringBuilder cpuMap, ppuMap;
    JLabel cpuLabel, ppuLabel;
    int offset;

    int tool;

    private Font f = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    Debugger(Nes nes, int tool) {
        super("Debug");
        this.nes = nes;
        this.tool = tool;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(null);
        // setResizable(false);
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
        Color shadow = new Color(0x602020);
        setTitle("PPU View");

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

        Color shadow = new Color(0x000030);
        int size = 475;
        offset = 0x200;

        JPanel back = new JPanel();
        JPanel screen = new JPanel();
        JPanel bar = new JPanel();
        JLabel title = new JLabel("Cpu Memory");

        JPanel back2 = new JPanel();
        JPanel screen2 = new JPanel();
        JPanel bar2 = new JPanel();
        JLabel title2 = new JLabel("Ppu Memory");

        cpuLabel = new JLabel();
        cpuLabel.setForeground(Color.white);
        cpuLabel.setFont(F);
        ppuLabel = new JLabel();
        ppuLabel.setForeground(Color.white);
        ppuLabel.setFont(F);

        title.setForeground(Color.WHITE);
        title.setFont(f);
        back.setBounds(x + 15, 15, size + 15, size - 120);
        back.setBackground(shadow);
        bar.setBounds(x + 20, 15, 90, 20);
        bar.setBackground(shadow);
        screen.setBounds(x, y, size, size - 120);
        screen.setBackground(Color.BLACK);

        title2.setForeground(Color.WHITE);
        title2.setFont(f);
        back2.setBounds(x + 515, 15, size + 15, size - 120);
        back2.setBackground(shadow);
        bar2.setBounds(x + 520, 15, 90, 20);
        bar2.setBackground(shadow);
        screen2.setBounds(x + 500, y, size, size - 120);
        screen2.setBackground(Color.BLACK);

        bar.add(title);
        screen.add(cpuLabel);
        add(bar);
        add(screen);
        add(back);

        bar2.add(title2);
        screen2.add(ppuLabel);
        add(bar2);
        add(screen2);
        add(back2);
        getContentPane().setBackground(new Color(0x000060));

        setSize((size * 2) + 150, 600);
    }

    public void update() {
        switch (tool) {
            case 0 -> pal.draw(nes.ppu.paletteTable);
            case 1 -> {
                String s;
                ;

                cpuMap = new StringBuilder(
                        "<font color=red>RAM </font>| <b><font color=yellow>00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F</font></b>");
                ppuMap = new StringBuilder(
                        "<font color=red>VRAM</font>| <b><font color=yellow>00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F</font></b>");
                for (int i = 0; i <= 0xFF; i++) {
                    if (i % 16 == 0) {
                        s = Integer.toHexString(offset + i);
                        if (s.length() == 3)
                            s = "0" + s.toUpperCase();
                        cpuMap.append("\n");
                        cpuMap.append("<b><font color=yellow>" + s + "</font></b>| ");
                        s = Integer.toHexString((offset & 0x3fff) + i);
                        if (s.length() == 3)
                            s = "0" + s.toUpperCase();
                        ppuMap.append("\n");
                        ppuMap.append("<b><font color=yellow>" + s + "</font></b>| ");
                    }
                    s = Integer.toHexString(nes.cpu.read(offset + i));
                    if (s.length() == 1)
                        s = "0" + s;
                    cpuMap.append(s + "\t");
                    s = Integer.toHexString(nes.ppu.read(offset + i));
                    if (s.length() == 1)
                        s = "0" + s;
                    ppuMap.append(s + "\t");

                }
                cpuLabel.setText(format(cpuMap));
                ppuLabel.setText(format(ppuMap));

            }

        }
    }

    private String format(StringBuilder raw) {
        String s = raw.toString();

        return "<html>" + s.toString().replace("\n", "<br>") + "</html>";
    }
}