package com.joyful.arcade;

public class PowerUp {
    private double x;
    private double y;
    private int r;
    private  int type;

    public PowerUp(int type, double x, double y) {
        this.type = type;
        this.x = x;
        this.y = y;
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
}
