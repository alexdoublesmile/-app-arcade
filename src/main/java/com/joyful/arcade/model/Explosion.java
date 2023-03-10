package com.joyful.arcade.model;

import com.joyful.arcade.model.api.Drawable;
import com.joyful.arcade.model.api.Updatable;

import java.awt.*;

public class Explosion implements Updatable, Drawable {

    private double x;
    private double y;
    private int r;
    private int maxRadius;

    public Explosion(double x, double y, int r, int maxRadius) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.maxRadius = maxRadius;
    }

    @Override
    public boolean update() {
        r += 2;
        if (r > maxRadius) {
            return true;
        }
        return false;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(255, 255, 255, 128));
        g.setStroke(new BasicStroke(3));
        g.drawOval((int) (x - r), (int) (y - r), 2 * r, 2 * r);

        g.setStroke(new BasicStroke(1));
    }
}
