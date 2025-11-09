package io.github.FOUR.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

public abstract class LivingThing extends Entity {
    public boolean dead, hit;
    public int hp, attack;
    public float speed, effectTime;

    public LivingThing(float x, float y, float speed, int hp, int attack, Texture texture) {
        super(x, y, texture);
        this.hp = hp;
        this.attack = attack;
        this.speed = speed;
    }

    public abstract void move();

    public void damage(int damage) {
        if (!dead) {
            hp -= damage;
            effectTime = 0f;
            if (hp <= 0) {
                kill();
            }
        }
    };
}
