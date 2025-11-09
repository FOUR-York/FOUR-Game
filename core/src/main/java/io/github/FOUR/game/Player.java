package io.github.FOUR.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Player extends LivingThing {
    private float stateTime = 0f, swingTime = 0f;
    private int mapHeight = (Main.mapS * Main.mapY);
    private boolean swinging = false, down = true, up = false, left = false, right = false;
    Animation<TextureRegion> walkUp, walkSide, walkDown, fall;
    TextureRegion[] walkUpFrames, walkSideFrames, walkDownFrames, fallFrames;

    public Player(float x, float y, float speed, int hp, int attack, Texture texture) {
        super(x, y, speed, hp, attack, texture);

        //Create texture region arrays for anims
        walkDownFrames = new TextureRegion[] {new TextureRegion(texture, 0, 0, 32, 32), new TextureRegion(texture, 32, 0, 32, 32),};
        walkSideFrames = new TextureRegion[] {new TextureRegion(texture, 64, 0, 32, 32), new TextureRegion(texture, 96, 0, 32, 32),};
        walkUpFrames = new TextureRegion[] {new TextureRegion(texture, 128, 0, 32, 32), new TextureRegion(texture, 160, 0, 32, 32),};

        fallFrames = new TextureRegion[] {new TextureRegion(texture, 0, 64, 32, 32), new TextureRegion(texture, 32, 64, 32, 32),};

        //Create anims with the arrays
        walkDown = new Animation<TextureRegion>(0.1f, walkDownFrames);
        walkSide = new Animation<TextureRegion>(0.1f, walkSideFrames);
        walkUp = new Animation<TextureRegion>(0.1f, walkUpFrames);

        fall = new Animation<TextureRegion>(0.1f, fallFrames);
    }

    public void move() {
        float delta = Gdx.graphics.getDeltaTime();
        stateTime += delta;

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            if (Main.mapW[(int) ((mapHeight-(y+16))/32)][(int) x/32] <= 0) {y += speed * delta;}

            TextureRegion frame = walkUp.getKeyFrame(stateTime, true);
            sprite.setRegion(frame);
            sprite.flip(false, false);

            up = true;
            down = false;
            right = false;
            left = false;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            if (Main.mapW[(int) ((mapHeight-(y-16))/32)][(int) x/32] <= 0) {y -= speed * delta;}

            TextureRegion frame = walkDown.getKeyFrame(stateTime, true);
            sprite.setRegion(frame);
            sprite.flip(false, false);

            down = true;
            up = false;
            right = false;
            left = false;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            if (Main.mapW[(int) ((mapHeight-y)/32)][(int) (x+16)/32] <= 0) {x += speed * delta;}

            TextureRegion frame = walkSide.getKeyFrame(stateTime, true);
            sprite.setRegion(frame);
            sprite.flip(false, false);

            right = true;
            up = false;
            left = false;
            down = false;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            if (Main.mapW[(int) ((mapHeight-y)/32)][(int) (x-16)/32] <= 0) {x -= speed * delta;}

            TextureRegion frame = walkSide.getKeyFrame(stateTime, true);
            sprite.setRegion(frame);
            sprite.flip(true, false);

            left = true;
            up = false;
            right = false;
            down = false;
        }

        sprite.setPosition(x-16, y-16);

        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            if (!swinging) {
                swinging = true;
                swingTime = 0;
            }
        }

        if (swinging) {
            if (down) {
                sprite.setRegion(new TextureRegion(texture, 0, 32, 32, 32));
                sprite.flip(false, false);
                swingTime += delta;
                if (swingTime >= 0.5f) {
                    sprite.setRegion(new TextureRegion(texture, 0, 0, 32, 32));
                    sprite.flip(false, false);
                    swinging = false;
                }
            }
            else if (right) {
                sprite.setRegion(new TextureRegion(texture, 32, 32, 32, 32));
                sprite.flip(false, false);
                swingTime += delta;
                if (swingTime >= 0.5f) {
                    sprite.setRegion(new TextureRegion(texture, 64, 0, 32, 32));
                    sprite.flip(false, false);
                    swinging = false;
                }
            }
            else if (left) {
                sprite.setRegion(new TextureRegion(texture, 32, 32, 32, 32));
                sprite.flip(true, false);
                swingTime += delta;
                if (swingTime >= 0.5f) {
                    sprite.setRegion(new TextureRegion(texture, 64, 0, 32, 32));
                    sprite.flip(true, false);
                    swinging = false;
                }
            }
            else if (up) {
                sprite.setRegion(new TextureRegion(texture, 64, 32, 32, 32));
                sprite.flip(false, false);
                swingTime += delta;
                if (swingTime >= 0.5f) {
                    sprite.setRegion(new TextureRegion(texture, 128, 0, 32, 32));
                    sprite.flip(false, false);
                    swinging = false;
                }
            }
        }
    }

    public void collision() {
        if (Main.mapW[(int) ((mapHeight-y)/32)-1][(int) x/32] == -2) {
            //run some code
            //mapW can be replaced with mapE if needed, and == -2 can be any number <=0
        }
    }

    public void kill() {}

    public void drawFOV(ShapeDrawer drawer, int fov) {
        for (int r = 0; r < fov; r++) {
            float maxDist = 640;

            float angle = (float) Math.toRadians(r);
            float[] startPos = rayToNearestWall(angle, maxDist);
            float dist = (float) Math.sqrt(Math.pow(startPos[0] - x, 2) + Math.pow(startPos[1] - y, 2));

            if (dist <= maxDist) {
                float[] endPos = new float[] {startPos[0] + ((float) (Math.cos(angle) * maxDist)), startPos[1] + ((float) (Math.sin(angle) * maxDist))};

                drawer.setColor(0f, 0f, 0f, 1f);
                drawer.line(startPos[0], startPos[1], endPos[0], endPos[1], 7f);
            }
        }
    }

    private float[] rayToNearestWall(float angle, float maxDist) {
        float rx = x;
        float ry = y;

        for (int i = 0; i <= maxDist/4; i++) {
            rx += (float) (Math.cos(angle) * 4);
            ry += (float) (Math.sin(angle) * 4);

            if (Main.mapW[(int) ((mapHeight - (ry)) / 32)][(int) rx / 32] == 1) {
                return new float[] { rx, ry };
            }
        }
        return new float[] {9999999, 9999999};
    }
}
