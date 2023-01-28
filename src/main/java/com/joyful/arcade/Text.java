package com.joyful.arcade;

import java.awt.*;

import static java.lang.System.nanoTime;

public class Text {
    private double x;
    private double y;
    private long time;
    private String s;

    private long start;

    public Text(double x, double y, long time, String s) {
        this.x = x;
        this.y = y;
        this.time = time;
        this.s = s;
        start = nanoTime();
    }

    public boolean update() {
        long elapsed = (nanoTime() - start) / 1000_000;
        if (elapsed > time) {
            return true;
        }
        return false;
    }

    public void draw(Graphics2D g) {
        g.setFont(new Font("Century Gothic", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        g.drawString(s, (int) x, (int) y);
    }
}
