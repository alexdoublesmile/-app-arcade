package com.joyful.arcade;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static java.awt.event.KeyEvent.*;
import static java.lang.System.nanoTime;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    public static final int WIDTH = 400;
    public static final int HEIGHT = 400;
    private static final int FPS = 30;
    private double averageFps;

    private Thread thread;
    private boolean running;

    private BufferedImage image;
    private Graphics2D g;

    public static Player player;
    public static ArrayList<Bullet> bullets = new ArrayList<>();
    public static ArrayList<Enemy> enemies = new ArrayList<>();

    public GamePanel() {
        super();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        setFocusable(true);
        requestFocus(); // for key listener to get keys
    }

    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
        addKeyListener(this);
    }

    @Override
    public void run() {
        running = true;

        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();

        player = new Player();
        for (int i = 0; i < 5; i++) {
            enemies.add(new Enemy(1, 1));
        }

        long startTime;
        long URDTimeMillis;
        long waitTime;
        long totalTime = 0;

        int frameCount = 0;
        int maxFrameCount = 30;

        long targetTime = 1000 / FPS;

        while(running) {

            startTime = nanoTime();

            gameUpdate();
            gameRender();
            gameDraw();

            URDTimeMillis = (nanoTime() - startTime) / 1000_000;
            waitTime = targetTime - URDTimeMillis;

            if (waitTime > 0) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            totalTime += nanoTime() - startTime;
            frameCount++;
            if (frameCount == maxFrameCount) {
                averageFps = 1000.0 / (totalTime / frameCount / 1000_000);
                frameCount = 0;
                totalTime = 0;
            }
        }
    }

    private void gameUpdate() {
        player.update();

        for (int i = 0; i < bullets.size(); i++) {
            boolean remove = bullets.get(i).update();
            if (remove) {
                bullets.remove(i);
                i--;
            }
        }

        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).update();
        }
    }

    private void gameRender() {
        final Color backgroundColor = new Color(0, 100, 255);
        g.setColor(backgroundColor);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.BLACK);
        g.drawString("FPS: " + averageFps, 10, 10);
        g.drawString("Bullets counter: " + bullets.size(), 10, 20);

        player.draw(g);

        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).draw(g);
        }

        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).draw(g);
        }
    }

    private void gameDraw() {
        final Graphics g2 = this.getGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        final int keyCode = e.getKeyCode();
        if (keyCode == VK_LEFT) {
            player.setLeft(true);
        }
        if (keyCode == VK_RIGHT) {
            player.setRight(true);
        }
        if (keyCode == VK_UP) {
            player.setUp(true);
        }
        if (keyCode == VK_DOWN) {
            player.setDown(true);
        }
        if (keyCode == VK_Z) {
            player.setFiring(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        final int keyCode = e.getKeyCode();
        if (keyCode == VK_LEFT) {
            player.setLeft(false);
        }
        if (keyCode == VK_RIGHT) {
            player.setRight(false);
        }
        if (keyCode == VK_UP) {
            player.setUp(false);
        }
        if (keyCode == VK_DOWN) {
            player.setDown(false);
        }
        if (keyCode == VK_Z) {
            player.setFiring(false);
        }
    }
}
