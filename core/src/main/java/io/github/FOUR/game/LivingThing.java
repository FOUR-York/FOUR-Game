package io.github.FOUR.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

/**
 * base class for anything that needs to have health or have AI, such as the player and enemies
 */
public abstract class LivingThing extends Entity {
    public boolean dead, hit;
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
        if (!dead) {
            hp -= damage;
            if (hp <= 0) {
                kill();
            }
        }
    };
}
