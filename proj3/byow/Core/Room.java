package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.List;
import java.util.Random;

public class Room {
    private int x;
    private int y;
    private int width;
    private int height;

    public Room(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        width = w;
        height = h;
    }

    /**
     * Draws the room onto a given TETile[][].
     * @param world The world to draw on.
     */
    public void drawRoom(TETile[][] world) {
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                world[i][j] = Tileset.FLOOR;
            }
        }
        for (int i = x - 1; i < x + width + 1; i++) {
            if (world[i][y - 1] == Tileset.NOTHING) {
                world[i][y - 1] = Tileset.WALL;
            }
            if (world[i][y + height] == Tileset.NOTHING) {
                world[i][y + height] = Tileset.WALL;
            }
        }
        for (int j = y; j < y + height; j++) {
            if (world[x - 1][j] == Tileset.NOTHING) {
                world[x - 1][j] = Tileset.WALL;
            }
            if (world[x + width][j] == Tileset.NOTHING) {
                world[x + width][j] = Tileset.WALL;
            }
        }
    }

    /**
     * Connects this room to another room with a random hallway.
     * @param world The world to draw on
     * @param other The other room to connect to
     */
    public void connect(TETile[][] world, Room other, Random rand) {
        if (intervalsIntersect(x, x + width, other.x, other.x + other.width)) {
            connectVerticallyAligned(world, other, rand);
        } else if (intervalsIntersect(y, y + height, other.y, other.y + other.height)) {
            connectHorizontallyAligned(world, other, rand);
        } else {
            connectWithTurn(world, other, rand);
        }
    }

    /**
     * Checks if this room intersects with another room.
     * @param other The room to check for intersections with
     * @return true if the rooms intersect, false otherwise
     */
    public boolean intersects(Room other) {
        boolean xIntersects = intervalsIntersect(x - 1, x + width + 1,
                other.x - 1, other.x + other.width);
        boolean yIntersects = intervalsIntersect(y - 1, y + height + 1,
                other.y - 1, other.y + other.height);
        return xIntersects && yIntersects;
    }

    /**
     * Returns true if this room intersects with any of the rooms in the provided List.
     * @param roomList A list of rooms to check against for intersections.
     * @return true if this room intersects with at least 1 room in the list; false otherwise
     */
    public boolean intersectsAny(List<Room> roomList) {
        for (Room r : roomList) {
            if (intersects(r)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Spawns a given character in a random location within this room.
     */
    public CharacterTile randomSpawn(Random rand, TETile type) {
        int characterX = RandomUtils.uniform(rand, x, x + width);
        int characterY = RandomUtils.uniform(rand, y, y + height);
        return new CharacterTile(characterX, characterY, type);
    }

    // Returns true if the given coordinate lies within the given interval.
    private static boolean intersectsInterval(int coordinate, int start, int end) {
        return coordinate >= start && coordinate < end;
    }

    private static boolean intervalsIntersect(int start1, int end1, int start2, int end2) {
        return intersectsInterval(start1, start2, end2)
                || intersectsInterval(end1 - 1, start2, end2)
                || intersectsInterval(start2, start1, end1)
                || intersectsInterval(end2 - 1, start1, end1);
    }

    private void connectVerticallyAligned(TETile[][] world, Room other, Random rand) {
        int minX = Math.max(x, other.x);
        int maxX = Math.min(x + width, other.x + other.width);
        int hallwayX = RandomUtils.uniform(rand, minX, maxX);
        int minHallwayY = Math.min(y + height, other.y + other.height);
        int maxHallwayY = Math.max(y, other.y);
        for (int i = minHallwayY; i < maxHallwayY; i++) {
            world[hallwayX][i] = Tileset.FLOOR;
            if (world[hallwayX - 1][i] == Tileset.NOTHING) {
                world[hallwayX - 1][i] = Tileset.WALL;
            }
            if (world[hallwayX + 1][i] == Tileset.NOTHING) {
                world[hallwayX + 1][i] = Tileset.WALL;
            }
        }
    }

    private void connectHorizontallyAligned(TETile[][] world, Room other, Random rand) {
        int minY = Math.max(y, other.y);
        int maxY = Math.min(y + height, other.y + other.height);
        int hallwayY = RandomUtils.uniform(rand, minY, maxY);
        int minHallwayX = Math.min(x + width, other.x + other.width);
        int maxHallwayX = Math.max(x, other.x);
        for (int i = minHallwayX; i < maxHallwayX; i++) {
            world[i][hallwayY] = Tileset.FLOOR;
            if (world[i][hallwayY - 1] == Tileset.NOTHING) {
                world[i][hallwayY - 1] = Tileset.WALL;
            }
            if (world[i][hallwayY + 1] == Tileset.NOTHING) {
                world[i][hallwayY + 1] = Tileset.WALL;
            }
        }
    }

    private void connectWithTurn(TETile[][] world, Room other, Random rand) {
        if (RandomUtils.bernoulli(rand)) {
            // vertical, then horizontal
            int hallwayX = RandomUtils.uniform(rand, x, x + width);
            int hallwayY = RandomUtils.uniform(rand, other.y, other.y + other.height);
            Room turnRoom = new Room(hallwayX, hallwayY, 1, 1);
            turnRoom.drawRoom(world);
            connectVerticallyAligned(world, turnRoom, rand);
            other.connectHorizontallyAligned(world, turnRoom, rand);
        } else {
            // horizontal, then vertical
            int hallwayX = RandomUtils.uniform(rand, other.x, other.x + other.width);
            int hallwayY = RandomUtils.uniform(rand, y, y + height);
            Room turnRoom = new Room(hallwayX, hallwayY, 1, 1);
            turnRoom.drawRoom(world);
            connectHorizontallyAligned(world, turnRoom, rand);
            other.connectVerticallyAligned(world, turnRoom, rand);
        }
    }
}
