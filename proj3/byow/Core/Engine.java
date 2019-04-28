package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

    private enum State { MAIN_MENU, SEED_INPUT, IN_GAME; }

    public Engine() {
        seed = 0L;
        currentState = State.MAIN_MENU;
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
        input = input.toLowerCase();
        int begin = input.indexOf("n") + 1;
        int end = input.indexOf("s");
        long seed = Long.parseLong(input.substring(begin, end));

        TETile[][] finalWorldFrame = generateWorld(seed);
        return finalWorldFrame;
    }


    private void inputCharacter(char c) {
        c = Character.toLowerCase(c);
        if (currentState == State.MAIN_MENU) {
            if (c == 'n') {
                currentState = State.SEED_INPUT;
            }
        } else if (currentState == State.SEED_INPUT) {
            if (Character.isDigit(c)) {
                int digit = Character.getNumericValue(c);
                seed = seed * 10 + digit;
            } else if (c == 's') {
                rand = new Random(seed);
                generateWorld();
                currentState = State.IN_GAME;
            }
        } else {
            if (c == 'w') {
                player.moveUp(world);
            } else if (c == 'a') {
                player.moveLeft(world);
            } else if (c == 's') {
                player.moveDown(world);
            } else if (c == 'd') {
                player.moveRight(world);
            }
            ter.renderFrame(world);
        }
    }
}
