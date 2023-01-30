package com.joyful.arcade.process;

import com.joyful.arcade.exception.WaitFrameException;
import com.joyful.arcade.listener.KeyboardListener;
import com.joyful.arcade.model.*;
import com.joyful.arcade.model.Window;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static com.joyful.arcade.util.FrameConstants.TARGET_FRAME_TIME;
import static com.joyful.arcade.util.TimeHelper.nanosToMillis;
import static com.joyful.arcade.util.WindowConstants.PANEL_HEIGHT;
import static com.joyful.arcade.util.WindowConstants.PANEL_WIDTH;
import static java.lang.System.nanoTime;

public class GamePanel extends JPanel implements Runnable {
    private Thread thread;
    private boolean running = true;

    private Window window = new Window();
    public static List<Explosion> explosions = new ArrayList<>();
    public static List<PowerUp> powerUps = new ArrayList<>();
    public static List<Text> texts = new ArrayList<>();

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
        
        addKeyListener(new KeyboardListener(window.getPlayer()));
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
                throw new WaitFrameException(e);
            }
        }
    }

    private void gameUpdate() {
        List<Bullet> bullets = window.getBullets();
        List<Enemy> enemies = window.getEnemies();

        // new wave
        if (waveStartTimer == 0 && enemies.size() == 0) {
            waveNumber++;
            waveStart = false;
            waveStartTimer = nanoTime();
        } else {
            waveStartTimerDiff = nanosToMillis((nanoTime() - waveStartTimer));
            if (waveStartTimerDiff > waveDelay) {
                waveStart = true;
                waveStartTimer = 0;
                waveStartTimerDiff = 0;
            }
        }
        if (waveStart && enemies.size() == 0) {
            createNewEnemies();
        }

        window.getPlayer().update();
        enemies.forEach(Enemy::update);
        updateElements(window.getBullets());
        updateElements(powerUps);
        updateElements(explosions);
        updateElements(texts);

        checkCollisions(bullets, enemies);

        // update enemy-bullet collisions
//        for (int i = 0; i < bullets.size(); i++) {
//            final Bullet bullet = bullets.get(i);
//            final double bx = bullet.getX();
//            final double by = bullet.getY();
//            final double br = bullet.getR();
//
//            for (final Enemy enemy : enemies) {
//                final double ex = enemy.getX();
//                final double ey = enemy.getY();
//                final double er = enemy.getR();
//
//                final double dx = bx - ex;
//                final double dy = by - ey;
//                final double dist = Math.sqrt(dx * dx + dy * dy);
//
//                if (dist < br + er) {
//                    enemy.hit();
//                    bullets.remove(i);
//                    i--;
//                    break;
//                }
//            }
//        }

        // update window.getPlayer()-enemy collision
        if (!window.getPlayer().isRecovering()) {
            int px = window.getPlayer().getX();
            int py = window.getPlayer().getY();
            int pr = window.getPlayer().getR();
            for (final Enemy enemy : enemies) {
                final double ex = enemy.getX();
                final double ey = enemy.getY();
                final double er = enemy.getR();

                double dx = px - ex;
                double dy = py - ey;
                final double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < pr + er) {
                    window.getPlayer().loseLife();
                }
            }
        }

        // update enemies dead
        final List<Enemy> enemiesForRemove = new ArrayList<>();
        final List<Enemy> enemiesForAdd = new ArrayList<>();
        for (Enemy enemy : window.getEnemies()) {
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

                window.getPlayer().addScore(enemy.getType() + enemy.getRank());

                final List<Enemy> explodedMinions = enemy.explode();

                enemiesForRemove.add(enemy);
                enemiesForAdd.addAll(explodedMinions);

                explosions.add(new Explosion(enemy.getX(), enemy.getY(), enemy.getR(), enemy.getR() + 20));
            }
        }
        enemiesForRemove.forEach(enemy -> window.removeEnemy(enemy));
        enemiesForAdd.forEach(enemy -> window.addEnemy(enemy));

        //update dead window.getPlayer()
        if (window.getPlayer().isDead()) {
            running = false;
        }

        // update window.getPlayer()-power ups collision
        int px = window.getPlayer().getX();
        int py = window.getPlayer().getY();
        int pr = window.getPlayer().getR();
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
                    window.getPlayer().gainLife();
                    texts.add(new Text(window.getPlayer().getX(), window.getPlayer().getY(), 2000, "Extra life"));
                }
                if (powerUp.getType() == 2) {
                    window.getPlayer().increasePower(1);
                    texts.add(new Text(window.getPlayer().getX(), window.getPlayer().getY(), 2000, "Power"));
                }
                if (powerUp.getType() == 3) {
                    window.getPlayer().increasePower(2);
                    texts.add(new Text(window.getPlayer().getX(), window.getPlayer().getY(), 2000, "Double Power"));
                }
                if (powerUp.getType() == 4) {
                    slowDownTimer = nanoTime();
                    for (Enemy enemy : enemies) {
                        enemy.setSlow(true);
                    }
                    texts.add(new Text(window.getPlayer().getX(), window.getPlayer().getY(), 2000, "Slow Down"));
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
                for (Enemy enemy : enemies) {
                    enemy.setSlow(false);
                }
            }
        }
    }

    private void gameRender() {
        List<Bullet> bullets = window.getBullets();
        List<Enemy> enemies = window.getEnemies();

        // render background
        final Color backgroundColor = new Color(0, 100, 255);
        mainGraphics.setColor(backgroundColor);
        mainGraphics.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        // render slow background
        if (slowDownTimer != 0) {
            mainGraphics.setColor(new Color(255, 255, 255, 64));
            mainGraphics.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        }

        window.getPlayer().draw(mainGraphics);
        drawElements(bullets);
        drawElements(enemies);
        drawElements(powerUps);
        drawElements(explosions);
        drawElements(texts);
        drawElements(bullets);

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

        //  render window.getPlayer() lives
        for (int i = 0; i < window.getPlayer().getLives(); i++) {
            mainGraphics.setColor(Color.WHITE);
            mainGraphics.fillOval(20 + (20 * i), 20, window.getPlayer().getR() * 2, window.getPlayer().getR() * 2);

            mainGraphics.setStroke(new BasicStroke(3));
            mainGraphics.setColor(Color.WHITE.darker());
            mainGraphics.drawOval(20 + (20 * i), 20, window.getPlayer().getR() * 2, window.getPlayer().getR() * 2);

            mainGraphics.setStroke(new BasicStroke(1));
        }

        // render window.getPlayer() powers
        mainGraphics.setColor(Color.YELLOW);
        mainGraphics.fillRect(20, 40, window.getPlayer().getPower() * 8, 8);
        mainGraphics.setColor(Color.YELLOW.darker());

        mainGraphics.setStroke(new BasicStroke(2));
        for (int i = 0; i < window.getPlayer().getRequiredPower(); i++) {
            mainGraphics.drawRect(20 + 8 * i, 40, 8, 8);
        }

        mainGraphics.setStroke(new BasicStroke(1));


        // render window.getPlayer() scores
        mainGraphics.setColor(Color.WHITE);
        mainGraphics.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        mainGraphics.drawString("Score: " + window.getPlayer().getScore(), PANEL_WIDTH - 100, 30);

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
        window.clearEnemies();
        if (waveNumber == 1) {
            for (int i = 0; i < 4; i++) {
                window.addEnemy(new Enemy(1, 1));
            }
        }
        if (waveNumber == 2) {
            for (int i = 0; i < 8; i++) {
                window.addEnemy(new Enemy(1, 1));
            }
        }
        if (waveNumber == 3) {
            for (int i = 0; i < 4; i++) {
                window.addEnemy(new Enemy(1, 1));
            }
            window.addEnemy(new Enemy(1, 2));
            window.addEnemy(new Enemy(1, 2));
        }
        if (waveNumber == 4) {
            for (int i = 0; i < 4; i++) {
                window.addEnemy(new Enemy(2, 1));
            }
            window.addEnemy(new Enemy(1, 3));
            window.addEnemy(new Enemy(1, 4));
        }
        if (waveNumber == 5) {
            window.addEnemy(new Enemy(2, 3));
            window.addEnemy(new Enemy(1, 3));
            window.addEnemy(new Enemy(1, 4));
        }
        if (waveNumber == 6) {
            window.addEnemy(new Enemy(1, 3));
            for (int i = 0; i < 4; i++) {
                window.addEnemy(new Enemy(2, 1));
                window.addEnemy(new Enemy(3, 1));
            }
        }
        if (waveNumber == 7) {
            window.addEnemy(new Enemy(1, 3));
            window.addEnemy(new Enemy(2, 3));
            window.addEnemy(new Enemy(3, 3));
        }
        if (waveNumber == 8) {
            window.addEnemy(new Enemy(1, 4));
            window.addEnemy(new Enemy(2, 4));
            window.addEnemy(new Enemy(3, 4));
        }
        if (waveNumber == 9) {
            running = false;
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
        s = "Final score: " + window.getPlayer().getScore();
        length = (int) mainGraphics.getFontMetrics().getStringBounds(s, mainGraphics).getWidth();
        mainGraphics.drawString(s, (PANEL_WIDTH - length) / 2, PANEL_HEIGHT / 2 + 30);
        gameDraw();
    }

    private void updateElements(List<? extends Updatable> elements) {
        for (int i = 0; i < elements.size(); i++) {
            boolean remove = elements.get(i).update();
            if (remove) {
                elements.remove(i);
                i--;
            }
        }
    }

    private <F extends Contactable, S extends Contactable> void checkCollisions(List<F> firstElements, List<S> secondElements) {
//        final ArrayList<Contactable> firstElementsForResolve = new ArrayList<>();
//        final ArrayList<Contactable> secondElementsForResolve = new ArrayList<>();
        final Map<Contactable, Contactable> mapForResolve = new LinkedHashMap<>();
        for (final Contactable firstElement : firstElements) {
            final double bx = firstElement.getX();
            final double by = firstElement.getY();
            final double br = firstElement.getR();

            for (final Contactable secondElement : secondElements) {
                final double ex = secondElement.getX();
                final double ey = secondElement.getY();
                final double er = secondElement.getR();

                final double dx = bx - ex;
                final double dy = by - ey;
                final double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < br + er) {
                    mapForResolve.put(firstElement, secondElement);
//                    firstElementsForResolve.add(firstElement);
//                    secondElementsForResolve.add(secondElement);
//                    firstElement.resolveContact(secondElement);
//                    secondElement.resolveContact(firstElement);
                }
            }
        }
        mapForResolve.entrySet().forEach(entry -> {
            entry.getKey().resolveContact(entry.getValue());
            entry.getValue().resolveContact(entry.getKey());
        });
//        firstElementsForResolve.stream()
//                .forEach(element -> element.resolveContact(secondElementsForResolve.));
    }

    private void drawElements(List<? extends Drawable> elements) {
        elements.forEach(element -> element.draw(mainGraphics));
    }
}
