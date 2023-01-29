package com.joyful.arcade.model;


import java.awt.*;

import static com.joyful.arcade.util.WindowConstants.PANEL_HEIGHT;
import static com.joyful.arcade.util.WindowConstants.PANEL_WIDTH;

public class Bullet {
    private double x;
    private double y;
    private int r;
    private double speed;
    private double rad;
    private double dx;
    private double dy;
    private Color color1;

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

    public boolean update() {
        x += dx;
        y += dy;

        if (x < -r || x > PANEL_WIDTH + r
            || y < -r || y > PANEL_HEIGHT + r) {
            return true;
        }

        return false;
    }

    public void draw(Graphics2D g) {
        g.setColor(color1);
        g.fillOval((int) x - r, (int) y - r, 2 * r, 2 * r);
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
}
