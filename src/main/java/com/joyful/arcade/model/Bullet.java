package com.joyful.arcade.model;
import java.awt.*;

import static com.joyful.arcade.util.WindowConstants.PANEL_HEIGHT;
import static com.joyful.arcade.util.WindowConstants.PANEL_WIDTH;

public class Bullet implements Updatable, Drawable, Contactable {
    private double x;
    private double y;
    private int r;
    private double speed;
    private double rad;
    private double dx;
    private double dy;
    private Color color1;

    private Window window;

    public Bullet(double angle, int x, int y) {
        this.x = x;
        this.y = y;
        r = 2;
        rad = Math.toRadians(angle); // for Math functions
        speed = 10;

        // angled_distance(casetus) = cos(angle) * default_distance(hypo)
        dx = Math.cos(rad) * speed;
        dy = Math.sin(rad) * speed;

        color1 = Color.YELLOW;
    }

    @Override
    public boolean update() {
        x += dx;
        y += dy;

        if (x < -r || x > PANEL_WIDTH + r
            || y < -r || y > PANEL_HEIGHT + r) {
            return true;
        }

        return false;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(color1);
        g.fillOval((int) x - r, (int) y - r, 2 * r, 2 * r);
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public int getR() {
        return r;
    }

    @Override
    public void resolveContact(Contactable with) {
        if (with instanceof Enemy) {
            window.removeBullet(this);
        }
    }

    public void setWindow(Window window) {
        this.window = window;
    }
}
