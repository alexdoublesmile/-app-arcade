package com.joyful.arcade.process;

import com.joyful.arcade.listener.KeyboardListener;
import com.joyful.arcade.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static com.joyful.arcade.util.FrameConstants.TARGET_FRAME_TIME;
import static com.joyful.arcade.util.TimeHelper.nanosToMillis;
import static com.joyful.arcade.util.WindowConstants.PANEL_HEIGHT;
import static com.joyful.arcade.util.WindowConstants.PANEL_WIDTH;
import static java.lang.System.nanoTime;

public class GamePanel extends JPanel implements Runnable {
    private Thread thread;
    private boolean running = true;

    public static Player player;
    public static ArrayList<Bullet> bullets = new ArrayList<>();
    public static ArrayList<Enemy> enemies = new ArrayList<>();
    public static ArrayList<Explosion> explosions = new ArrayList<>();
    public static ArrayList<PowerUp> powerUps = new ArrayList<>();
    public static ArrayList<Text> texts = new ArrayList<>();

    private BufferedImage mainImage;
    private Graphics2D mainGraphics;

    private long waveStartTimer;
    private long waveStartTimerDiff;
    private int waveNumber;
    private int waveDelay = 2000;
    private boolean waveStart = true;

    private long slowDownTimer;
    private long slowDownTimerDiff;
    private int slowDownLength = 6000;

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setFocusable(true);
        requestFocus();

        player = new Player();

        addKeyListener(new KeyboardListener(player));
        initGraphics();
    }

    private void initGraphics() {
        mainImage = new BufferedImage(PANEL_WIDTH, PANEL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        mainGraphics = (Graphics2D) mainImage.getGraphics();
        mainGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        mainGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
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
        while(running) {
            final long waitTime = makeFrame();
            waitFrameForTargetTime(waitTime);
        }
        gameOver();
    }

    private long makeFrame() {
        final long startTime = nanoTime();

        gameUpdate();
        gameRender();
        gameDraw();

        long frameTime = nanosToMillis((nanoTime() - startTime));
        return TARGET_FRAME_TIME - frameTime;
    }

    private void waitFrameForTargetTime(long waitTime) {
        if (waitTime > 0) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void gameOver() {
        mainGraphics.setColor(new Color(0, 100, 255));
        mainGraphics.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        mainGraphics.setColor(Color.WHITE);
        mainGraphics.setFont(new Font("Century Gothic", Font.PLAIN, 16));
        String s = "G A M E   O V E R";
        int length = (int) mainGraphics.getFontMetrics().getStringBounds(s, mainGraphics).getWidth();
        mainGraphics.drawString(s, (PANEL_WIDTH - length) / 2, PANEL_HEIGHT / 2);
        s = "Final score: " + player.getScore();
        length = (int) mainGraphics.getFontMetrics().getStringBounds(s, mainGraphics).getWidth();
        mainGraphics.drawString(s, (PANEL_WIDTH - length) / 2, PANEL_HEIGHT / 2 + 30);
        gameDraw();
    }

    private void gameUpdate() {
        // new wave
        if (waveStartTimer == 0 && enemies.size() == 0) {
            waveNumber++;
            waveStart = false;
            waveStartTimer = nanoTime();
        } else {
            waveStartTimerDiff = (nanoTime() - waveStartTimer) / 1000_000;
            if (waveStartTimerDiff > waveDelay) {
                waveStart = true;
                waveStartTimer = 0;
                waveStartTimerDiff = 0;
            }
        }
        if (waveStart && enemies.size() == 0) {
            createNewEnemies();
        }


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

        // update explosions
        for (int i = 0; i < explosions.size(); i++) {
            boolean remove = explosions.get(i).update();
            if (remove) {
                explosions.remove(i);
                i--;
            }
        }

        // update texts
        for (int i = 0; i < texts.size(); i++) {
            boolean remove = texts.get(i).update();
            if (remove) {
                texts.remove(i);
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
                } else if (random < 0.13) {
                    powerUps.add(new PowerUp(4, enemy.getX(), enemy.getY()));
                }

                player.addScore(enemy.getType() + enemy.getRank());
                enemies.remove(i);
                i--;

                enemy.explode();
                explosions.add(new Explosion(enemy.getX(), enemy.getY(), enemy.getR(), enemy.getR() + 20));
            }
        }

        //update dead player
        if (player.isDead()) {
            running = false;
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

                if (powerUp.getType() == 1) {
                    player.gainLife();
                    texts.add(new Text(player.getX(), player.getY(), 2000, "Extra life"));
                }
                if (powerUp.getType() == 2) {
                    player.increasePower(1);
                    texts.add(new Text(player.getX(), player.getY(), 2000, "Power"));
                }
                if (powerUp.getType() == 3) {
                    player.increasePower(2);
                    texts.add(new Text(player.getX(), player.getY(), 2000, "Double Power"));
                }
                if (powerUp.getType() == 4) {
                    slowDownTimer = nanoTime();
                    for (int j = 0; j < enemies.size(); j++) {
                        enemies.get(j).setSlow(true);
                    }
                    texts.add(new Text(player.getX(), player.getY(), 2000, "Slow Down"));
                }

                powerUps.remove(i);
                i--;
            }
        }

        // slowdown time
        if (slowDownTimer > 0) {
            slowDownTimerDiff = (nanoTime() - slowDownTimer) / 1000_000;
            if (slowDownTimerDiff > slowDownLength) {
                slowDownTimer = 0;
                for (int j = 0; j < enemies.size(); j++) {
                    enemies.get(j).setSlow(false);
                }
            }
        }
    }

    private void gameRender() {
        // render background
        final Color backgroundColor = new Color(0, 100, 255);
        mainGraphics.setColor(backgroundColor);
        mainGraphics.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        // render slow background
        if (slowDownTimer != 0) {
            mainGraphics.setColor(new Color(255, 255, 255, 64));
            mainGraphics.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        }

        // render player
        player.draw(mainGraphics);

        // render bullets
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).draw(mainGraphics);
        }

        // render enemies
        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).draw(mainGraphics);
        }

        // render power ups
        for (int i = 0; i < powerUps.size(); i++) {
            powerUps.get(i).draw(mainGraphics);
        }

        // render explosions
        for (int i = 0; i < explosions.size(); i++) {
            explosions.get(i).draw(mainGraphics);
        }

        // render texts
        for (int i = 0; i < texts.size(); i++) {
            texts.get(i).draw(mainGraphics);
        }

        // render wave numbers
        if (waveStartTimer != 0) {
            mainGraphics.setFont(new Font("Century Gothic", Font.PLAIN, 18));
            final String s = "- W A V E  " + waveNumber + "  -";
            // ?
            final int length = (int) mainGraphics.getFontMetrics().getStringBounds(s, mainGraphics).getWidth();
            // ?
            int alpha = (int) (255 * Math.sin(3.14 * waveStartTimerDiff / waveDelay));
            // ?
            if (alpha > 255) {
                alpha = 255;
            }
            // ?
            mainGraphics.setColor(new Color(255, 255, 255, alpha));
            // ?
            mainGraphics.drawString(s, PANEL_WIDTH / 2 - length / 2, PANEL_HEIGHT / 2);
        }

        //  render player lives
        for (int i = 0; i < player.getLives(); i++) {
            mainGraphics.setColor(Color.WHITE);
            mainGraphics.fillOval(20 + (20 * i), 20, player.getR() * 2, player.getR() * 2);

            mainGraphics.setStroke(new BasicStroke(3));
            mainGraphics.setColor(Color.WHITE.darker());
            mainGraphics.drawOval(20 + (20 * i), 20, player.getR() * 2, player.getR() * 2);

            mainGraphics.setStroke(new BasicStroke(1));
        }

        // render player powers
        mainGraphics.setColor(Color.YELLOW);
        mainGraphics.fillRect(20, 40, player.getPower() * 8, 8);
        mainGraphics.setColor(Color.YELLOW.darker());

        mainGraphics.setStroke(new BasicStroke(2));
        for (int i = 0; i < player.getRequiredPower(); i++) {
            mainGraphics.drawRect(20 + 8 * i, 40, 8, 8);
        }

        mainGraphics.setStroke(new BasicStroke(1));


        // render player scores
        mainGraphics.setColor(Color.WHITE);
        mainGraphics.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        mainGraphics.drawString("Score: " + player.getScore(), PANEL_WIDTH - 100, 30);

        // render slow down meter
        if (slowDownTimer != 0) {
            mainGraphics.setColor(Color.WHITE);
            mainGraphics.drawRect(20, 60, 100, 8);
            mainGraphics.fillRect(20, 60, (int) ((slowDownLength - slowDownTimerDiff) * 100 / slowDownLength), 8);
        }
    }

    private void gameDraw() {
        final Graphics g2 = this.getGraphics();
        g2.drawImage(mainImage, 0, 0, null);
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
                enemies.add(new Enemy(1, 1));
            }
            enemies.add(new Enemy(1, 2));
            enemies.add(new Enemy(1, 2));
        }
        if (waveNumber == 4) {
            for (int i = 0; i < 4; i++) {
                enemies.add(new Enemy(2, 1));
            }
            enemies.add(new Enemy(1, 3));
            enemies.add(new Enemy(1, 4));
        }
        if (waveNumber == 5) {
            enemies.add(new Enemy(2, 3));
            enemies.add(new Enemy(1, 3));
            enemies.add(new Enemy(1, 4));
        }
        if (waveNumber == 6) {
            enemies.add(new Enemy(1, 3));
            for (int i = 0; i < 4; i++) {
                enemies.add(new Enemy(2, 1));
                enemies.add(new Enemy(3, 1));
            }
        }
        if (waveNumber == 7) {
            enemies.add(new Enemy(1, 3));
            enemies.add(new Enemy(2, 3));
            enemies.add(new Enemy(3, 3));
        }
        if (waveNumber == 8) {
            enemies.add(new Enemy(1, 4));
            enemies.add(new Enemy(2, 4));
            enemies.add(new Enemy(3, 4));
        }
        if (waveNumber == 9) {
            running = false;
        }
    }
}
