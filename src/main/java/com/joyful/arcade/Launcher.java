package com.joyful.arcade;

import javax.swing.*;

public class Launcher {
    public static void main(String[] args) {
        final JFrame window = new JFrame("First game");
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        window.setContentPane(new GamePanel());

        window.pack();
        window.setVisible(true);
    }
}
