package net.lullsea.NesMini;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;

public class Canvas extends JPanel {
    private final BufferedImage image;
    private int width, height, i, j;
    final private int serialVersionUID = 203;

    Canvas(int x, int y, int width, int height, int i, int j) {
        super();
        this.width = width;
        this.height = height;
        this.i = i;
        this.j = j;

        setVisible(true);
        setBounds(x, y, this.width, this.height);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        image = new BufferedImage(i, j, BufferedImage.TYPE_INT_RGB);
    }

    public void draw(int[] rgbArray) {
        image.setRGB(0, 0, i, j, rgbArray, 0, i);
        repaint();
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        g2D.drawImage(image, 0, 0, width, height, null);
        g2D.setColor(Color.WHITE);
    }
}
