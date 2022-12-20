package net.lullsea.NesMini;

import java.awt.*;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.image.BufferedImage;

public class NesFrame extends JFrame {
    Graphics graphic = new Graphics();

    NesFrame() {
        super("Nes Mini - by Lull");
        setSize(820, 940);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        getContentPane().setBackground(new Color(0x505050));
        
        add(graphic);
        setVisible(true);
    }

    public class Graphics extends JPanel {
        private int FPS;

        // Graphics
        private final BufferedImage image;

        Graphics() {
            super();
            setVisible(true);
            setBounds(50, 50, 699, 796);
            image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        }

        public void Draw(int[] rgbArray) {
            // image.setRGB(0, 0, 256, 240, rgbArray, 0, 256);
            image.setRGB(0, 0, 128, 128, rgbArray, 0, 128);
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            Graphics2D g2D = (Graphics2D) g;
            g2D.drawImage(image, 0, 0, 233 * 3, 199 * 4, null);
            g2D.setColor(Color.WHITE);

        }
    }
}