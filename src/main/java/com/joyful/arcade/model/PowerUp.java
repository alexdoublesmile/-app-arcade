package com.joyful.arcade.model;

import com.joyful.arcade.model.api.Drawable;
import com.joyful.arcade.model.api.Updatable;

import java.awt.*;

import static com.joyful.arcade.util.WindowConstants.PANEL_HEIGHT;

public class PowerUp implements Updatable, Drawable {
    private double x;
    private double y;
    private int r;
    private  int type;
    private Color color1;

    // 1 -- +1 life
    // 2 -- +1 power
    // 3 -- +1 power
    // 4 -- slow the time

    public PowerUp(int type, double x, double y) {
        this.type = type;
        this.x = x;
        this.y = y;

        if (type == 1) {
            color1 = Color.PINK;
            r = 3;
        }
        if (type == 2) {
            color1 = Color.YELLOW;
            r = 3;
        }
        if (type == 3) {
            color1 = Color.YELLOW;
            r = 5;
        }
        if (type == 4) {
            color1 = Color.WHITE;
            r = 3;
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

    @Override
    public boolean update() {
        y += 2;
        if (y > PANEL_HEIGHT + r) {
            return true;
        }
        return false;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(color1);
        g.fillRect((int) (x - r), (int) (y - r), 2 * r, 2 * r);

        g.setStroke(new BasicStroke(3));
        g.setColor(color1.darker());
        g.drawRect((int) (x - r), (int) (y - r), 2 * r, 2 * r);

        g.setStroke(new BasicStroke(1));
    }
}
