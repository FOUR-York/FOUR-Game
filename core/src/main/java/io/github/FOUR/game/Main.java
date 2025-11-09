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
    //the screen size. make sure to change it in lwjgl3 too
    public static final int WORLD_WIDTH = 640, WORLD_HEIGHT = 360;

    private static boolean ended = false, won = false, lost = false;

    private static OrthographicCamera camera;
    private static FitViewport viewport;
    private static SpriteBatch batch, UIbatch;

    private static ShapeDrawer shapeDrawer, UIdrawer;

    private static Texture drawerTexture, playerTexture, enemyTexture, floorTexture, itemTexture, hpTexture;
    private static TextureRegion[] floorTiles;

    public static Sound hitSound, fallSound, deathSound, awakeSound, pickUpSound;
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

    public static Enemy[] enemies = new Enemy[1000];
    public static int enemyCount = 0;

    public static Item[] items = new Item[1000];
    public static int itemCount = 0;

    public Random random;

    @Override
    public void create() {
        // libgdx init
        camera = new  OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        batch = new SpriteBatch();
        UIbatch = new SpriteBatch();

        initialiseShapeDrawers();
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
        player = new Player(playerSpawn[0], playerSpawn[1], 100f, 100, 10, playerTexture);

        enemyTexture = new Texture(Gdx.files.internal("textures/enemy.png"));

        floorTexture = new Texture(Gdx.files.internal("textures/floor.png"));
        floorTiles = new TextureRegion[] {new TextureRegion(floorTexture, 0, 0, 32, 32)};

        itemTexture = new Texture(Gdx.files.internal("textures/item.png"));

        hpTexture = new Texture(Gdx.files.internal("textures/hp.png"));

        hitSound = Gdx.audio.newSound(Gdx.files.internal("audio/hit.mp3"));
        deathSound = Gdx.audio.newSound(Gdx.files.internal("audio/death.wav"));
        awakeSound = Gdx.audio.newSound(Gdx.files.internal("audio/awake.wav"));
        fallSound = Gdx.audio.newSound(Gdx.files.internal("audio/fall.wav"));
        pickUpSound = Gdx.audio.newSound(Gdx.files.internal("audio/pickup.wav"));
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

    /**
     * handles player input
     */
    public void input() {
        player.move();
    }

    /**
     * handles everything not drawing or input related
     */
    public void logic() {
        updateCamera();
        for (Enemy enemy : enemies) {
            if (enemy != null) {
                enemy.move();
            }
        }
        for (Item item : items) {
            if (item != null) {
                item.collision();
            }
        }
        player.collision();
    }

    /**
     * handles drawing
     */
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

        UIbatch.begin();
        player.drawUI(UIdrawer, UIbatch, hpTexture);
        UIbatch.end();
    }


    @Override
    public void dispose() {
        batch.dispose();
        UIbatch.dispose();
        playerTexture.dispose();
        drawerTexture.dispose();
        enemyTexture.dispose();
        floorTexture.dispose();
        hpTexture.dispose();
        hitSound.dispose();
        deathSound.dispose();
        awakeSound.dispose();
        fallSound.dispose();
        pickUpSound.dispose();
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

    /**
     * draws walls and handles special tiles
     */
    private void drawMapW2D() {
        int x, y, xo, yo;
        for(y = 0; y < mapY; y++) {
            for(x = 0; x < mapX; x++) {
                xo = x * mapS;
                yo = (mapY*mapS)-((y+1) * mapS);
                switch (mapW[y][x]) {
                    case 1:
                        shapeDrawer.setColor(0f,0f,0f,1f);
                        shapeDrawer.filledRectangle(xo,yo,mapS,mapS);
                        break;
                    case -4:
                        spawnEnemy(xo, yo);
                        break;
                }
            }
        }
    }

    /**
     * draws floor tiles
     */
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

    /**
     * makes the camera follow the player
     */
    private void updateCamera() {
        camera.position.set(player.x, player.y, 0);
        camera.update();
    }

    /**
     * sets up both the shape drawers
     */
    private void initialiseShapeDrawers() {
        Pixmap drawerPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        drawerPixmap.setColor(Color.WHITE);
        drawerPixmap.drawPixel(0, 0);
        drawerTexture = new Texture(drawerPixmap); //remember to dispose of later
        drawerPixmap.dispose();
        TextureRegion drawerRegion = new TextureRegion(drawerTexture, 0, 0, 1, 1);
        shapeDrawer = new ShapeDrawer(batch, drawerRegion);
        UIdrawer = new ShapeDrawer(UIbatch, drawerRegion);
    }

    /**
     * searches mapW until it finds the player spawn so it can assign it
     *
     * @return the coordinates of the spawn point
     *
     * @throws NoPlayerSpawnException when there's no spawn point on the map
     */
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

    /**
     * spawns a new enemy at x, y
     *
     * @param x the x coordinate for it to spawn
     * @param y the y coordinate for it to spawn
     */
    public static void spawnEnemy(float x, float y) {
        enemies[enemyCount] = new Enemy(x, y, 50, 100, 5, 160, enemyTexture, enemyCount);
        enemyCount++;
    }

    /**
     * deletes the specified enemy
     *
     * @param index the index of the enemy that needs to be deleted
     */
    public static void removeEnemy(int index) {
        enemies[index] = null;
    }

    /**
     * spawns a new item of type, at x, y
     *
     * @param x the x coord
     * @param y the y coord
     * @param type the type of item. see Item class for the key
     */
    public static void spawnItem(float x, float y, int type) {
        items[itemCount] = new Item(x, y, type, itemTexture, itemCount);
        itemCount++;
    }

    /**
     * deletes the specified item
     *
     * @param index the index of the item to be deleted
     */
    public static void removeItem(int index) {
        items[index] = null;
    }
}
