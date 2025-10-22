package io.github.FOUR.game;

import com.badlogic.gdx.math.Vector2;

// entity base class. mostly abstract.
public abstract class Entity {
    private Vector2 position;
    // optional cleanup
    public abstract void kill();
    public abstract void hit();
    public Vector2 getPosition() {
        return position;
    }
    public abstract void step(float delta);
}
