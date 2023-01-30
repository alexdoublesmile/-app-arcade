package com.joyful.arcade.model;

import java.util.ArrayList;
import java.util.List;

public class Window {

    private Player player;
    private List<Bullet> bullets;
    private List<Enemy> enemies;

    public Window() {
        this.player = new Player();
        player.setWindow(this);
        this.bullets = new ArrayList<>();
        this.enemies = new ArrayList<>();
    }

    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
        bullet.setWindow(this);
    }

    public void removeBullet(Bullet bullet) {
        bullets.remove(bullet);
        bullet.setWindow(null);
    }

    public List<Bullet> getBullets() {
        return new ArrayList<>(bullets);
    }

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
        enemy.setWindow(this);
    }

    public void removeEnemy(Enemy enemy) {
        enemies.remove(enemy);
        enemy.setWindow(null);
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public Player getPlayer() {
        return player;
    }

    public void clearEnemies() {
        enemies.clear();
    }
}
