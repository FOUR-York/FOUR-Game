package io.github.FOUR.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class LivingThing extends Entity {
    public int hp;
    public float speed;

    public LivingThing(float x, float y, float speed, int hp, Texture texture) {
        super(x, y, texture);
        this.hp = hp;
        this.speed = speed;
    }

    public abstract void move();

    public void damage(int damage) {
        hp -= damage;
        if (hp <= 0) {
            kill();
        }
    };
}
