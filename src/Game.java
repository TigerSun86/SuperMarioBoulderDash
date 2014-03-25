import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Game {
    private char[][] map;
    // Record which rock is falling down
    private boolean[][] isFalling;
    private final int height;
    private final int width;
    private Point curPos;
    private int score;
    private int goldCollected;
    private int maxGold;
    private int numOfMoves;
    private int gameState;
    private AudioSwitch audioSwitch = null;

    private static final int MIN_SCORE = -1000;
    private static final int GOLD_AWARD = 25;
    private static final int ABORT_AWARD = 25;
    private static final int WIN_AWARD = 50;
    /* Public method begin ******************************************** */
    public static final int STATE_RUNNING = 0;
    public static final int STATE_WINNING = 1;
    public static final int STATE_FAILED = 2;
    public static final int STATE_ABORTED = 3;

    public static final char MINER = 'R';
    public static final char WALL = '#';
    public static final char ROCK = '*';
    public static final char GOLD = '\\';
    public static final char LIFT = 'L';
    public static final char OPEN = 'O';
    public static final char EARTH = '.';
    public static final char EMPTY = ' ';

    // Holds integers that map to images
    public Game(final ArrayList<String> mapList) {
        maxGold = 0;
        // This is m
        height = mapList.size();
        // Get width of the map
        int maxWidth = 0;
        for (int i = 0; i < mapList.size(); i++) {
            if (mapList.get(i).length() > maxWidth) {
                maxWidth = mapList.get(i).length();
            }
        }
        // This is n
        width = maxWidth;

        /* Inversely store as the task requirement. But the the point (1, m)
         * becomes (0, m-1). */
        map = new char[width][height];
        for (int i = 0; i < width; i++) {
            // Make all spot default to empty spot first
            Arrays.fill(map[i], EMPTY);
        }
        // Read input map
        for (int y = height - 1; y >= 0; y--) {
            final String oneLine = mapList.get(height - 1 - y);
            for (int x = 0; x < oneLine.length(); x++) {
                map[x][y] = oneLine.charAt(x);
                if (map[x][y] == MINER) {
                    curPos = new Point(x, y);
                } else if (map[x][y] == GOLD) {
                    maxGold++;
                }
            }
        }

        isFalling = new boolean[width][height];
        for (int i = 0; i < width; i++) {
            // All rocks default state is not falling down.
            Arrays.fill(isFalling[i], false);
        }
        score = 0;
        goldCollected = 0;
        numOfMoves = 0;
        gameState = STATE_RUNNING;
    }

    public Game(final char[][] newMap) {
        maxGold = 0;
        // This is m
        height = newMap[0].length;
        // This is n
        width = newMap.length;

        /* Inversely store as the task requirement. But the the point (1, m)
         * becomes (0, m-1). */
        map = new char[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                map[i][j] = newMap[i][j];
                if (map[i][j] == MINER) {
                    curPos = new Point(i, j);
                } else if (map[i][j] == GOLD) {
                    maxGold++;
                }
            }
        }

        isFalling = new boolean[width][height];
        for (int i = 0; i < width; i++) {
            // All rocks default state is not falling down.
            Arrays.fill(isFalling[i], false);
        }
        score = 0;
        goldCollected = 0;
        numOfMoves = 0;
        gameState = STATE_RUNNING;
    }

    public final void play (final char next) {
        minerMove(next);
        update();
        updateScore();
    }

    public final char getSpot (final int i, final int j) {
        return map[i][j];
    }

    public final int getHeight () {
        return height;
    }

    public final int getWidth () {
        return width;
    }

    public final int getScore () {
        return score;
    }

    public final int getGoldCollected () {
        return goldCollected;
    }

    public final int getNumOfMoves () {
        return numOfMoves;
    }

    public final int getGameState () {
        return gameState;
    }

    public final int getCurX () {
        return curPos.x;
    }

    public final int getCurY () {
        return curPos.y;
    }

    public final char[][] getMap () {
        return map;
    }

    public final void setAudio (final AudioSwitch as) {
        audioSwitch = as;
        // Play theme from begining
        audioSwitch.turnOn(AudioSwitch.THEME);
    }

    /* Public method end ******************************************** */

    private static final char ACTION_ABORT = 'A';
    private static final char ACTION_UP = 'U';
    private static final char ACTION_DOWN = 'D';
    private static final char ACTION_LEFT = 'L';
    private static final char ACTION_RIGHT = 'R';
    private static final char ACTION_WAIT = 'W';

    private static final char DIRECT_UP = 'U';
    private static final char DIRECT_DOWN = 'D';
    private static final char DIRECT_LEFT = 'L';
    private static final char DIRECT_RIGHT = 'R';
    private static final char DIRECT_LEFTDOWN = '<';
    private static final char DIRECT_RIGHTDOWN = '>';
    private static final char DIRECT_NONE = 'N';
    private static final HashMap<Character, Point> DIRECT_MAP =
            new HashMap<Character, Point>();
    static {
        initDirectMap();
    }

    private static void initDirectMap () {
        DIRECT_MAP.put(DIRECT_UP, new Point(0, 1));
        DIRECT_MAP.put(DIRECT_DOWN, new Point(0, -1));
        DIRECT_MAP.put(DIRECT_LEFT, new Point(-1, 0));
        DIRECT_MAP.put(DIRECT_RIGHT, new Point(1, 0));
        DIRECT_MAP.put(DIRECT_LEFTDOWN, new Point(-1, -1));
        DIRECT_MAP.put(DIRECT_RIGHTDOWN, new Point(1, -1));
        DIRECT_MAP.put(DIRECT_NONE, new Point(0, 0));
    }

    private static char getDirectByAction (final char action) {
        final char direct;
        switch (action) {
            case ACTION_UP:
                direct = DIRECT_UP;
                break;
            case ACTION_DOWN:
                direct = DIRECT_DOWN;
                break;
            case ACTION_LEFT:
                direct = DIRECT_LEFT;
                break;
            case ACTION_RIGHT:
                direct = DIRECT_RIGHT;
                break;
            default:
                direct = DIRECT_NONE;
        }
        return direct;
    }

    private void movePos (final Point pos, final char direct) {
        // Move the point along the direct
        final Point action = DIRECT_MAP.get(direct);
        pos.move(pos.x + action.x, pos.y + action.y);
    }

    private char getSpot (final Point pos, final char direct) {
        final Point newPos = new Point(pos);
        movePos(newPos, direct);
        if (newPos.x >= 0 && newPos.x < width && newPos.y >= 0
                && newPos.y < height) {
            return map[newPos.x][newPos.y];
        } else {
            // ilegal move
            return 0;
        }
    }

    private void setSpot (final char[][] targetMap, final Point pos,
            final char direct, final char item) {
        final Point newPos = new Point(pos);
        movePos(newPos, direct);
        targetMap[newPos.x][newPos.y] = item;
    }

    private void moveSpot (final char[][] targetMap, final Point pos,
            final char direct) {
        final char item = getSpot(pos, DIRECT_NONE);
        // Set target spot
        setSpot(targetMap, pos, direct, item);
        // Empty current spot
        setSpot(targetMap, pos, DIRECT_NONE, EMPTY);

    }

    private void updateScore () {
        // 25 points gained for every Gold collected
        score = goldCollected * GOLD_AWARD;
        // 1 point lost for every move made
        score -= numOfMoves;
        if (gameState == STATE_ABORTED) {
            // 25 extra points per Gold collected on executing Abort
            score += goldCollected * ABORT_AWARD;
        } else if (gameState == STATE_WINNING) {
            // 50 extra points per Gold collected on reaching the winning
            // gameState
            score += goldCollected * WIN_AWARD;
        }

        if (score <= MIN_SCORE) {
            // If score is too low, then game over
            gameOver(STATE_FAILED);
        }
    }

    private void gameOver (final int stateType) {
        gameState = stateType;
        // Game over, so stop the theme music
        turnOffAudio(AudioSwitch.THEME);
        if (stateType == STATE_WINNING) {
            turnOnAudio(AudioSwitch.WIN);
        } else if (stateType == STATE_FAILED) {
            turnOnAudio(AudioSwitch.DIE);
        } else if (stateType == STATE_ABORTED) {
            turnOnAudio(AudioSwitch.ABORT);
        }
    }

    private void minerMove (final char next) {
        if (next == ACTION_ABORT) {
            // update moves
            numOfMoves++;
            gameOver(STATE_ABORTED);
            return;
        }
        // Get the new position of miner.
        char direct = getDirectByAction(next);
        char newSpot = getSpot(curPos, direct);
        if (newSpot == 0) {
            // if move is illegal, regard this action as wait
            direct = getDirectByAction(ACTION_WAIT);
            newSpot = getSpot(curPos, direct);
        }

        boolean isValidAction = false;
        if (newSpot == EMPTY) {
            isValidAction = true;
        } else if (newSpot == EARTH) {
            turnOnAudio(AudioSwitch.EARTH);
            isValidAction = true;
        } else if (newSpot == GOLD) {
            goldCollected++;
            turnOnAudio(AudioSwitch.COIN);
            isValidAction = true;
        } else if (newSpot == OPEN) {
            gameOver(STATE_WINNING);
            isValidAction = true;
        } else if ((newSpot == ROCK)
                && ((direct == DIRECT_RIGHT) || (direct == DIRECT_LEFT))) {
            // Get the spot behind the rock
            final Point rockPos = new Point(curPos);
            movePos(rockPos, direct);
            final char newRockSpot = getSpot(rockPos, direct);
            if (newRockSpot == EMPTY) {
                // Move rock to the next spot
                moveSpot(map, rockPos, direct);
                turnOnAudio(AudioSwitch.ROCK);
                isValidAction = true;
            }
        }

        if (isValidAction) {
            // Move miner to new spot
            moveSpot(map, curPos, direct);
            // Update current position of miner
            movePos(curPos, direct);
        }
        // update moves
        numOfMoves++;
    }

    /* update begin *********************************************** */
    private boolean fallDownTest (final Point pos) {
        // Spot under the rock is empty, rock will fall down
        final char spotDown = getSpot(pos, DIRECT_DOWN);
        return spotDown == EMPTY;
    }

    private boolean slideRightDownTest (final Point pos) {
        final char spotDown = getSpot(pos, DIRECT_DOWN);
        final char spotRight = getSpot(pos, DIRECT_RIGHT);
        final char spotRightDown = getSpot(pos, DIRECT_RIGHTDOWN);
        return ((spotDown == ROCK) || (spotDown == GOLD))
                && (spotRight == EMPTY) && (spotRightDown == EMPTY);
    }

    private boolean slideLeftDownTest (final Point pos) {
        final char spotDown = getSpot(pos, DIRECT_DOWN);
        final char spotRight = getSpot(pos, DIRECT_RIGHT);
        final char spotRightDown = getSpot(pos, DIRECT_RIGHTDOWN);
        final char spotLeft = getSpot(pos, DIRECT_LEFT);
        final char spotLeftDown = getSpot(pos, DIRECT_LEFTDOWN);
        /* Spot under the current rock is rock
         * and either spot right to the current rock is not
         * empty
         * or spot right down to the current rock is not empty
         * and spot left to the current rock is empty
         * and spot left down to the current rock is empty */

        return (spotDown == ROCK)
                && ((spotRight != EMPTY) || (spotRightDown != EMPTY))
                && (spotLeft == EMPTY) && (spotLeftDown == EMPTY);
    }

    private boolean noGoldTest () {
        return maxGold == goldCollected;
    }

    private boolean minerCrushedTest (final Point pos) {
        /* Dillon's rule: if the miner is under the rock and the rock is falling
         * down, the miner is crushed. */
        return (getSpot(pos, DIRECT_DOWN) == MINER)
                && (isFalling[pos.x][pos.y]);
    }

    private void updateCurSpot (final char[][] newMap,
            final boolean[][] newIsFalling, final Point pos) {
        final char curSpot = getSpot(pos, DIRECT_NONE);
        if (curSpot == ROCK) {
            final Point newPosOfRock = new Point(pos);
            boolean isRockMoved = true;
            if (fallDownTest(pos)) {
                // Spot under the rock is empty, rock will fall down
                moveSpot(newMap, pos, DIRECT_DOWN);
                movePos(newPosOfRock, DIRECT_DOWN);
            } else if (slideRightDownTest(pos)) {
                // Rock falls to the right down
                moveSpot(newMap, pos, DIRECT_RIGHTDOWN);
                movePos(newPosOfRock, DIRECT_RIGHTDOWN);
            } else if (slideLeftDownTest(pos)) {
                // Rock falls to the left down
                moveSpot(newMap, pos, DIRECT_LEFTDOWN);
                movePos(newPosOfRock, DIRECT_LEFTDOWN);
            } else if (minerCrushedTest(pos)) {
                // The miner is crushed. So do not move rock, and game over.
                setSpot(newMap, pos, DIRECT_NONE, curSpot);
                isRockMoved = false;
                gameOver(STATE_FAILED);
            } else {
                // Rock do not move
                setSpot(newMap, pos, DIRECT_NONE, curSpot);
                isRockMoved = false;
            } // if (fallDownTest(curPos)) {

            if (isRockMoved) {
                // If the rock is falling down, record it.
                newIsFalling[newPosOfRock.x][newPosOfRock.y] = true;
            }
        } else if (curSpot == LIFT) {
            if (noGoldTest()) {
                // All golds are collected, lift opens
                setSpot(newMap, pos, DIRECT_NONE, OPEN);
            } else {
                // Lift remains
                setSpot(newMap, pos, DIRECT_NONE, curSpot);
            }
        } else {
            // All other item remains
            setSpot(newMap, pos, DIRECT_NONE, curSpot);
        }
    }

    private void update () {
        final char[][] newMap = new char[width][height];
        final boolean[][] newIsFalling = new boolean[width][height];
        // Update works left to right, then bottom to top
        for (int y = 0; y < height; y++) { // bottom to top
            for (int x = 0; x < width; x++) { // left to right
                final Point pos = new Point(x, y);
                updateCurSpot(newMap, newIsFalling, pos);
            }
        }

        // update map
        map = newMap;
        // update rock falling down state
        isFalling = newIsFalling;
    }

    /* update end *********************************************** */

    private void turnOnAudio (final int audioType) {
        if (audioSwitch != null) {
            audioSwitch.turnOn(audioType);
        }
    }

    private void turnOffAudio (final int audioType) {
        if (audioSwitch != null) {
            audioSwitch.turnOff(audioType);
        }
    }
}
