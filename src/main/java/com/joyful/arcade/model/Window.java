package com.joyful.arcade.model;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.nanoTime;

public class Window {

    private Player player;
    private List<Bullet> bullets;
    private List<Enemy> enemies;
    private List<Explosion> explosions = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();
    private List<Text> texts = new ArrayList<>();



    private long slowDownTimer;
    private long slowDownTimerDiff;
    private int slowDownLength = 6000;

    public Window() {
        this.player = new Player();
        player.setWindow(this);
        this.bullets = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.explosions = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.texts = new ArrayList<>();
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

    public void addExplosion(Explosion explosion) {
        explosions.add(explosion);
        explosion.setWindow(this);
    }

    public void removeExplosion(Explosion explosion) {
        explosions.remove(explosion);
        explosion.setWindow(null);
    }

    public List<Explosion> getExplosions() {
        return new ArrayList<>(explosions);
    }

    public void addPowerUp(PowerUp powerUp) {
        powerUps.add(powerUp);
        powerUp.setWindow(this);
    }

    public void removePowerUp(PowerUp powerUp) {
        powerUps.remove(powerUp);
        powerUp.setWindow(null);
    }

    public List<PowerUp> getPowerUps() {
        return new ArrayList<>(powerUps);
    }

    public void addText(Text text) {
        texts.add(text);
        text.setWindow(this);
    }

    public void removeText(Text text) {
        texts.remove(text);
        text.setWindow(null);
    }

    public List<Text> getTexts() {
        return new ArrayList<>(texts);
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

    public void clearEnemies() {
        enemies.clear();
    }

    public Player getPlayer() {
        return player;
    }

    public void startSlowDown() {
        slowDownTimer = nanoTime();
        enemies.forEach(Enemy::setSlowSpeed);
    }

    public void checkSlowDown() {
        if (slowDownTimer > 0) {
            slowDownTimerDiff = (nanoTime() - slowDownTimer) / 1000_000;
            if (slowDownTimerDiff > slowDownLength) {
                slowDownTimer = 0;

                enemies.forEach(Enemy::setNormSpeed);
            }
        }
    }

    public boolean isSlowDown() {
        return slowDownTimer != 0;
    }

    public long getSlowDownTimer() {
        return slowDownTimer;
    }

    public long getSlowDownTimerDiff() {
        return slowDownTimerDiff;
    }

    public int getSlowDownLength() {
        return slowDownLength;
    }

    public void removePlayer() {

    }
}
