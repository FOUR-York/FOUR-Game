package io.github.FOUR.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.sin;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    //the screen size. make sure to change it in lwjgl3 too
    public static final int WORLD_WIDTH = 640, WORLD_HEIGHT = 360;

    public static int time = 300, score = 0;

    private static boolean loading = false, ended = false, won = false, lost = false;

    private static OrthographicCamera camera;
    private static FitViewport viewport;
    private static SpriteBatch batch, scBatch, UIbatch;
    private static ShaderProgram shaderProgram;

    private static float scPrevSpeed = 1.0f;
    private static float scDelta = 0.0f;

    private static ShapeDrawer shapeDrawer, UIdrawer;

    private static BitmapFont font;

    private static Texture drawerTexture, playerTexture, enemyTexture, itemTexture, hpTexture, winTexture, loseTexture, shaderSpace;
    private static Texture[] floorTextures;

    public static Sound hitSound, fallSound, deathSound, awakeSound, pickUpSound, winSound, loseSound;
    public static Music music1;

    public static float shaderTime;

    public static int[][] mapF;
    public static int[][] mapW;
    public static int mapX, mapY, mapS = 32;

    public static Player player;

    public static Enemy[] enemies = new Enemy[1000];
    public static int enemyCount = 0;

    public static Item[] items = new Item[1000];
    public static int itemCount = 0;

    public static Furniture[] furniture = new Furniture[1000];
    public static int furnitureCount = 0;
    public static Texture[] furnitureTextures;

    public static int nextArea = 2;

    public static int chaseCount = 0;

    public static Random random;

    @Override
    public void create() {
        // libgdx init
        camera = new  OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        batch = new SpriteBatch();
        UIbatch = new SpriteBatch();
        scBatch = new SpriteBatch();
        font = new BitmapFont();

        initialiseShapeDrawers();

        playerTexture = new Texture(Gdx.files.internal("textures/player.png"));

        enemyTexture = new Texture(Gdx.files.internal("textures/enemy.png"));

        loadFloorTextures();

        loadFurnitureTextures();

        itemTexture = new Texture(Gdx.files.internal("textures/item.png"));

        hpTexture = new Texture(Gdx.files.internal("textures/hp.png"));

        winTexture = new Texture(Gdx.files.internal("textures/win.png"));
        loseTexture = new Texture(Gdx.files.internal("textures/lose.png"));

        hitSound = Gdx.audio.newSound(Gdx.files.internal("audio/hit.mp3"));
        deathSound = Gdx.audio.newSound(Gdx.files.internal("audio/death.wav"));
        awakeSound = Gdx.audio.newSound(Gdx.files.internal("audio/awake.wav"));
        fallSound = Gdx.audio.newSound(Gdx.files.internal("audio/fall.wav"));
        pickUpSound = Gdx.audio.newSound(Gdx.files.internal("audio/pickup.wav"));
        winSound = Gdx.audio.newSound(Gdx.files.internal("audio/win.wav"));
        loseSound = Gdx.audio.newSound(Gdx.files.internal("audio/lose.wav"));

        music1 = Gdx.audio.newMusic(Gdx.files.internal("audio/music.mp3"));
        music1.setLooping(true);
        music1.setVolume(0.2f);

        // screen shader
        shaderSpace = new Texture("textures/blank.png");
        String vertexShader = Gdx.files.internal("shaders/vertex.glsl").readString();
        String fragmentShader = Gdx.files.internal("shaders/fragment.glsl").readString();
        shaderProgram = new ShaderProgram(vertexShader,fragmentShader);
        if (!shaderProgram.isCompiled()) {
            System.out.print(shaderProgram.getLog());
        }
        shaderProgram.pedantic = false;

        scBatch.setShader(shaderProgram);
        shaderTime = 0;

        random = new Random();

        areaStart();
    }

    /**
     * get mapW cell with inbuilt bounds check
     * @param x
     * @param y
     * @return
     */
    public static int mapWSafe(int x, int y) {
        if (x < 0 || x >= mapX || y < 0 || y >= mapY) {
            return -1;
        } else {
            return mapW[y][x];
        }
    }

    /**
     * helper function to load furniture textures
     */
    public void loadFurnitureTextures() {
        String[] textureNames = new String[] {
            "bench1",
            "bench2",
            "book",
            "chair",
            "lampost",
            "longboi",
            "screen1_1",
            "screen1",
            "screen2_1",
            "screen2",
            "screen3_1",
            "screen3",
            "shelf1",
            "shelf2",
            "shelf3",
            "tablecorner",
            "tableedge",
            "torch",
        };
        furnitureTextures = new Texture[textureNames.length];
        for (int i = 0; i < textureNames.length; i++) {
            furnitureTextures[i] = new Texture("textures/furniture/"+textureNames[i]+".png");
        }
    }

    public static void spawnFurniture(int texId, int dir, float x, float y) {
        Furniture f = new Furniture(furnitureTextures[texId], dir, mapS, x, y);
        furniture[furnitureCount] = f;
        furnitureCount++;
    }

    /**
     * helper function to load floor textures
     */
    public void loadFloorTextures() {
        String[] textureNames = new String[] {
           "floortiles",
            "carpet",
            "stage",
            "stagestairs",
            "lectureseats",
            "stagewscreen",
            "lectureseats",
            "insidestairs",
            "insideentryway", //8
            "bridgeHoriz",
            "bridgetoEast",
            "bridgetoNorth",
            "bridgetoSouth",
            "bridgetoWest",
            "bridgeVert",
            "doorway",
            "pathCorner",
            "path",
            "pathT",
            "grassEdgeESW",
            "grassEdgeSWE2Corner",
            "grass",
            "grassyflowers",
            "lake",
            "minicornerN",
            "minicornerS",
            "treebushes",
        };
        floorTextures = new Texture[textureNames.length];
        for (int i = 0; i < textureNames.length; i++) {
            floorTextures[i] = new Texture("textures/floor/"+textureNames[i]+".png");
        }
    }

    /**
     * start and initialise new area according to nextArea
     */
    public void areaStart() {
        switch(nextArea) {
            case 0:
                centralHall();
                break;
            case 1:
                beginMap(System.currentTimeMillis());
                break;
            case 2:
                office();
                break;
            case 3:
                outsideRoom();
                break;
        }

        float[] playerSpawn = findPlayerSpawn();
        player = new Player(playerSpawn[0], playerSpawn[1], 100f, 100, 10, playerTexture);

        for(int y = 0; y < mapY; y++) {
            for(int x = 0; x < mapX; x++) {
                switch (mapW[y][x]) {
                    case -4:
                        spawnEnemy((x*mapS)+((float)mapS/2), (mapY*mapS)-(((y)*mapS)+((float)mapS/2)));
                        break;
                    case -6:
                        spawnFurniture(5, 0, x, y);                }
            }
        }

        music1.play();
        loading = false;
    }

    /**
     * cleanup finished area
     */
    public static void endArea() {
        music1.stop();
        Arrays.fill(enemies, null);
        Arrays.fill(items, null);
        Arrays.fill(furniture, null);
        enemyCount = 0;
        itemCount = 0;
        furnitureCount = 0;
    }

    /**
     * called when a zone change tile is touched by the player
     * @param tile
     */
    public static void changeZoneFromTile(int tile) {
        endArea();
        loading = true;

        switch(tile>>2) {
            case 8:
                nextArea = 1;
                break;
            case 12:
                nextArea = 2;
                break;
        }
    }

    /**
     * Generates security office
     */
    public void office() {
        mapW = new int[][] {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, -1, 0, 2, 2, 2, 2, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 2, 0, 2, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 2, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 0, 0, 0, 0, 2, 1},
            {1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1},
            {1, 0, 0, 0, 2, 0, 0, 1, 1, 2, 2, 2, 2, 2, 1, 1},
            {1, 0, 2, 2, 2, 2, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 2, 2, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 2, 0, 2, 2, 0, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 2, 2, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 2, 0, -5, -5, 0, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        };
        mapY = mapW.length;
        mapX = mapW[0].length;
        // floor cells are encoded as multiples of 4 to incoporate rotation
        mapF = new int[][] {
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, 33, 0, 0, 0, 0, 0, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, -1},
            {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
            {-1, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, 0, -1},
            {-1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, 0, 0, 35, 35, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        };

        //Office Furniture
        //Chairs
        spawnFurniture(3, 0, 12, 10);
        spawnFurniture(3, 3, 9, 8);
        spawnFurniture(3, 2, 1, 7);
        spawnFurniture(3, 2, 4, 6);
        spawnFurniture(3, 3, 2, 5);
        spawnFurniture(3, 1, 5, 5);
        spawnFurniture(3, 3, 1, 3);
        spawnFurniture(3, 1, 6, 3);
        spawnFurniture(3, 0, 1, 1);
        spawnFurniture(3, 0, 6, 1);
        //Shelves
        spawnFurniture(13, 1, 14, 11);
        spawnFurniture(13, 1, 14, 10);
        spawnFurniture(12, 1, 14, 9);
        spawnFurniture(14, 1, 14, 8);
        spawnFurniture(12, 1, 14, 7);
        spawnFurniture(12, 0, 9, 6);
        spawnFurniture(12, 0, 10, 6);
        spawnFurniture(14, 0, 11, 6);
        spawnFurniture(13, 0, 12, 6);
        spawnFurniture(12, 0, 13, 6);
        //Screens
        spawnFurniture(7, 0, 11, 11);
        spawnFurniture(9, 0, 12, 11);
        spawnFurniture(11, 0, 13, 11);
        //Tables
        spawnFurniture(15, 0, 3, 5);
        spawnFurniture(15, 3, 4, 5);
        spawnFurniture(16, 0, 3, 4);
        spawnFurniture(16, 2, 4, 4);
        spawnFurniture(16, 0, 3, 3);
        spawnFurniture(16, 2, 4, 3);
        spawnFurniture(15, 1, 3, 2);
        spawnFurniture(15, 2, 4, 2);
    }

    /**
     * generates central hall
     */
    public void centralHall() {
        mapW = new int[][] {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 1},
            {1, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 1},
            {1, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 1},
            {1, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 1},
            {1, 2, 2, 2, 2, 2, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 1},
            {1, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 1},
            {1, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 1},
            {1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 2, 2, 2, 2, -5, -5, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        };
        mapY = mapW.length;
        mapX = mapW[0].length;
        // floor cells are encoded as multiples of 4 to incoporate rotation
        mapF = new int[][] {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 18, 18, 18, 18, 18, 0, 4, 8, 8, 8, 8, 8, 8, 4, 0, 16, 16, 16, 16, 16, 0},
            {0, 18, 18, 18, 18, 18, 0, 4, 8, 8, 8, 8, 8, 8, 4, 0, 16, 16, 16, 16, 16, 0},
            {0, 18, 18, 18, 18, 18, 0, 4, 12, 8, 8, 8, 8, 14, 4, 0, 16, 16, 16, 16, 16, 0},
            {0, 18, 18, 18, 18, 18, 0, 4, 4, 4, 4, 4, 4, 4, 4, 0, 16, 16, 16, 16, 16, 0},
            {0, 18, 11, 14, 11, 14, 34, 0, 0, 0, 0, 0, 0, 0, 0, 32, 12, 8, 12, 8, 16, 0},
            {0, 18, 18, 18, 18, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 16, 16, 0},
            {0, 18, 18, 18, 18, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 16, 16, 0},
            {0, 18, 18, 18, 18, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 16, 16, 0},
            {0, 18, 18, 18, 18, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 16, 16, 0},
            {1, 1, 1, 1, 1, 1, 19, 19, 19, 19, 1, 1, 19, 19, 19, 19, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 19, 19, 19, 19, 1, 1, 19, 19, 19, 19, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 19, 19, 19, 19, 35, 35, 19, 19, 19, 19, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        };
    }

    public void outsideRoom() {
        mapW = new int[][] {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 1},
            {1, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 1},
            {1, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 2, 2, 1},
            {1, 2, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
            {1, 2, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 1},
            {1, 2, 2, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 2, 1},
            {1, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 0, 0, 1},
            {1, 2, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 1},
            {1, 0, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 2, 1},
            {1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -5, 1},
            {1, 0, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 2, 1},
            {1, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 2, 1},
            {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 2, 0, 0, 1},
            {1, 0, 1, 1, 1, 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 0, 2, 1},
            {1, 2, 0, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 2, 1},
            {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 2, -5, 2, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        };
        mapY = mapW.length;
        mapX = mapW[0].length;
        // floor cells are encoded as multiples of 4 to incoporate rotation
        mapF = new int[][] {
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, 128, 128, 128, 128, 128, 84, 84, 84, 84, 84, 84, 84, 79, 92, 92, 92, 92, 92, 92, -1},
            {-1, 88, 88, 128, 128, 128, 51, 51, 51, 51, 51, 51, 51, 40, 36, 36, 56, 36, 92, 97, -1},
            {-1, 88, 88, 88, 128, 128, 51, 84, 88, 84, 84, 88, 88, 79, 92, 56, 36, 36, 92, 77, -1},
            {-1, 128, 88, 84, 51, 51, 51, 84, 81, 77, 77, 77, 77, 72, 92, 92, 92, 92, 92, 77, -1},
            {-1, 88, 88, 84, 51, 84, 84, 84, 79, 92, 92, 92, 92, 92, 92, 75, 76, 76, 76, 83, -1},
            {-1, 128, 84, 84, 51, 88, 84, 84, 79, 92, 92, 92, 92, 92, 92, 77, 51, 51, 51, 88, -1},
            {-1, 128, 128, 84, 51, 88, 88, 84, 79, 92, 92, 92, 92, 92, 92, 77, 84, 84, 51, 84, -1},
            {-1, 88, 84, 84, 51, 88, 88, 81, 72, 92, 92, 92, 92, 92, 92, 77, 88, 128, 51, 84, -1},
            {-1, 128, 84, 84, 51, 88, 128, 79, 92, 92, 92, 92, 92, 92, 75, 83, 88, 128, 51, 128, -1},
            {-1, 84, 84, 84, 51, 128, 54, 79, 92, 92, 92, 92, 92, 92, 77, 84, 128, 84, 51, 84, -1},
            {-1, 60, 51, 51, 51, 51, 51, 40, 36, 36, 36, 36, 36, 36, 52, 51, 51, 51, 51, 62, -1},
            {-1, 84, 88, 88, 88, 128, 84, 79, 92, 92, 92, 92, 92, 92, 77, 84, 128, 84, 51, 84, -1},
            {-1, 128, 88, 88, 128, 128, 128, 79, 92, 92, 92, 92, 92, 92, 77, 128, 128, 88, 51, 128, -1},
            {-1, 128, 128, 128, 128, 128, 128, 79, 92, 92, 92, 92, 92, 92, 77, 88, 88, 128, 51, 51, 88, -1},
            {-1, 84, 128, 128, 128, 84, 128, 79, 92, 92, 92, 92, 92, 92, 77, 128, 84, 128, 51, 128, -1},
            {-1, 128, 84, 88, 128, 88, 128, 79, 92, 92, 92, 92, 92, 92, 77, 128, 84, 88, 51, 128, -1},
            {-1, 128, 128, 128, 128, 128, 128, 79, 92, 92, 92, 92, 92, 92, 77, 84, 84, 84, 51, 128, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        };

        //Furniture
        //Benches
        spawnFurniture(0, 1, 9, 15);
        spawnFurniture(1, 1, 10, 15);
        spawnFurniture(0, 0, 17, 16);
        spawnFurniture(1, 0, 17, 15);
        spawnFurniture(1, 1, 16, 11);
        spawnFurniture(0, 1, 17, 11);
        //lamposts
        spawnFurniture(4, 0, 2, 8);
        spawnFurniture(4, 2, 2, 6);
        spawnFurniture(4, 0, 6, 8);
        spawnFurniture(4, 2, 6, 6);
        spawnFurniture(4, 0, 15, 8);
        spawnFurniture(4, 2, 15, 6);
        spawnFurniture(4, 1, 19, 11);
        spawnFurniture(4, 0, 19, 8);
        spawnFurniture(4, 2, 19, 6);
        spawnFurniture(4, 3, 17, 1);
    }


    /**
     * generate a procedural map and input the data
     * @param seed
     */
    public void beginMap(long seed) {

        // generate map
        random.setSeed(seed);
        ProcGen p = new ProcGen();
        // copy p.map to mapW
        // need to go backwards...
        mapX = p.roomX*p.blockSize;
        mapY = p.roomY*p.blockSize;
        mapW = new int[mapY][mapX];
        mapF = new int[mapY][mapX];
        for (int i = 0; i < mapX; i++) {
            for (int j = 0; j < mapY; j++) {
                mapF[j][i] = p.floor[p.cellToMap(i, j)];
            }
        }
        for  (int i = 0; i < mapX; i++) {
            for (int j = 0; j < mapY; j++) {
                mapW[j][i] = p.map[p.cellToMap(i, j)];
            }
        }
    }


    /**
     *
     */
    @Override
    public void render() {
        if (!ended) {
            if (!loading) {
                input();
                logic();
                draw();
            }
            else
            {
                areaStart();
            }
        }

        if (won) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            UIbatch.begin();
            UIbatch.draw(winTexture, 0, 0);
            CharSequence scoreStr = "" + score;
            font.draw(UIbatch, scoreStr, ((float) WORLD_WIDTH /2) - 25, 75);
            UIbatch.end();
        }

        if (lost) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            UIbatch.begin();
            UIbatch.draw(loseTexture, 0, 0);
            CharSequence scoreStr = "" + score;
            font.draw(UIbatch, scoreStr, ((float) WORLD_WIDTH /2) - 25, 75);
            UIbatch.end();
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
        if (score < 0) {
            score = 0;
        }
    }

    /**
     * handles drawing
     */
    public void draw() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        drawMapF2D();
        drawMapW2D();
        for (Furniture f : furniture) {
           if (f != null) {
               f.draw(batch);
           }
        }
        for (Enemy enemy : enemies) {
            if (enemy != null) {
                enemy.draw(batch);
            }
        }
        for (Item item : items) {
            if (item != null) {
                item.draw(batch);
            }
        }
        player.draw(batch);
        player.drawFOV(shapeDrawer, 360);

        batch.setProjectionMatrix(camera.combined);
        batch.end();

        drawScreenShader();

        UIbatch.begin();
        player.drawUI(UIdrawer, UIbatch, hpTexture);
        CharSequence timeStr = "Time: " + time;
        CharSequence scoreStr = "Score: " + score;
        font.draw(UIbatch, timeStr, 10, WORLD_HEIGHT - 10);
        font.draw(UIbatch, scoreStr, 10, WORLD_HEIGHT - 30);
        UIbatch.end();
    }

    /**
     * draws the screen shader overlay
     */
    public void drawScreenShader () {

        shaderTime+=Gdx.graphics.getDeltaTime();

        scBatch.begin();

        // pass in the following to the fragment glsl scripts
        float speed = (float) Math.log(chaseCount+1)+1f;
        if (speed != scPrevSpeed) {
            scDelta = shaderTime*5.0f*(scPrevSpeed-speed) + scDelta; // adjust position in the cycle to match previous cycle
            scPrevSpeed = speed;
        }
        float cycle = (float) (sin(speed*shaderTime*5.0f+ scDelta)/8.0f+1.0f);
        Vector2 v = new Vector2(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
        v.x = v.x / Gdx.graphics.getWidth();
        v.y = v.y / Gdx.graphics.getHeight();
        shaderProgram.setUniformf("center", v);
        shaderProgram.setUniformf("u_time", shaderTime);
        shaderProgram.setUniformf("u_cycle", cycle);
        shaderProgram.setUniformf("u_resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        scBatch.draw(shaderSpace, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        scBatch.end();
    }


    @Override
    public void dispose() {
        batch.dispose();
        UIbatch.dispose();
        playerTexture.dispose();
        drawerTexture.dispose();
        enemyTexture.dispose();
        for (Texture tex : floorTextures) {
            tex.dispose();
        }
        for (Texture tex : furnitureTextures) {
            tex.dispose();
        }
        hpTexture.dispose();
        winTexture.dispose();
        loseTexture.dispose();
        hitSound.dispose();
        deathSound.dispose();
        awakeSound.dispose();
        fallSound.dispose();
        pickUpSound.dispose();
        winSound.dispose();
        loseSound.dispose();
        music1.dispose();
        scBatch.dispose();
        shaderSpace.dispose();
        shaderProgram.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public static void win() {
        music1.stop();
        ended = true;
        won = true;
        score += time * 10;
        winSound.play();
    }

    public static void lose() {
        music1.stop();
        ended = true;
        lost = true;
        loseSound.play();
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
                if(mapF[y][x] >= 0) {
                    xo = x * mapS;
                    yo = (mapY*mapS)-((y+1) * mapS);
                    batch.draw(floorTextures[(mapF[y][x])>>2], xo, yo, mapS/2, mapS/2, mapS, mapS, 1, 1, 90f*(mapF[y][x]%4), 0, 0, 32, 32, false, false);
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
                    return new float[] {(x*mapS)+((float)mapS/2), (mapY*mapS)-(((y)*mapS)+((float)mapS/2))};
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
        enemies[enemyCount] = new Enemy(x, y, 50, 25, 5, 160, enemyTexture, enemyCount);
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

    /**
     * deletes the specified furniture
     * @param index
     */
    public static void removeFurniture(int index) {
        furniture[index] = null;
    }

    /**
     * Fisher–Yates shuffle helper function
     */
    public static int[] shuffle(int[] array) {
        for (int i = array.length-1; i > 0; i--) {
            int index = random.nextInt(i+1);
            int tmp = array[i];
            array[i] = array[index];
            array[index] = tmp;
        }
        return array;
    }
}
