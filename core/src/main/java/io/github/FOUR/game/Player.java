package io.github.FOUR.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player extends LivingThing {
    private float stateTime = 0f;
    Animation<TextureRegion> walkUp, walkSide, walkDown, swing, fall;
    TextureRegion[] walkUpFrames, walkSideFrames, walkDownFrames, swingFrames, fallFrames;

    public Player(float x, float y, float speed, int hp, Texture texture) {
        super(x, y, speed, hp, texture);

        //Create texture region arrays for anims
        walkDownFrames = new TextureRegion[] {new TextureRegion(texture, 0, 0, 32, 32), new TextureRegion(texture, 32, 0, 32, 32),};
        walkSideFrames = new TextureRegion[] {new TextureRegion(texture, 64, 0, 32, 32), new TextureRegion(texture, 96, 0, 32, 32),};
        walkUpFrames = new TextureRegion[] {new TextureRegion(texture, 128, 0, 32, 32), new TextureRegion(texture, 160, 0, 32, 32),};

        //Create anims with the arrays
        walkDown = new Animation<TextureRegion>(0.1f, walkDownFrames);
        walkSide = new Animation<TextureRegion>(0.1f, walkSideFrames);
        walkUp = new Animation<TextureRegion>(0.1f, walkUpFrames);
    }

    public void move() {
        float delta = Gdx.graphics.getDeltaTime();
        stateTime += delta;
        int mapHeight = (Main.mapS * Main.mapY);

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            if (Main.map[(int) ((mapHeight-(y+16))/32)-1][(int) x/32] <= 0) {y += speed * delta;}

            TextureRegion frame = walkUp.getKeyFrame(stateTime, true);
            super.
            sprite.flip(false, false);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            if (Main.map[(int) ((mapHeight-(y-16))/32)-1][(int) x/32] <= 0) {y -= speed * delta;}

            TextureRegion frame = walkDown.getKeyFrame(stateTime, true);
            sprite.setRegion(frame);
            sprite.flip(false, false);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            if (Main.map[(int) ((mapHeight-y)/32)-1][(int) (x+16)/32] <= 0) {x += speed * delta;}

            TextureRegion frame = walkSide.getKeyFrame(stateTime, true);
            sprite.setRegion(frame);
            sprite.flip(false, false);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            if (Main.map[(int) ((mapHeight-y)/32)-1][(int) (x-16)/32] <= 0) {x -= speed * delta;}

            TextureRegion frame = walkSide.getKeyFrame(stateTime, true);
            sprite.setRegion(frame);
            sprite.flip(true, false);
        }

        sprite.setPosition(x-16, y+16);
    }

    public void kill() {}
}
