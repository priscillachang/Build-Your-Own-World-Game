package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.List;

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

    public void drawRoom(TETile[][] world) {
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                world[i][j] = Tileset.FLOOR;
            }
        }
        for (int i = x - 1; i < x + width + 1; i++) {
            world[i][y - 1] = Tileset.WALL;
            world[i][y + height] = Tileset.WALL;
        }
        for (int j = y; j < y + height; j++) {
            world[x - 1][j] = Tileset.WALL;
            world[x + width][j] = Tileset.WALL;
        }
    }

    public boolean intersects(Room other) {
        int startX = x - 1;
        int endX = x + width + 1;
        int startY = y - 1;
        int endY = y + height + 1;

        boolean xIntersects = intersectsInterval(other.x, startX, endX)
                || intersectsInterval(other.x + other.width, startX, endX);
        boolean yIntersects = intersectsInterval(other.y, startY, endY)
                || intersectsInterval(other.y + other.height, startY, endY);
        return xIntersects && yIntersects;
    }

    public boolean intersectsAny(List<Room> roomList) {
        for (Room r : roomList) {
            if (intersects(r)) {
                return true;
            }
        }
        return false;
    }

    private boolean intersectsInterval(int coordinate, int start, int end) {
        return coordinate >= start && coordinate < end;
    }
}
