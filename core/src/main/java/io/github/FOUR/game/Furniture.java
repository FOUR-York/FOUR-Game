package io.github.FOUR.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Furniture {
    float x, y;
    Texture texture;
    int mapS;
    int dir;
    public Furniture(Texture texture, int dir, int mapS, float x, float y)
    {
        this.texture = texture;
        this.dir = dir;
        this.mapS = mapS;
        this.x = x;
        this.y = y;
    }

    public void draw(Batch batch) {
        batch.draw(texture, x*mapS, y*mapS, mapS/2, mapS/2, mapS, mapS, 1, 1, 90f*(dir), 0, 0, 32, 32, false, false);
    }
}
