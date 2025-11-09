package io.github.FOUR.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.FitViewport;
import space.earlygrey.shapedrawer.ShapeDrawer;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    public static final int WORLD_WIDTH = 640, WORLD_HEIGHT = 360;

    private static OrthographicCamera camera;
    private static FitViewport viewport;
    private static SpriteBatch batch;

    private static ShapeDrawer shapeDrawer;

    private Texture drawerTexture;
    private Texture playerTexture;
    private Texture enemyTexture;

    //Map key:
    //1 = basic wall
    //0 = empty
    //-1 = player spawn

    //>0 = solid
    //<=0 = no collision
    public static final int[][] mapW = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
    };
    public static final int[][] mapE = {
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
    };
    public static final int mapX = 20, mapY = 20, mapS = 32;

    public static Player player;

    public static Enemy[] enemies = new Enemy[32];

    @Override
    public void create() {
        // libgdx init
        camera = new  OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        batch = new SpriteBatch();

        initialiseShapeDrawer();

        playerTexture = new Texture(Gdx.files.internal("textures/player.png"));
        float[] playerSpawn = findPlayerSpawn();
        player = new Player(playerSpawn[0], playerSpawn[1], 100f, 100, 10, playerTexture);

        enemyTexture = new Texture(Gdx.files.internal("textures/enemy.png"));
        enemies[0] = new Enemy(200, 50, 50, 100, 10, 50, enemyTexture);
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
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
        drawMap2D();
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
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    private void drawMap2D() {
        int x, y, xo, yo;
        for(y = 0; y < mapY; y++) {
            for(x = 0; x < mapX; x++) {
                if(mapW[y][x] == 0) {
                    shapeDrawer.setColor(1f,1f,1f,1f);
                    xo = x * mapS;
                    yo = (mapY*mapS)-((y+1) * mapS);
                    shapeDrawer.filledRectangle(xo,yo,mapS-1,mapS-1);
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
}
