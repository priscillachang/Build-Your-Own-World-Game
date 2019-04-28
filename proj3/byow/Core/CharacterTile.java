package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class CharacterTile {
    private int x;
    private int y;
    private TETile avatar;

    public CharacterTile(int x, int y, TETile avatar) {
        this.x = x;
        this.y = y;
        this.avatar = avatar;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean moveUp(TETile[][] world) {
        return moveTo(world, x, y + 1);
    }

    public boolean moveDown(TETile[][] world) {
        return moveTo(world, x, y - 1);
    }

    public boolean moveLeft(TETile[][] world) {
        return moveTo(world, x - 1, y);
    }

    public boolean moveRight(TETile[][] world) {
        return moveTo(world, x + 1, y);
    }

    public boolean moveTo(TETile[][] world, int newX, int newY) {
        if (newX < 0 || newX >= world.length
                || newY < 0 || newY >= world[newX].length) {
            return false;
        }
        if (isMovable(world[newX][newY])) {
            world[newX][newY] = avatar;
            world[x][y] = Tileset.FLOOR;
            x = newX;
            y = newY;
            return true;
        }
        return false;
    }

    private boolean isMovable(TETile tile) {
        return tile == Tileset.FLOOR;
    }
}
