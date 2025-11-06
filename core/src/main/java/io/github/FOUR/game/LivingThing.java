package io.github.FOUR.game;

import com.badlogic.gdx.graphics.Texture;

public abstract class LivingThing extends Entity {
    public int hp, attack;
    public float speed;

    public LivingThing(float x, float y, float speed, int hp, int attack, Texture texture) {
        super(x, y, texture);
        this.hp = hp;
        this.attack = attack;
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
