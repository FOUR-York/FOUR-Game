package io.github.FOUR.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class Enemy extends LivingThing {
    public float range;
    public boolean patrolling = false, chasing = false, swinging = false;

    private float targetX, targetY;
    private int mapHeight = (Main.mapS * Main.mapY);

    public Enemy(float x, float y, float speed, int hp, int attack, float range, Texture texture) {
        super(x, y, speed, hp, attack, texture);
        this.range = range;
    }

    public void move() {
        if (isPlayerInSight(Main.player)) {
            //if player in los, chase
            chase(Main.player);
        }
        else {
            //if not, patrol
            patrol(range);
        }
        sprite.setPosition(x-16, y-16);
    }

    public void kill() {}

    private boolean isPlayerInSight(Player player) {
        float angle = (float) Math.atan2(player.y - y, player.x - x);
        float dist = (float) Math.sqrt(Math.pow(player.x - x, 2) + Math.pow(player.y - y, 2));
        float rx = x;
        float ry = y;

        for (int i = 0; i <= dist/4; i++) {
            rx += (float) (Math.cos(angle) * 4);
            ry += (float) (Math.sin(angle) * 4);

            if (Main.mapW[(int) ((mapHeight - (ry)) / 32)][(int) rx / 32] > 0) {
                return false;
            }
        }
        return true;
    }

    private void chase(Player player) {
        chasing = true;

        float delta = Gdx.graphics.getDeltaTime();
        int mapHeight = (Main.mapS * Main.mapY);

        float angle = (float) Math.atan2(player.y - y, player.x - x);
        float dist = (float) Math.sqrt(Math.pow(player.x - x, 2) + Math.pow(player.y - y, 2));

        if (dist < 8) {
            swing(player);
        }
        else {
            float dx = (float) (Math.cos(angle) * speed * delta);
            float dy = (float) (Math.sin(angle) * speed * delta);

            if (Main.mapW[(int) ((mapHeight-y-dy)/32)][(int) (x+dx)/32] <= 0) {
                x += dx;
                y += dy;
            }
        }

        chasing = false;
    }

    private void swing(Player player) {
        swinging = true;

        swinging = false;
    }

    private void patrol(float range) {
        float delta = Gdx.graphics.getDeltaTime();

        if (!patrolling) {
            patrolling = true;

            float minX = x - range;
            float maxX = x + range;
            float minY = y - range;
            float maxY = y + range;

            targetX = minX + (float) Math.random() * (maxX - minX);
            targetY = minY + (float) Math.random() * (maxY - minY);
        }

        float angle = (float) Math.atan2(targetY - y, targetX - x);

        float dx = (float) (Math.cos(angle) * speed * delta);
        float dy = (float) (Math.sin(angle) * speed * delta);

        if (Main.mapW[(int) ((mapHeight-y-dy)/32)][(int) (x+dx)/32] <= 0) {
            x += dx;
            y += dy;
        }
        else {
            patrolling = false;
        }

        if (((float) Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2))) < 5) {
            patrolling = false;
        }
    }
}
