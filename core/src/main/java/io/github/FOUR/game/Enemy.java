package io.github.FOUR.game;

import com.badlogic.gdx.graphics.Texture;

public class Enemy extends LivingThing {
    public Enemy(float x, float y, float speed, int hp, int attack, Texture texture) {
        super(x, y, speed, hp, attack, texture);
    }

    public void move() {}

    public void kill() {}
}
