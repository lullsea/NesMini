package net.lullsea.NesMini;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import java.awt.image.*;

public class Debugger extends JFrame{
    PatternGraphics left, right;
    Debugger(){
        super("Debug");
        
        left = new PatternGraphics(25, 30);
        right = new PatternGraphics(25, 400);

        setSize(420, 815);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        getContentPane().setBackground(new Color(0x802525));

        add(left);
        add(right);

        setVisible(true);


    }

    public class PatternGraphics extends JPanel {
        private int FPS;

        // Graphics
        private final BufferedImage image;

        PatternGraphics(int x, int y) {
            super();
            setVisible(true);
            setBounds(x, y, 350, 350);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        }

        public void Draw(int[] rgbArray) {
            image.setRGB(0, 0, 128, 128, rgbArray, 0, 128);
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            Graphics2D g2D = (Graphics2D) g;
            g2D.drawImage(image, 0, 0, 400, 400, null);
            g2D.setColor(Color.WHITE);

        }
    }
}
