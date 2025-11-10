package io.github.FOUR.game;

import java.util.Arrays;
import java.util.Random;

//Map key:
//1 = basic wall
//0 = empty
//-1 = player spawn
// -2 = win space tbi
// -3 = damage tick tile
// -4 = enemy tile

//>0 = solid
//<=0 = no collision
public class ProcGen {
    public int[] map;
    public int[] floor;
    public int blockSize = 10;
    public int roomX, roomY;
    Random random;
    public ProcGen(Random random) {
        this.random = random;
        // rooms
        roomX = 10;
        roomY = 5;
        map  = new int[roomX*roomY*blockSize*blockSize];
        Arrays.fill(map,0);
        // copy rooms into map
        for (int i = 0; i < roomX; i++) {
            for (int j = 0; j < roomY; j++) {
                int[] room = genRoom();
                for (int x = 0; x < blockSize; x++) {
                    for (int y = 0; y < blockSize; y++) {
                        map[cellToMap(i * blockSize + x, j * blockSize + y)] = room[x + y * blockSize];
                    }
                }
            }
        }
        // dig paths
        RecursiveBacktracker r = new RecursiveBacktracker(roomX, roomY, random);
        for (int i = 0; i < roomX; i++) {
            for (int j = 0; j < roomY; j++) {
                int roomPath = r.grid[r.gridToCell(i, j)];

                int x = i*blockSize,  y = j*blockSize;
                int tunnelWidth = random.nextInt(2)+1;
                int midBlock = random.nextInt(blockSize-tunnelWidth-1)+tunnelWidth;
                // bitmask for dir
                if ((roomPath & 1) == 1) {
                   // path up
                    // make path starting halfway through room width 3
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
        int spawnX = random.nextInt(roomX), spawnY = random.nextInt(roomY);
        boolean found = false;
        for (int x = 0;  x < blockSize; x++) {
            for (int y = 0; y < blockSize; y++) {
               if (map[cellToMap(spawnX*blockSize+x, spawnY*blockSize+y)] == 0) {
                   map[cellToMap(spawnX * blockSize + x, spawnY * blockSize + y)] = -1;
                   found = true;
                   break;
               }
            }
            if (found) break;
        }

        // corridors
    }
    public int[] genRoom() {
        // declare map
        // generate
        int[] room = new int[blockSize*blockSize];
        Arrays.fill(room, 1);
        for (int i = 1; i < blockSize-1; i++) {
            for (int j = 1; j < blockSize-1; j++) {

                room[i + j*(blockSize)] = 0;
                if (random.nextInt(20) == 0) {
                    room[i+j*(blockSize)] = 1;
                }
                if (random.nextInt(100) == 0) {
                    room[i+j*(blockSize)] = -4;
                }
            }
        }
        return room;
    }

    public void makeEmptyRect(int sX, int sY, int x, int y) {
        for  (int i = sX; i < sX+x; i++) {
            for(int j = sY; j < sY+y; j++) {
                map[cellToMap(i,j)] = 0;
            }
        }
    }

    public void display() {
        for (int y = roomY*blockSize-1; y > -1; y--) {
            for (int x = 0; x < roomX*blockSize; x++) {
                System.out.print(map[x+y*blockSize*roomX] + ",");
            }
            System.out.print("\n");
        }
    }

    public int cellToMap(int x, int y) {
        return x+y*roomX*blockSize;
    }
}

class RecursiveBacktracker {
    int[] grid;
    int dimensionsX, dimensionsY;
    Random random;

    public RecursiveBacktracker(int dimensionsX, int dimensionsY, Random random) {
        this.dimensionsX = dimensionsX;
        this.dimensionsY = dimensionsY;
        this.random = random;
        grid = new int[dimensionsX * dimensionsY];
        Arrays.fill(grid, -1);
        branch(random.nextInt(dimensionsX), random.nextInt(dimensionsY));
    }

    void branch(int x, int y) {
        grid[gridToCell(x, y)] = 0;
        int mask = 0;

        for (int i = 0; i < 4; i++) {
            int nextX = x, nextY = y;
            int dir = 1 << random.nextInt(4);

            while ((dir | mask) == mask) {
                dir <<= 1;
                if (dir > 8) dir = 1;
            }
            mask |= dir;

            switch (dir) {
                case 1: nextY = y + 1; break; // up
                case 2: nextX = x - 1; break; // left
                case 4: nextY = y - 1; break; // down
                case 8: nextX = x + 1; break; // right
            }

            if (nextX < 0 || nextY < 0 || nextX >= dimensionsX || nextY >= dimensionsY) continue;
            if (grid[gridToCell(nextX, nextY)] != -1) continue;

            grid[gridToCell(x, y)] |= dir;

            branch(nextX, nextY);
        }
    }

    public int[] getGrid() {
        return grid;
    }

    int gridToCell(int x, int y) {
        return x + y * dimensionsX;
    }
}

