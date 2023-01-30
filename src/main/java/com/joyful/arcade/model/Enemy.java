package com.joyful.arcade.model;

import com.joyful.arcade.process.GamePanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.joyful.arcade.util.WindowConstants.PANEL_HEIGHT;
import static com.joyful.arcade.util.WindowConstants.PANEL_WIDTH;
import static java.lang.System.nanoTime;

public class Enemy implements Updatable, Drawable, Contactable {
    private double x;
    private double y;
    private int r;

    private double dx;
    private double dy;
    private double rad;
    private double speed;

    private int health;
    private int type;
    private int rank;

    private Color color1;

    private boolean hit;
    private long hitTimer;

    private boolean ready;
    private boolean dead;

    private boolean slow;

    private Window window;

    public Enemy(int type, int rank) {

        this.type = type;
        this.rank = rank;

        // default enemy
        if (type == 1) {
            color1 = new Color(0, 0, 255, 128);
            if (rank == 1) {
                speed = 2;
                r = 5;
                health = 1;
            }
            if (rank == 2) {
                speed = 2;
                r = 10;
                health = 2;
            }
            if (rank == 3) {
                speed = 1.5;
                r = 20;
                health = 3;
            }
            if (rank == 4) {
                speed = 1.5;
                r = 30;
                health = 4;
            }
        }
        // faster & stronger enemy
        if (type == 2) {
            color1 = new Color(255, 0, 0, 128);
            if (rank == 1) {
                speed = 3;
                r = 5;
                health = 2;
            }
            if (rank == 2) {
                speed = 3;
                r = 10;
                health = 3;
            }
            if (rank == 3) {
                speed = 2.5;
                r = 20;
                health = 3;
            }
            if (rank == 4) {
                speed = 2.5;
                r = 30;
                health = 4;
            }
        }
        // slow but extra hard enemy
        if (type == 3) {
            color1 = new Color(0, 255, 0, 128);
            if (rank == 1) {
                speed = 1.5;
                r = 5;
                health = 3;
            }
            if (rank == 2) {
                speed = 1.5;
                r = 10;
                health = 4;
            }
            if (rank == 3) {
                speed = 1.5;
                r = 25;
                health = 5;
            }
            if (rank == 4) {
                speed = 1.5;
                r = 45;
                health = 5;
            }
        }

        // y is null & x is random in left screen half + quater (so left & right quarters are empty)
        x = Math.random() * PANEL_WIDTH / 2 + PANEL_WIDTH / 4;
        y = -r;

        // 20-160 degrees (so they all move to down by different angles)
        double angle = Math.random() * 140 + 20;
        rad = Math.toRadians(angle);

        dx = Math.cos(rad) * speed;
        dy = Math.sin(rad) * speed;

        ready = false;
        dead = false;

    }

    public void hit() {
        health--;
        if (health <= 0) {
            dead = true;
        }
        hit = true;
        hitTimer = nanoTime();
    }

    public List<Enemy> explode() {
        final ArrayList<Enemy> enemiesForAdd = new ArrayList<>();
        if (rank > 1) {
            int amount = 0;
            if (type == 1) {
                amount = 3;
            }
            if (type == 2) {
                amount = 3;
            }
            if (type == 3) {
                amount = 4;
            }
            for (int i = 0; i < amount; i++) {
                final Enemy enemy = new Enemy(type, rank - 1);
                enemy.setSlow(slow);
                enemy.x = x;
                enemy.y = y;
                // ?
                double angle = 0;
                if (!ready) {
                    angle = Math.random() * 140 + 20;
                } else {
                    angle = Math.random() * 360;
                }
                enemy.rad = Math.toRadians(angle);
                enemiesForAdd.add(enemy);
            }
        }
        return enemiesForAdd;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getR() {
        return r;
    }

    public boolean isDead() {
        return dead;
    }

    @Override
    public boolean update() {
        if (slow) {
            x += dx * 0.3;
            y += dy * 0.3;
        } else {
            x += dx;
            y += dy;
        }

        if (!ready) {
            if (x > r && x < PANEL_WIDTH - r
                    && y > r && y < PANEL_HEIGHT - r) {
                ready = true;
            }
        }

        if (x < r && dx < 0) dx = -dx;
        if (y < r && dy < 0) dy = -dy;
        if (x > PANEL_WIDTH - r && dx > 0) dx = -dx;
        if (y > PANEL_HEIGHT - r && dy > 0) dy = -dy;



        if (hit) {
            long elapsed = (nanoTime() - hitTimer) / 1000_000;
            if (elapsed > 50) {
                hit = false;
                hitTimer = 0;
            }
        }
        return false;
    }

    @Override
    public void draw(Graphics2D g) {
        if (hit) {

            g.setColor(Color.WHITE);
            g.fillOval((int) x - r, (int) y - r, 2 * r, 2 * r);

            g.setStroke(new BasicStroke(3));
            g.setColor(Color.WHITE.darker());
            g.drawOval((int) x - r, (int) y - r, 2 * r, 2 * r);

            g.setStroke(new BasicStroke(1));
        } else {

            g.setColor(color1);
            g.fillOval((int) x - r, (int) y - r, 2 * r, 2 * r);

            g.setStroke(new BasicStroke(3));
            g.setColor(color1.darker());
            g.drawOval((int) x - r, (int) y - r, 2 * r, 2 * r);

            g.setStroke(new BasicStroke(1));
        }
    }

    public int getType() {
        return type;
    }

    public int getRank() {
        return rank;
    }

    public boolean isHit() {
        return hit;
    }

    public long getHitTimer() {
        return hitTimer;
    }

    public boolean isSlow() {
        return slow;
    }

    public void setSlow(boolean slow) {
        this.slow = slow;
    }

    @Override
    public void resolveContact(Contactable with) {
        if (with instanceof Bullet) {
            hit();
        }
    }

    public void setWindow(Window window) {
        this.window = window;
    }

//    public void remove() {
//        window.removeEnemy(this);
//        window = null;
//    }
}
