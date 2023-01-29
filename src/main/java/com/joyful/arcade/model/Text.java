package com.joyful.arcade.model;

import java.awt.*;

import static java.lang.System.nanoTime;

public class Text implements Updatable, Drawable {
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

    @Override
    public boolean update() {
        long elapsed = (nanoTime() - start) / 1000_000;
        if (elapsed > time) {
            return true;
        }
        return false;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setFont(new Font("Century Gothic", Font.PLAIN, 12));

        long elapsed = (nanoTime() - start) / 1000_000;
        int alpha = (int) (255 * Math.sin(3.14 * elapsed / time));
        if (alpha > 255) {
            alpha = 255;
        }
        g.setColor(new Color(255, 255, 255, alpha));
        final int length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();

        g.drawString(s, (int) (x - (length / 2)), (int) y);
    }
}
