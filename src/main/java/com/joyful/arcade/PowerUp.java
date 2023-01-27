package com.joyful.arcade;

import java.awt.*;

public class PowerUp {
    private double x;
    private double y;
    private int r;
    private  int type;
    private Color color1;

    // 1 -- +1 life
    // 2 -- +1 power
    // 3 -- +1 power

    public PowerUp(int type, double x, double y) {
        this.type = type;
        this.x = x;
        this.y = y;

        if (type == 1) {
            color1 = Color.PINK;
        }
        if (type == 2 || type == 3) {
            color1 = Color.YELLOW;
        }
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

    public int getType() {
        return type;
    }

    public boolean update() {
        y += 2;

        if (y > GamePanel.HEIGHT + r) {
            return true;
        }

        return false;

    }

    public void draw(Graphics2D g) {


    }
}
