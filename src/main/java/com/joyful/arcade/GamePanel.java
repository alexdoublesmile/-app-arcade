package com.joyful.arcade;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel implements Runnable {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 400;
    private static final int FPS = 30;
    private double averageFps;

    private Thread thread;
    private boolean running;

    private BufferedImage image;
    private Graphics2D g;


    public GamePanel() {
        super();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();

    }

    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    @Override
    public void run() {

        running = true;

        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();

        long startTime;
        long URDTimeMillis;
        long waitTime;
        long totalTime;

        int frameCount;
        int maxFrameCount = 30;
        
        while(running) {

            gameUpdate();
            gameRender();
            gameDraw();
        }
    }

    private void gameUpdate() {

    }

    private void gameRender() {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.BLACK);
        g.drawString("test str", 100, 100);
    }

    private void gameDraw() {
        final Graphics g2 = getGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
    }
}
