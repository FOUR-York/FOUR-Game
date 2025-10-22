package io.github.FOUR.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.awt.*;
import java.util.Random;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    public static int WORLD_WIDTH = 640, WORLD_HEIGHT = 360;

    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;

    private ShapeRenderer shapeRenderer;

    int[][] map;

    Random random;

    @Override
    public void create() {
        // libgdx init
        camera = new  OrthographicCamera();
        viewport =  new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        //
        random = new Random();
        random.setSeed(System.currentTimeMillis());

        //generate map
        // dimensions
        int mapX = 10;
        int mapY = 10;
        map = new int[mapX][mapY];
        for (int i = 0; i < mapX; i++) {
            for (int j = 0; j < mapY; j++) {
                map[i][j] = 1;
            }
        }
        // generate nodes
        int nodes = 10;
        Vector2 lastNode = new Vector2(random.nextInt(0, mapX), random.nextInt(0, mapY));
        for (int i = 1; i < nodes; i++) {
            Vector2 node = new Vector2(random.nextInt(0, mapX), random.nextInt(0, mapY));
            // x dir
            for  (int j = 0; j < (int)Math.abs(lastNode.y-node.y)+1; j++) {
                map[(int)lastNode.x][(int)lastNode.y+j*(lastNode.y < node.y? 1 : -1)] = 0;
            }
            // y dir
            for (int j = 0; j < (int)Math.abs(lastNode.x-node.x)+1; j++) {
                map[(int)node.x+j*(lastNode.x < node.x? -1 : 1)][(int)node.y] = 0;
            }
            lastNode = node;
        }
        for (int i = 0; i < mapY; i++) {
            for (int j = 0; j < mapX; j++) {
                System.out.print(map[j][i]);
                System.out.print(",");
            }
            System.out.print("\n");
        }
    }

    @Override
    public void render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        input();
        step();
        draw();
    }

    public void input() {

    }

    public void step() {

    }

    public void draw() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1.5f, 0.5f, 0.5f, 1f);
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == 0) {
                    shapeRenderer.rect(j*32f, (map.length-1)*32f-i*32f, 32f, 32f);
                }
            }
        }
        shapeRenderer.end();
    }


    @Override
    public void dispose() {
        batch.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }
}
