package com.joyful.arcade.listener;

import com.joyful.arcade.model.Player;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static java.awt.event.KeyEvent.*;

public class KeyboardListener extends KeyAdapter {
    private final Player player;

    public KeyboardListener(Player player) {
        this.player = player;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        final int keyCode = e.getKeyCode();
        if (keyCode == VK_LEFT) {
            player.setLeft(true);
        }
        if (keyCode == VK_RIGHT) {
            player.setRight(true);
        }
        if (keyCode == VK_UP) {
            player.setUp(true);
        }
        if (keyCode == VK_DOWN) {
            player.setDown(true);
        }
        if (keyCode == VK_Z) {
            player.setFiring(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        final int keyCode = e.getKeyCode();
        if (keyCode == VK_LEFT) {
            player.setLeft(false);
        }
        if (keyCode == VK_RIGHT) {
            player.setRight(false);
        }
        if (keyCode == VK_UP) {
            player.setUp(false);
        }
        if (keyCode == VK_DOWN) {
            player.setDown(false);
        }
        if (keyCode == VK_Z) {
            player.setFiring(false);
        }
    }
}
