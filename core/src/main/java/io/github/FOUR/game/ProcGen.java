package io.github.FOUR.game;

import com.badlogic.gdx.utils.Array;

import java.util.Arrays;
import java.util.Random;

import static io.github.FOUR.game.Main.mapW;
import static io.github.FOUR.game.Main.random;

//Map key:
//1 = basic wall
//0 = empty
//-1 = player spawn
// -2 = win space tbi
// -3 = damage tick tile
// -4 = enemy tile
// -5 = room change tile
// -6 = long boi tile

//>0 = solid
//<=0 = no collision
public class ProcGen {
    public int[] map;
    public int[] floor;
    public int blockSize = 10;
    public int roomX, roomY;
    public ProcGen() {
        // rooms
        roomX = 5;
        roomY = 3;
        map  = new int[roomX*roomY*blockSize*blockSize];
        floor  = new int[roomX*roomY*blockSize*blockSize];
        Arrays.fill(map,0);
        Arrays.fill(floor,0);
        // copy rooms into map
        for (int i = 0; i < roomX; i++) {
            for (int j = 0; j < roomY; j++) {
                int[][] room = genRoom();
                for (int x = 0; x < blockSize; x++) {
                    for (int y = 0; y < blockSize; y++) {
                        map[cellToMap(i * blockSize + x, j * blockSize + y)] = room[0][x + y * blockSize];
                        floor[cellToMap(i * blockSize + x, j * blockSize + y)] = room[1][x + y * blockSize];
                    }
                }
            }
        }
        // dig paths
        RecursiveBacktracker r = new RecursiveBacktracker(roomX, roomY);
        for (int i = 0; i < roomX; i++) {
            for (int j = 0; j < roomY; j++) {
                int roomPath = r.grid[r.cellToGrid(i, j)];

                int x = i*blockSize,  y = j*blockSize;
                int tunnelWidth = random.nextInt(2)+1;
                int midBlock = random.nextInt(blockSize-tunnelWidth-1)+tunnelWidth;
                // bitmask for dir
                if ((roomPath & 1) == 1) {
                   // path up
                    makeEmptyRect(x+midBlock, y+midBlock, tunnelWidth, blockSize);
                }
                if ((roomPath & 2) == 2) {
                    // path left
                    makeEmptyRect(x+midBlock-blockSize, y+midBlock, blockSize, tunnelWidth);
                }
                if ((roomPath & 4) == 4) {
                    // path down
                    makeEmptyRect(x+midBlock, y+midBlock-blockSize, tunnelWidth, blockSize);
                }
                if ((roomPath & 8) == 8) {
                    // path right
                    makeEmptyRect(x+midBlock, y+midBlock, blockSize, tunnelWidth);
                }
            }
        }
        // make spawn
        int[] corners = Main.shuffle(new int[]{1,2,3,4});
        int spawnX, spawnY;
        switch(corners[0]) {
            case 2: {
                spawnX = roomX-1;
                spawnY = 0;
                break;
            }
            case 3: {
                spawnX = 0;
                spawnY = roomY-1;
                break;
            }
            case 4: {
                spawnX = roomX-1;
                spawnY = roomY-1;
                break;
            }
            case 1:
            default: {
                spawnX = 0;
                spawnY = 0;
                break;
            }
        };
        {
            int[][] positions = findXInMapRegion(spawnX * blockSize + 1, spawnY * blockSize + 1, blockSize - 1, blockSize - 1, 0);
            int[] position = positions[random.nextInt(positions.length)];
            map[cellToMap(position[0], position[1])] = -1;
        }

        // corridors

        // place win space
        int endX, endY;
        switch(corners[1]) {
            case 2: {
                endX = roomX-1;
                endY = 0;
                break;
            }
            case 3: {
                endX = 0;
                endY = roomY-1;
                break;
            }
            case 4: {
                endX = roomX-1;
                endY = roomY-1;
                break;
            }
            case 1:
            default: {
                endX = 0;
                endY = 0;
                break;
            }
        };
        {
            int[][] positions = findXInMapRegion(endX * blockSize + 1, endY * blockSize + 1, blockSize - 1, blockSize - 1, 0);
            int[] position = positions[random.nextInt(positions.length)];
            map[cellToMap(position[0], position[1])] = -2;
            floor[cellToMap(position[0], position[1])] = 4;
        }
    }

//    public void copyIntoRegion(int x, int y, int[] rWalls, int[] rFloor, int[] rFurniture) {
//        int[] rWidth;
//        int[] rHeight;
//        for (int i = x; i < rWalls; i++) {
//            for (int j = x; j < blockSize; j++) {
//                map[cellToMap(i * blockSize + x, j * blockSize + y)] = room[0][x + y * blockSize];
//                floor[cellToMap(i * blockSize + x, j * blockSize + y)] = room[1][x + y * blockSize];
//            }
//        }
//    }

    /**
     *
    * generates a room and floor map in blockSize coordinate space
    * to be copied into the main map
     * long boi has a low chance of appearing
     @return
     */
    public int[][] genRoom() {
        // declare map
        // generate
        int[] room = new int[blockSize*blockSize];
        int[] floor = new int[blockSize*blockSize];
        Arrays.fill(room, 1);
        Arrays.fill(floor, -1);
        for (int i = 1; i < blockSize-1; i++) {
            for (int j = 1; j < blockSize-1; j++) {
                room[i + j*(blockSize)] = 0;
                floor[i + j*(blockSize)] = 1;
                if (random.nextInt(30) == 0 && i > 2 &&  i < blockSize-2 && j > 2 && j < blockSize-2) {
                    room[i+j*(blockSize)] = 1;
                }
                else if (random.nextInt(100) == 0) {
                    room[i+j*(blockSize)] = -4;
                } else if (random.nextInt(1000) == 0) {
                    room[i+j*(blockSize)] = -6;
                }
            }
        }
        return new int[][]{room, floor};
    }

    /**
     * returns the x and y coordinate of tiles matching tileIndex in a region
     * @param x
     * @param y
     * @param width
     * @param height
     * @param tileIndex
     * @return
     */
    public int[][] findXInMapRegion(int x, int y, int width, int height, int tileIndex) {
        // count in range
        int count = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (map[cellToMap(x+i, y+j)] == tileIndex) {
                    count++;
                }
            }
        }
        int[][] result = new int[count][2];
        int counter = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (map[cellToMap(x+i, y+j)] == tileIndex) {
                    result[counter][0] = x+i;
                    result[counter][1] = y+j;
                    counter++;
                }
            }
        }
        return result;
    }

    /**
    * clears a rectangular space of walls on the map
     @param sX bottom left x coord
     @param sY bottom left y coord
     @param x width
     @param y height
     @return
     */
    public void makeEmptyRect(int sX, int sY, int x, int y) {
        for  (int i = sX; i < sX+x; i++) {
            for(int j = sY; j < sY+y; j++) {
                map[cellToMap(i,j)] = 0;
                floor[cellToMap(i,j)] = 1;
            }
        }
    }

    /**
     * takes two grid coords and returns a corresponding map array index
     * @param x
     * @param y
     * @return index
     */
    public int cellToMap(int x, int y) {
        return x+y*roomX*blockSize;
    }
}

class RecursiveBacktracker {
    int[] grid;
    int dimensionsX, dimensionsY;

    public RecursiveBacktracker(int dimensionsX, int dimensionsY) {
        this.dimensionsX = dimensionsX;
        this.dimensionsY = dimensionsY;
        grid = new int[dimensionsX * dimensionsY];
        Arrays.fill(grid, -1);
        branch(random.nextInt(dimensionsX), random.nextInt(dimensionsY));
    }

    /**
     * main recursive logic
     * @param x
     * @param y
     */
    void branch(int x, int y) {
        grid[cellToGrid(x, y)] = 0;

        int[] dirs = Main.shuffle(new int[]{1, 2, 4, 8});
        for (int i = 0; i < 4; i++) {
            int dir = dirs[i];
            int nextX = x, nextY = y;

            switch (dir) {
                case 1: nextY = y + 1; break; // up
                case 2: nextX = x - 1; break; // left
                case 4: nextY = y - 1; break; // down
                case 8: nextX = x + 1; break; // right
            }

            if (nextX < 0 || nextY < 0 || nextX >= dimensionsX || nextY >= dimensionsY) continue;
            if (grid[cellToGrid(nextX, nextY)] != -1) continue;

            grid[cellToGrid(x, y)] |= dir;
            grid[cellToGrid(nextX, nextY)] |= dirOpposite(dir);

            branch(nextX, nextY);
        }
    }

    int dirOpposite(int dir) {
        switch (dir) {
            case 1: return 4;
            case 2: return 8;
            case 4: return 1;
            case 8: return 2;
        }
        return 0;
    }

    /**
     * returns the corresponding grid index given cell coords
     * @param x
     * @param y
     * @return index
     */
    int cellToGrid(int x, int y) {
        return x + y * dimensionsX;
    }
}

