package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.princeton.cs.introcs.StdDraw;
import edu.princeton.cs.algs4.WeightedQuickUnionUF;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private State currentState;
    private long seed;
    private Random rand;
    private TETile[][] world;
    private CharacterTile player;
    private CharacterTile[] enemies;
    private String headsUpText;

    private enum State { MAIN_MENU, SEED_INPUT, IN_GAME; }

    public Engine() {
        seed = 0L;
        currentState = State.MAIN_MENU;
        mainMenu();
    }

    private void generateWorld() {
        world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        int numRooms = RandomUtils.uniform(rand, 5, 20);
        List<Room> rooms = new ArrayList<>();
        while (rooms.size() < numRooms) {
            Room newRoom = generateRandomRoom(rand);
            if (!newRoom.intersectsAny(rooms)) {
                rooms.add(newRoom);
                newRoom.drawRoom(world);
            }
        }
        WeightedQuickUnionUF weighted = new WeightedQuickUnionUF(rooms.size());
        while (weighted.count() > 1) {
            int roomOne = RandomUtils.uniform(rand, rooms.size());
            int roomTwo = RandomUtils.uniform(rand, rooms.size());
            if (roomOne != roomTwo && !weighted.connected(roomOne, roomTwo)) {
                weighted.union(roomOne, roomTwo);
                Room r = rooms.get(roomOne);
                r.connect(world, rooms.get(roomTwo), rand);
            }
        }
        Room playerRoom = rooms.get(RandomUtils.uniform(rand, rooms.size()));
        player = playerRoom.randomSpawn(rand, Tileset.AVATAR);
        world[player.getX()][player.getY()] = Tileset.AVATAR;

        enemies = new CharacterTile[3];
        for (int i = 0; i < 3; i++) {
            Room enemyRoom = rooms.get(RandomUtils.uniform(rand, rooms.size()));
            if (enemyRoom == playerRoom) {
                i--;
                continue;
            }
            enemies[i] = enemyRoom.randomSpawn(rand, Tileset.ENEMY);
            world[enemies[i].getX()][enemies[i].getY()] = Tileset.ENEMY;
        }
    }

    private Room generateRandomRoom(Random rand) {
        int width = RandomUtils.uniform(rand, 3, 10);
        int height = RandomUtils.uniform(rand, 3, 10);
        int x = RandomUtils.uniform(rand, 1, WIDTH - width - 1);
        int y = RandomUtils.uniform(rand, 1, HEIGHT - height - 1);
        return new Room(x, y, width, height);
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                inputCharacter(c);
            }
            if (world != null) {
                int xTile = Math.round((int) StdDraw.mouseX());
                int yTile = Math.round((int) StdDraw.mouseY());
                if (xTile >= 0 && xTile < Engine.WIDTH && yTile >= 0 && yTile < Engine.HEIGHT) {
                    TETile tile = world[xTile][yTile];
                    if (tile != null) {
                        headsUpText = tile.description();
                    } else {
                        headsUpText = null;
                    }
                } else {
                    headsUpText = null;
                }
                render();
            }
        }
    }
    public void mainMenu() {
       StdDraw.setCanvasSize(512, 512);
       Font font = new Font("Arial", Font.BOLD, 40);
       StdDraw.setFont(font);
       StdDraw.text(0.5, 0.85, "CS61B: The Game");
       Font font1 = new Font("Arial", Font.PLAIN, 24);
       StdDraw.setFont(font1);
       StdDraw.text(0.5, 0.55, "New Game [N]");
        StdDraw.text(0.5, 0.5, "Load Game [L]");
        StdDraw.text(0.5, 0.45, "Quit [:Q]");
    }
    public void seedMenu() {
        StdDraw.clear();
        Font font = new Font("Arial", Font.BOLD, 40);
        StdDraw.setFont(font);
        StdDraw.text(0.5, 0.60, "Enter Seed Number");
        Font font1 = new Font("Arial", Font.ITALIC, 36);
        StdDraw.setFont(font1);
        if (seed != 0) {
            StdDraw.text(0.5, 0.5, Long.toString(seed));
        }
        StdDraw.setFont(font);
        StdDraw.text(0.5, 0.40, "Press [S] when done");
    }

    private void render() {
        ter.renderFrame(world);
        if (headsUpText != null) {
            StdDraw.setPenColor(255, 255, 255);
            StdDraw.textLeft(0.1, HEIGHT - 1, headsUpText);
        }
        StdDraw.show();
    }

    private void moveEnemies() {
        for (int i = 0; i < enemies.length; i++) {
            enemies[i].moveTowards(world, player);
        }
    }

    private void showEnemyPaths() {
        for (int i = 0; i < enemies.length; i++) {
            List<Point> path = enemies[i].findPathTowards(world, player.getX(), player.getY());
            if (path == null) {
                continue;
            }
            for (int j = 0; j < path.size() - 1; j++) {
                Point p = path.get(j);
                if (world[p.x][p.y] == Tileset.FLOOR) {
                    world[p.x][p.y] = Tileset.ENEMY_PATH;
                }
            }
        }
    }

    private void hideEnemyPaths() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (world[x][y] == Tileset.ENEMY_PATH) {
                    world[x][y] = Tileset.FLOOR;
                }
            }
        }
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        for (int i = 0; i < input.length(); i++) {
            inputCharacter(input.charAt(i));
        }
        return world;
    }


    private void inputCharacter(char c) {
        System.out.print(c);
        c = Character.toLowerCase(c);
        if (currentState == State.MAIN_MENU) {
            if (c == 'n') {
                currentState = State.SEED_INPUT;
                seedMenu();
            }
        } else if (currentState == State.SEED_INPUT) {
            if (Character.isDigit(c)) {
                int digit = Character.getNumericValue(c);
                seed = seed * 10 + digit;
                seedMenu();
            } else if (c == 's') {
                rand = new Random(seed);
                generateWorld();
                currentState = State.IN_GAME;
                ter.initialize(Engine.WIDTH, Engine.HEIGHT);
                render();
            }
        } else {
            hideEnemyPaths();
            if (c == 'w') {
                moveEnemies();
                player.moveUp(world);
            } else if (c == 'a') {
                moveEnemies();
                player.moveLeft(world);
            } else if (c == 's') {
                moveEnemies();
                player.moveDown(world);
            } else if (c == 'd') {
                moveEnemies();
                player.moveRight(world);
            } else if (c == 'e') {
                showEnemyPaths();
            }
            render();
        }
    }
}
