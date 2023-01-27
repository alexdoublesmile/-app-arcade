package com.joyful.arcade;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 400;

    public GamePanel() {
        super();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

    }
}
