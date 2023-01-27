package com.joyful.arcade;

import java.awt.*;

import static java.lang.System.nanoTime;

public class Player {
    private int x;
    private int y;
    private int r;
    private int dx;
    private int dy;
    private int speed;
    private int lives;
    private boolean left;
    private boolean right;
    private boolean up;
    private boolean down;
    private boolean firing;
    private long firingTimer;
    private long firingDelay;
    private Color color1;
    private Color color2;

    public Player() {
        x = GamePanel.WIDTH / 2;
        y = GamePanel.HEIGHT / 2;
        r = 5;

        dx = 0;
        dy = 0;
        speed = 5;

        lives = 3;

        color1 = Color.WHITE;
        color2 = Color.RED;

        firingTimer = nanoTime();
        firingDelay = 200; // 5 shots per second

    }

    public void update() {
        if (left) {
            dx = -speed;
        }
        if (right) {
            dx = speed;
        }
        if (up) {
            dy = -speed;
        }
        if (down) {
            dy = speed;
        }

        x += dx;
        y += dy;

        if (x < r) {
            x = r;
        }
        if (y < r) {
            y = r;
        }

        if (x > GamePanel.WIDTH - r) {
            x = GamePanel.WIDTH - r;
        }
        if (y > GamePanel.HEIGHT - r) {
            y = GamePanel.HEIGHT - r;
        }

        dx = 0;
        dy = 0;

        if (firing) {
            final long elapsed = (nanoTime() - firingTimer) / 1000_000;
            if (elapsed > firingDelay) {
                GamePanel.bullets.add(new Bullet(270, x, y));
                firingTimer = nanoTime();
            }
        }
    }

    public void draw(Graphics2D g) {
        g.setColor(color1);
        g.fillOval(x - r, y - r, 2 * r, 2 * r);

        g.setStroke(new BasicStroke(3));
        g.setColor(color1.darker());
        g.drawOval(x - r, y - r, 2 * r, 2 * r);

        g.setStroke(new BasicStroke(1));
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    public void setFiring(boolean firing) {
        this.firing = firing;
    }
}
