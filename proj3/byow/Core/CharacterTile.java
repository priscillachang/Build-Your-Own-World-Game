package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.proj2ab.ArrayHeapMinPQ;
import byow.proj2ab.ExtrinsicMinPQ;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.awt.Point;

public class CharacterTile {
    private int x;
    private int y;
    private TETile avatar;

    private enum Direction { NORTH, SOUTH, EAST, WEST; }

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

    public List<Point> findPathTowards(TETile[][] world, int targetX, int targetY) {
        Map<Point, Integer> distTo = new HashMap<>();
        Map<Point, Point> edgeTo = new HashMap<>();
        ExtrinsicMinPQ<Point> fringe = new ArrayHeapMinPQ<>();
        Point source = new Point(x, y);
        Point target = new Point(targetX, targetY);
        fringe.add(source, 0.0);
        distTo.put(source, 0);

        while (fringe.size() > 0 && !fringe.getSmallest().equals(target)) {
            Point current = fringe.removeSmallest();
            int distance = distTo.get(current);
            for (Direction d : Direction.values()) {
                Point to = getPoint(current, d);
                if (isMovable(world[(int) to.getX()][(int) to.getY()])
                        && (!distTo.containsKey(to) || distTo.get(to) > distance + 1)) {
                    distTo.put(to, distance + 1);
                    edgeTo.put(to, current);
                    int heuristic = manhattanDistance(to, target);
                    int priority = distance + 1 + heuristic;
                    if (fringe.contains(to)) {
                        fringe.changePriority(to, priority);
                    } else {
                        fringe.add(to, priority);
                    }
                }
            }
        }
        if (fringe.size() == 0) {
            return null;
        }
        List<Point> path = new ArrayList<>();
        Point current = target;
        while (!current.equals(source)) {
            path.add(current);
            current = edgeTo.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    public boolean moveTowards(TETile[][] world, int targetX, int targetY) {
        List<Point> path = findPathTowards(world, targetX, targetY);
        if (path == null || path.size() == 0) {
            return false;
        }
        Point next = path.get(0);
        return moveTo(world, (int) next.getX(), (int) next.getY());
    }

    public TETile getAvatar() {
        return avatar;
    }

    private Point getPoint(Point current, Direction direction) {
        if (direction == Direction.NORTH) {
            return new Point((int) current.getX(), (int) current.getY() + 1);
        } else if (direction == Direction.SOUTH) {
            return new Point((int) current.getX(), (int) current.getY() - 1);
        } else if (direction == Direction.EAST) {
            return new Point((int) current.getX() + 1, (int) current.getY());
        } else {
            return new Point((int) current.getX() - 1, (int) current.getY());
        }
    }

    private static int manhattanDistance(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private boolean isMovable(TETile tile) {
        if (avatar == Tileset.ENEMY) {
            return tile != Tileset.WALL && tile != Tileset.ITEM && tile != Tileset.ENEMY;
        }
        else {
            return tile != Tileset.WALL && tile != Tileset.AVATAR && tile != Tileset.FLOWER;
        }
    }
}
