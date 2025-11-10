package io.github.FOUR.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * the player class is used to handle mostly everything to do with the player.
 * movement, hp, attacking, drawing the fov effect, and collision.
 */
public class Player extends LivingThing {
    //timers
    private float stateTime = 0f, swingTime = 0f, downTime = 0f, collisionTime = 0f, hitTime = 0f;

    private int mapHeight = (Main.mapS * Main.mapY);

    //states
    private boolean swinging = false, down = true, up = false, left = false, right = false, hitSuccess = false, beingHit = false;

    private Animation<TextureRegion> walkUp, walkSide, walkDown;
    private TextureRegion[] walkUpFrames, walkSideFrames, walkDownFrames, fallFrames;

    /**
     * the constructor for the player class
     *
     * @param x initial x pos
     * @param y initial y pos
     * @param speed movement speed
     * @param hp starting hp, can't be above 100
     * @param attack damage per hit
     * @param texture player sprite sheet
     */
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
    }

    /**
     * Handles all the visual stuff for the player,
     * as well as movement and attacking logic.
     */
    public void move() {
        float delta = Gdx.graphics.getDeltaTime();
        stateTime += delta;

        if (!dead) {
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                // collision with walls
                if (Main.mapW[(int) ((mapHeight-(y+16))/32)][(int) x/32] <= 0) {y += speed * delta;}

                //animation
                TextureRegion frame = walkUp.getKeyFrame(stateTime, true);
                sprite.setRegion(frame);
                sprite.flip(false, false);

                //state
                up = true;
                down = false;
                right = false;
                left = false;

                //the other three directions are the same pretty much
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

            //sets the sprites position and offsets it to be centered
            sprite.setPosition(x-16, y-16);

            //if the z key is just pressed, start attacking
            if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
                if (!swinging) {
                    swinging = true;
                    swingTime = 0;
                }
            }

            if (swinging) {
                Enemy nearest = findNearestEnemy();
                float enemyDist = 9999999;
                if (nearest != null) {
                    enemyDist = (float) Math.sqrt(Math.pow(nearest.x - x, 2) + Math.pow(nearest.y - y, 2));
                }

                if (down) {
                    //first frame of anim
                    sprite.setRegion(new TextureRegion(texture, 0, 32, 32, 32));
                    sprite.flip(false, false);

                    swingTime += delta;

                    if (enemyDist < 48 && !hitSuccess) {
                        nearest.damage(attack);
                        hitSuccess = true;
                    }

                    if (swingTime >= 0.5f) {
                        //second frame of anim
                        sprite.setRegion(new TextureRegion(texture, 0, 0, 32, 32));
                        sprite.flip(false, false);

                        swinging = false;
                        hitSuccess = false;
                    }

                    //again, the other three directions are pretty much the same
                }
                else if (right) {
                    sprite.setRegion(new TextureRegion(texture, 32, 32, 32, 32));
                    sprite.flip(false, false);

                    swingTime += delta;

                    if (enemyDist < 48 && !hitSuccess) {
                        nearest.damage(attack);
                        hitSuccess = true;
                    }

                    if (swingTime >= 0.5f) {
                        sprite.setRegion(new TextureRegion(texture, 64, 0, 32, 32));
                        sprite.flip(false, false);

                        swinging = false;
                        hitSuccess = false;
                    }
                }
                else if (left) {
                    sprite.setRegion(new TextureRegion(texture, 32, 32, 32, 32));
                    sprite.flip(true, false);

                    swingTime += delta;

                    if (enemyDist < 48 && !hitSuccess) {
                        nearest.damage(attack);
                        hitSuccess = true;
                    }

                    if (swingTime >= 0.5f) {
                        sprite.setRegion(new TextureRegion(texture, 64, 0, 32, 32));
                        sprite.flip(true, false);

                        swinging = false;
                        hitSuccess = false;
                    }
                }
                else if (up) {
                    sprite.setRegion(new TextureRegion(texture, 64, 32, 32, 32));
                    sprite.flip(false, false);

                    swingTime += delta;

                    if (enemyDist < 48 && !hitSuccess) {
                        nearest.damage(attack);
                        hitSuccess = true;
                    }

                    if (swingTime >= 0.5f) {
                        sprite.setRegion(new TextureRegion(texture, 128, 0, 32, 32));
                        sprite.flip(false, false);

                        swinging = false;
                        hitSuccess = false;
                    }
                }
            }
        }
        else {
            //if downed set all states to false except down
            swinging = false;
            up = false;
            down = true;
            right = false;
            left = false;

            downTime += delta;

            if (downTime <= 0.5f) {
                //first frame of anim
                sprite.setRegion(fallFrames[0]);
                sprite.flip(false, false);
            }
            else {
                //second frame of anim
                sprite.setRegion(fallFrames[1]);
                sprite.flip(false, false);
            }
            if (hp >= 20) {
                //get back up when health reaches 10
                stateTime = 0f;
                dead = false;

                Main.awakeSound.play();

                sprite.setRegion(walkDownFrames[0]);
                sprite.flip(false, false);
            }
        }

        //hit effect
        if (beingHit) {
            hitTime += delta;
            sprite.setColor(Color.RED);
            if (hitTime > 0.5f) {
                beingHit = false;
            }
        }
        else {
            sprite.setColor(Color.WHITE);
        }

        if (stateTime > 300f) {
            stateTime = 0f;
        }
    }

    /**
     * This method is mainly used for special tile collision detection,
     * but it also has a 1 second clock in it so I used it for HP regen because
     * otherwise I would have to make a whole new method.
     */
    public void collision() {
        float delta = Gdx.graphics.getDeltaTime();
        collisionTime += delta;

        switch (Main.mapW[(int) ((mapHeight - (y)) / 32)][(int) x / 32]) {
            case -2:
                Main.win();
                break;
            case -3:
                if (collisionTime >= 1f) {
                    damage(5);
                }
                break;
            case -5:
                Main.changeZoneFromTile(Main.mapF[(int) ((mapHeight - (y)) / 32)][(int) x / 32]);
                break;
        }

        if (collisionTime > 1f) {
            if (hp < 100 && dead) {
                hp += 2;
            }

            Main.time--;
            if (Main.time <= 0) {
                Main.lose();
            }

            collisionTime = 0f;
        }
    }

    /**
     * if not already dead (downed) then die (become downed) also reset the down state timer.
     */
    public void kill() {
        if (!dead) {
            Main.fallSound.play();
            dead = true;
            downTime = 0f;
        }
    }

    /**
     * uses the rayToNearestWall method to draw lines extending from the nearest wall
     * to create an effect which in practice allows you to not see through walls
     *
     * @param drawer the ShapeDrawer. should be defined in main.
     * @param fov the fov of the player (so the amount of rays)
     */
    public void drawFOV(ShapeDrawer drawer, int fov) {
        int rays = 360*2;
        for (int r = 0; r < rays; r++) {
            float maxDist = 640;

            float angle = (float) Math.toRadians((float)r*((float)fov/(float)rays));
            float[] startPos = rayToNearestWall(angle, maxDist);
            float dist = (float) Math.sqrt(Math.pow(startPos[0] - x, 2) + Math.pow(startPos[1] - y, 2));

            if (dist <= maxDist) {
                float tDist = 10f;
                float delta = 1f;
                float lineWidth = 3.5f;
                float theta = (float) Math.atan((lineWidth/2f)/(float)tDist);
                // triangle
                float[] endPos = new float[] {startPos[0] + ((float) (Math.cos(angle+theta) * tDist)), startPos[1] + ((float) (Math.sin(angle+theta) * tDist))};
                float[] endPos2 = new float[] {startPos[0] + ((float) (Math.cos(angle-theta) * tDist)), startPos[1] + ((float) (Math.sin(angle-theta) * tDist))};
                // line
                float[] startPos2 = new float[] {startPos[0] + ((float) (Math.cos(angle) * (tDist-delta))), startPos[1] + ((float) (Math.sin(angle) * (tDist-delta)))};
                float[] endPos3 = new float[] {startPos[0] + ((float) (Math.cos(angle) * maxDist)), startPos[1] + ((float) (Math.sin(angle) * maxDist))};

                drawer.setColor(0f, 0f, 0f, 1f);
                drawer.filledTriangle(startPos[0], startPos[1], endPos[0], endPos[1], endPos2[0], endPos2[1]);
                drawer.line(startPos2[0], startPos2[1], endPos3[0], endPos3[1], lineWidth);
            }
        }
    }

    /**
     * finds the nearest wall from a line starting at x, y with an angle provided.
     * uses steps of 4 but this can be changed to be more or less precise
     * returns a very big number if there is no wall in a distance of maxDist
     *
     * @param angle the angle of the ray
     * @param maxDist the maximum distance the ray will search
     *
     * @return the nearest point of the wall
     */
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

    /**
     * finds the nearest enemy
     *
     * @return the nearest enemy
     */
    private Enemy findNearestEnemy() {
        float minDist = 9999999;
        Enemy nearest = null;
        for (Enemy enemy : Main.enemies) {
            if (enemy != null) {
                float dist = (float) Math.sqrt(Math.pow(enemy.x - x, 2) + Math.pow(enemy.y - y, 2));
                if (dist < minDist) {
                    minDist = dist;
                    nearest = enemy;
                }
            }
        }
        return nearest;
    }

    /**
     * does damage to the player and sets up states for the hit effect.
     *
     * @param damage the amount of damage to be done. can be negative for a weird healing thing if you want
     */
    @Override
    public void damage(int damage) {
        super.damage(damage);
        hitTime = 0f;
        beingHit = true;
        Main.hitSound.play();
    }

    /**
     * draws the players HP as a UI on the screen
     *
     * @param drawer the UI drawer. defined in main
     * @param batch the UI batch. defined in main
     * @param border the border texture.
     */
    public void drawUI(ShapeDrawer drawer, SpriteBatch batch, Texture border) {
        batch.draw(border, 0, 0);

        drawer.setColor(Color.GREEN);
        drawer.line(45f, 27f, ((float) hp) * 1.1f + 45f, 27f, 6f);
    }
}
