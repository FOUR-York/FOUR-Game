package io.github.FOUR.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class Entity {
    public float x, y;

    public Texture texture;
    public Sprite sprite;

    public Entity(float x, float y, Texture texture) {
        this.x = x;
        this.y = y;

        this.texture = texture;
        sprite = new Sprite(this.texture, 0, 0, 32, 32);
        sprite.setPosition(x, y);
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public abstract void kill();
}
