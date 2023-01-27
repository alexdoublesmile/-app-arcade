package com.joyful.arcade;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 400;

    private Thread thread;
    private boolean running;

    public GamePanel() {
        super();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();

    }

    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    @Override
    public void run() {

        running = true;

        while(running) {

            gameUpdate();
            gameRender();
            gameDraw();
        }
    }

    private void gameUpdate() {

    }

    private void gameRender() {

    }

    private void gameDraw() {
    }
}
