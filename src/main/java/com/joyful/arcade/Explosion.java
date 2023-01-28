package com.joyful.arcade;

import java.awt.*;

public class Explosion {

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

    public boolean update() {
        r++;
        if (r > maxRadius) {
            return true;
        }
        return false;
    }

    public void draw(Graphics2D g) {

    }
}
