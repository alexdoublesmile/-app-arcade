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
    public static ArrayList<PowerUp> powerUps = new ArrayList<>();

    private long waveStartTimer;
    private long waveStartTimerDiff;
    private int waveNumber;
    private int waveDelay = 2000;
    private boolean waveStart;

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
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        player = new Player();
        waveStart = true;

        // game loop
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

            // wait for 30 frames(game iterations) per second
            if (waitTime > 0) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // for render fps value
//            totalTime += nanoTime() - startTime;
//            frameCount++;
//            if (frameCount == maxFrameCount) {
//                averageFps = 1000.0 / (totalTime / frameCount / 1000_000);
//                frameCount = 0;
//                totalTime = 0;
//            }
        }
    }

    private void gameUpdate() {
        // new wave
        // if wave not started & no enemies, start timer & don't start wave than (wave start = false)
        if (waveStartTimer == 0 && enemies.size() == 0) {
            waveNumber++;
            waveStart = false;
            waveStartTimer = nanoTime();
        } else {
            // start new wave if last wave was started more than delay time ago (wave start = true)
            waveStartTimerDiff = (nanoTime() - waveStartTimer) / 1000_000;
            if (waveStartTimerDiff > waveDelay) {
                waveStart = true;
                waveStartTimer = 0;
                waveStartTimerDiff = 0;
            }
        }

        // create enemies if needs (wave start true & no enemies)
        if (waveStart && enemies.size() == 0) {
            createNewEnemies();
        }

        // update player
        player.update();

        // update bullets
        for (int i = 0; i < bullets.size(); i++) {
            boolean remove = bullets.get(i).update();
            if (remove) {
                bullets.remove(i);
                i--;
            }
        }

        // update enemies
        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).update();
        }

        // update power ups
        for (int i = 0; i < powerUps.size(); i++) {
            boolean remove = powerUps.get(i).update();
            if (remove) {
                powerUps.remove(i);
                i--;
            }
        }


        // update enemy-bullet collisions
        for (int i = 0; i < bullets.size(); i++) {
            final Bullet bullet = bullets.get(i);
            final double bx = bullet.getX();
            final double by = bullet.getY();
            final double br = bullet.getR();

            for (int j = 0; j < enemies.size(); j++) {
                final Enemy enemy = enemies.get(j);
                final double ex = enemy.getX();
                final double ey = enemy.getY();
                final double er = enemy.getR();

                final double dx = bx - ex;
                final double dy = by - ey;
                // ?
                final double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < br + er) {
                    enemy.hit();
                    bullets.remove(i);
                    i--;
                    break;
                }
            }
        }

        // update player-enemy collision
        if (!player.isRecovering()) {
            int px = player.getX();
            int py = player.getY();
            int pr = player.getR();
            for (int i = 0; i < enemies.size(); i++) {
                final Enemy enemy = enemies.get(i);
                final double ex = enemy.getX();
                final double ey = enemy.getY();
                final double er = enemy.getR();

                double dx = px - ex;
                double dy = py - ey;
                final double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < pr + er) {
                    player.loseLife();
                }
            }
        }

        // update enemies dead
        for (int i = 0; i < enemies.size(); i++) {
            final Enemy enemy = enemies.get(i);
            if (enemy.isDead()) {

                // chance for poweruo
                final double random = Math.random();
                if (random < 0.001) {
                    powerUps.add(new PowerUp(1, enemy.getX(), enemy.getY()));
                } else if (random < 0.02) {
                    powerUps.add(new PowerUp(3, enemy.getX(), enemy.getY()));
                } else if (random < 0.12) {
                    powerUps.add(new PowerUp(2, enemy.getX(), enemy.getY()));
                } else {
                    powerUps.add(new PowerUp(2, enemy.getX(), enemy.getY()));
                }

                player.addScore(enemy.getType() + enemy.getRank());
                enemies.remove(i);
                i--;
            }
        }

        // update player-power ups collision
        int px = player.getX();
        int py = player.getY();
        int pr = player.getR();
        for (int i = 0; i < powerUps.size(); i++) {
            final PowerUp powerUp = powerUps.get(i);
            final double ex = powerUp.getX();
            final double ey = powerUp.getY();
            final double er = powerUp.getR();

            double dx = px - ex;
            double dy = py - ey;
            final double dist = Math.sqrt(dx * dx + dy * dy);

            // collected power ups
            if (dist < pr + er) {
                powerUps.remove(i);

                if (powerUp.getType() == 1) {
                    player.gainLife();
                }

                if (powerUp.getType() == 2) {
                    player.increasePower(1);
                }

                if (powerUp.getType() == 3) {
                    player.increasePower(2);
                }
            }
        }
    }

    private void gameRender() {
        // render background
        final Color backgroundColor = new Color(0, 100, 255);
        g.setColor(backgroundColor);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // render player
        player.draw(g);

        // render bullets
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).draw(g);
        }

        // render enemies
        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).draw(g);
        }

        // render power ups
        for (int i = 0; i < powerUps.size(); i++) {
            powerUps.get(i).draw(g);
        }

        // render wave numbers
        if (waveStartTimer != 0) {
            g.setFont(new Font("Century Gothic", Font.PLAIN, 18));
            final String s = "- W A V E  " + waveNumber + "  -";
            // ?
            final int length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
            // ?
            int alpha = (int) (255 * Math.sin(3.14 * waveStartTimerDiff / waveDelay));
            // ?
            if (alpha > 255) {
                alpha = 255;
            }
            // ?
            g.setColor(new Color(255, 255, 255, alpha));
            // ?
            g.drawString(s, WIDTH / 2 - length / 2, HEIGHT / 2);
        }

        //  render player lives
        for (int i = 0; i < player.getLives(); i++) {
            g.setColor(Color.WHITE);
            g.fillOval(20 + (20 * i), 20, player.getR() * 2, player.getR() * 2);

            g.setStroke(new BasicStroke(3));
            g.setColor(Color.WHITE.darker());
            g.drawOval(20 + (20 * i), 20, player.getR() * 2, player.getR() * 2);

            g.setStroke(new BasicStroke(1));
        }

        // render player powers
        g.setColor(Color.YELLOW);
        g.fillRect(20, 40, player.getPower() * 8, 8);
        g.setColor(Color.YELLOW.darker());

        g.setStroke(new BasicStroke(2));
        for (int i = 0; i < player.getRequiredPower(); i++) {
            g.drawRect(20 + 8 * i, 40, 8, 8);
        }

        g.setStroke(new BasicStroke(1));


        // render player scores
        g.setColor(Color.WHITE);
        g.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        g.drawString("Score: " + player.getScore(), WIDTH - 100, 30);
    }

    private void gameDraw() {
        final Graphics g2 = this.getGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
    }

    private void createNewEnemies() {
        enemies.clear();
        if (waveNumber == 1) {
            for (int i = 0; i < 4; i++) {
                enemies.add(new Enemy(1, 1));
            }
        }
        if (waveNumber == 2) {
            for (int i = 0; i < 8; i++) {
                enemies.add(new Enemy(1, 1));
            }
        }
        if (waveNumber == 3) {
            for (int i = 0; i < 4; i++) {
                enemies.add(new Enemy(2, 1));
            }
            for (int i = 0; i < 4; i++) {
                enemies.add(new Enemy(3, 1));
            }
        }
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
