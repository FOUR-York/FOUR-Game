package io.github.FOUR.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * the enemy class handles an individual enemy
 * used for: enemy AI, handling enemy health, drawing them.
 */
public class Enemy extends LivingThing {
    private float range;
    public int index;

    //timers
    private float stateTime = 0f, swingTime = 0f, hitTime = 0f;

    //states
    private boolean patrolling = false, chasing = false, swinging = false, down = true, up = false, right = false, left = false, hitSuccess = false, beingHit = false;

    private float targetX, targetY, cAngle;
    private int mapHeight = (Main.mapS * Main.mapY);

    private Animation<TextureRegion> walkUp, walkSide, walkDown;
    private TextureRegion[] walkUpFrames, walkSideFrames, walkDownFrames;

    //private Sound hitSound;

    /**
     * the constructor for the enemy class
     *
     * @param x initial x pos
     * @param y initial y pos
     * @param speed movement speed
     * @param hp initial hp
     * @param attack damage per hit
     * @param range max sight range
     * @param texture enemy sprite sheet
     */
    public Enemy(float x, float y, float speed, int hp, int attack, float range, Texture texture, int index) {
        super(x, y, speed, hp, attack, texture);
        this.range = range;
        this.index = index;

        //Create texture region arrays for anims
        walkDownFrames = new TextureRegion[] {new TextureRegion(texture, 0, 0, 32, 32), new TextureRegion(texture, 32, 0, 32, 32),};
        walkSideFrames = new TextureRegion[] {new TextureRegion(texture, 64, 0, 32, 32), new TextureRegion(texture, 96, 0, 32, 32),};
        walkUpFrames = new TextureRegion[] {new TextureRegion(texture, 128, 0, 32, 32), new TextureRegion(texture, 160, 0, 32, 32),};

        //Create anims with the arrays
        walkDown = new Animation<TextureRegion>(0.1f, walkDownFrames);
        walkSide = new Animation<TextureRegion>(0.1f, walkSideFrames);
        walkUp = new Animation<TextureRegion>(0.1f, walkUpFrames);
    }

    /**
     * handles enemy AI
     * if the player is in sight, chase and try to attack
     * if not then patrol
     */
    public void move() {
        float delta = Gdx.graphics.getDeltaTime();
        stateTime += delta;

        if (!dead) {
            if (isPlayerInSight(Main.player) && !Main.player.dead) {
                //if player in los, chase
                if (!swinging) {
                    chase(Main.player);
                }
                else {
                    swing(Main.player);
                }
            }
            else {
                //if not, patrol
                if (chasing) {
                    chasing = false;
                    Main.chaseCount -= 1;
                }
                patrol(range);
            }
            //set the sprite pos and offset it to be centered

            if (!swinging) {
                //make sure angle is between 0 and 2 pi
                if (cAngle < 0f) {
                    cAngle += 2 * (float) Math.PI;
                }
                if (cAngle > 2 * (float) Math.PI) {
                    cAngle -= 2 * (float) Math.PI;
                }

                if (cAngle <= Math.toRadians(45) || cAngle > Math.toRadians(315)) {
                    //state
                    right = true;
                    up = false;
                    down = false;
                    left = false;

                    //animation
                    TextureRegion frame = walkSide.getKeyFrame(stateTime, true);
                    sprite.setRegion(frame);
                    sprite.flip(false, false);
                }
                else if (cAngle > Math.toRadians(45) && cAngle <= Math.toRadians(135)) {
                    up = true;
                    right = false;
                    down = false;
                    left = false;

                    TextureRegion frame = walkUp.getKeyFrame(stateTime, true);
                    sprite.setRegion(frame);
                    sprite.flip(false, false);
                }
                else if (cAngle > Math.toRadians(135) && cAngle <= Math.toRadians(225)) {
                    left = true;
                    right = false;
                    up = false;
                    down = false;

                    TextureRegion frame = walkSide.getKeyFrame(stateTime, true);
                    sprite.setRegion(frame);
                    sprite.flip(true, false);
                }
                else {
                    down = true;
                    up = false;
                    right = false;
                    left = false;

                    TextureRegion frame = walkDown.getKeyFrame(stateTime, true);
                    sprite.setRegion(frame);
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
        }

        sprite.setPosition(x-16, y-16);

        if (stateTime > 300) {
            stateTime = 0f;
        }
    }

    @Override
    public void kill() {
        Main.deathSound.play();

        Main.score += 100;

        float rng = (float) Math.random() * 100;
        if (rng <= 20) {
            Main.spawnItem(x, y, 1);
        }

        Main.removeEnemy(index);
        if (chasing)
        {
            Main.chaseCount -= 1;
        }
    }

    /**
     * uses simple raycasting to calculate whether the enemy can see the player
     *
     * @param player the player, should be defined in main
     *
     * @return true if the player is in sight, false if not
     */
    private boolean isPlayerInSight(Player player) {
        float angle = (float) Math.atan2(player.y - y, player.x - x);
        float dist = (float) Math.sqrt(Math.pow(player.x - x, 2) + Math.pow(player.y - y, 2));
        float rx = x;
        float ry = y;

        for (int i = 0; i <= dist/4; i++) {
            rx += (float) (Math.cos(angle) * 4);
            ry += (float) (Math.sin(angle) * 4);

            if (Main.mapW[(int) ((mapHeight - (ry)) / 32)][(int) rx / 32] > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * moves towards the player and tries to attack if close enough
     *
     * @param player the player to chase
     */
    private void chase(Player player) {
        if (!chasing) {
            chasing = true;
            Main.chaseCount += 1;
        }

        float delta = Gdx.graphics.getDeltaTime();
        int mapHeight = (Main.mapS * Main.mapY);

        float angle = (float) Math.atan2(player.y - y, player.x - x);
        float dist = (float) Math.sqrt(Math.pow(player.x - x, 2) + Math.pow(player.y - y, 2));

        cAngle = angle;

        if (dist < 32) {
            swing(player);
        }
        else {
            float dx = (float) (Math.cos(angle) * speed * delta);
            float dy = (float) (Math.sin(angle) * speed * delta);

            if (Main.mapWSafe((int) (x+dx)/32,(int) ((mapHeight-y-dy)/32)) <= 0) {
                x += dx;
                y += dy;
            }
        }

    }

    /**
     * tries to attack the player and deals damage if in range
     *
     * @param player the player to be attacked
     */
    private void swing(Player player) {
        float delta = Gdx.graphics.getDeltaTime();
        float dist = (float) Math.sqrt(Math.pow(player.x - x, 2) + Math.pow(player.y - y, 2));

        if (!swinging) {
            swinging = true;
            swingTime = 0f;
        }
        else {
            if (down) {
                //first frame of anim
                sprite.setRegion(new TextureRegion(texture, 0, 32, 32, 32));
                sprite.flip(false, false);

                swingTime += delta;

                if (dist < 48 && !hitSuccess) {
                    player.damage(attack);
                    hitSuccess = true;
                    Main.score -= 10;
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

                if (dist < 48 && !hitSuccess) {
                    player.damage(attack);
                    hitSuccess = true;
                    Main.score -= 10;
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

                if (dist < 48 && !hitSuccess) {
                    player.damage(attack);
                    hitSuccess = true;
                    Main.score -= 10;
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

                if (dist < 48 && !hitSuccess) {
                    player.damage(attack);
                    hitSuccess = true;
                    Main.score -= 10;
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

    /**
     * moves around randomly in the set range
     *
     * @param range the range to move around
     */
    private void patrol(float range) {
        float delta = Gdx.graphics.getDeltaTime();

        if (!patrolling) {
            patrolling = true;

            float minX = x - range;
            float maxX = x + range;
            float minY = y - range;
            float maxY = y + range;

            targetX = minX + (float) Math.random() * (maxX - minX);
            targetY = minY + (float) Math.random() * (maxY - minY);
        }

        float angle = (float) Math.atan2(targetY - y, targetX - x);

        cAngle = angle;

        float dx = (float) (Math.cos(angle) * speed * delta);
        float dy = (float) (Math.sin(angle) * speed * delta);

        if (Main.mapWSafe((int) (x+dx)/32, (int) ((mapHeight-y-dy)/32)) <= 0) {
            x += dx;
            y += dy;
        }
        else {
            patrolling = false;
        }

        if (((float) Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2))) < 5) {
            patrolling = false;
        }
    }

    @Override
    public void damage(int damage) {
        super.damage(damage);
        hitTime = 0f;
        beingHit = true;
        Main.hitSound.play();
    }
}
