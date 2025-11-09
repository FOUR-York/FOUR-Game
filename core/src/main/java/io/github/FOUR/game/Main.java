package io.github.FOUR.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.Arrays;
import java.util.Random;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    public static final int WORLD_WIDTH = 640, WORLD_HEIGHT = 360;

    private static boolean ended = false, won = false, lost = false;

    private static OrthographicCamera camera;
    private static FitViewport viewport;
    private static SpriteBatch batch;

    private static ShapeDrawer shapeDrawer;

    private static Texture drawerTexture, playerTexture, enemyTexture, floorTexture;
    private static TextureRegion[] floorTiles;

    public static Sound hitSound, fallSound, deathSound, awakeSound;
    public static Music music1;

    //Map key:
    //1 = basic wall
    //0 = empty
    //-1 = player spawn
    // -2 = win space tbi
    // -3 = damage tick tile
    // -4 = enemy tile

    //>0 = solid
    //<=0 = no collision
    public static int[][] mapF;
    public static int[][] mapW;
    public static int mapX, mapY, mapS = 32;

    public static Player player;

    public static Enemy[] enemies = new Enemy[100];
    public static int enemyCount = 0;

    public Random random;

    @Override
    public void create() {
        // libgdx init
        camera = new  OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        batch = new SpriteBatch();

        initialiseShapeDrawer();
        // generate map
        random = new Random();
        random.setSeed(System.currentTimeMillis());
        ProcGen p = new ProcGen(random);
        // copy p.map to mapW
        // need to go backwards...
        mapX = p.roomX*p.blockSize;
        mapY = p.roomY*p.blockSize;
        mapW = new int[mapY][mapX];
        mapF = new int[mapY][mapX];
        for (int i = 0; i < mapY; i++) {
            for (int j = 0; j < mapX; j++) {
                mapF[i][j] = 1;
            }
        }
        for  (int i = 0; i < mapX; i++) {
            for (int j = 0; j < mapY; j++) {
                mapW[j][i] = p.map[p.cellToMap(i, j)];
            }
        }

        playerTexture = new Texture(Gdx.files.internal("textures/player.png"));
        float[] playerSpawn = findPlayerSpawn();
        player = new Player(playerSpawn[0], playerSpawn[1], 1000f, 100, 10, playerTexture);

        enemyTexture = new Texture(Gdx.files.internal("textures/enemy.png"));

        floorTexture = new Texture(Gdx.files.internal("textures/floor.png"));
        floorTiles = new TextureRegion[] {new TextureRegion(floorTexture, 0, 0, 32, 32)};

        hitSound = Gdx.audio.newSound(Gdx.files.internal("audio/hit.mp3"));
    }

    @Override
    public void render() {
        if (!ended) {
            input();
            logic();
            draw();
        }

        if (won) {

        }

        if (lost) {

        }
    }

    public void input() {
        player.move();
    }

    public void logic() {
        updateCamera();
        for (Enemy enemy : enemies) {
            if (enemy != null) {
                enemy.move();
            }
        }
        player.collision();
    }

    public void draw() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        drawMapF2D();
        drawMapW2D();
        for (Enemy enemy : enemies) {
            if (enemy != null) {
                enemy.draw(batch);
            }
        }
        player.draw(batch);
        player.drawFOV(shapeDrawer, 360);
        batch.setProjectionMatrix(camera.combined);
        batch.end();
    }


    @Override
    public void dispose() {
        batch.dispose();
        playerTexture.dispose();
        drawerTexture.dispose();
        enemyTexture.dispose();
        floorTexture.dispose();
        hitSound.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public static void win() {
        ended = true;
        won = true;
    }

    public static void lose() {
        ended = true;
        lost = true;
    }

    private void drawMapW2D() {
        int x, y, xo, yo;
        for(y = 0; y < mapY; y++) {
            for(x = 0; x < mapX; x++) {
                if(mapW[y][x] == 1) {
                    shapeDrawer.setColor(0f,0f,0f,1f);
                    xo = x * mapS;
                    yo = (mapY*mapS)-((y+1) * mapS);
                    shapeDrawer.filledRectangle(xo,yo,mapS-1,mapS-1);
                }
            }
        }
    }

    private void drawMapF2D() {
        int x, y, xo, yo;
        for(y = 0; y < mapY; y++) {
            for(x = 0; x < mapX; x++) {
                if(mapF[y][x] == 1) {
                    xo = x * mapS;
                    yo = (mapY*mapS)-((y+1) * mapS);
                    batch.draw(floorTiles[0], xo, yo);
                }
            }
        }
    }


    private void updateCamera() {
        camera.position.set(player.x, player.y, 0);
        camera.update();
    }

    private void initialiseShapeDrawer() {
        Pixmap drawerPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        drawerPixmap.setColor(Color.WHITE);
        drawerPixmap.drawPixel(0, 0);
        drawerTexture = new Texture(drawerPixmap); //remember to dispose of later
        drawerPixmap.dispose();
        TextureRegion drawerRegion = new TextureRegion(drawerTexture, 0, 0, 1, 1);
        shapeDrawer = new ShapeDrawer(batch, drawerRegion);
    }

    private float[] findPlayerSpawn() {
        int x, y;
        for (y = 0; y < mapY; y++) {
            for (x = 0; x < mapX; x++) {
                if (mapW[y][x] == -1) {
                    return new float[] {(x*mapS)+((float)mapS/2), (mapY*mapS)-(((y+1)*mapS)+((float)mapS/2))};
                }
            }
        }
        throw new NoPlayerSpawnException("No player spawn found");
    }

    private void spawnEnemy(float x, float y) {
        enemies[enemyCount] = new Enemy(x, y, 50, 100, 5, 160, enemyTexture, enemyCount);
        enemyCount++;
    }

    private void removeEnemy(int index) {
        enemies[index] = null;
        enemyCount--;
    }
}
