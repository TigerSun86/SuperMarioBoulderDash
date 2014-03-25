import java.awt.Point;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class GameSimulation implements Cloneable {
    private final int height;
    private final int width;
    private char[][] map;
    // Record which rock is falling down
    private boolean[][] isFalling;
    private HashSet<Point> goldSet = new HashSet<Point>();
    private Point liftPos;
    private Point minerPos;
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

    public GameSimulation(final char[][] newMap) {
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
                    minerPos = new Point(i, j);
                } else if (map[i][j] == GOLD) {
                    maxGold++;
                    // Record gold position
                    goldSet.add(new Point(i, j));
                } else if (map[i][j] == LIFT || map[i][j] == OPEN) {
                    liftPos = new Point(i, j);
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

    public final HashSet<Point> getGoldPosSet () {
        return goldSet;
    }

    public final Point getLiftPos () {
        return liftPos;
    }

    public final Point getMinerPos () {
        return minerPos;
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
        return minerPos.x;
    }

    public final int getCurY () {
        return minerPos.y;
    }

    public final char[][] getMap () {
        return map;
    }

    public final boolean isInBound (final Point pos) {
        return (pos.x >= 0 && pos.x < width && pos.y >= 0 && pos.y < height);
    }

    public final boolean isValidMove (final char action) {
        final char direct = getDirectByAction(action);
        if (direct == '\0') {
            // It's a invalid action.
            return false;
        }

        final Point newPos = new Point(minerPos);
        movePos(newPos, direct);
        if (!isInBound(newPos)) {
            // This move is out of bound of map.
            return false;
        }

        final char spot = map[newPos.x][newPos.y];
        if (spot == WALL || spot == LIFT) {
            // Can not go through the wall nor the closed lift.
            return false;
        }

        if (spot == ROCK && (direct == DIRECT_RIGHT || direct == DIRECT_LEFT)) {
            final Point posBehindRock = new Point(newPos);
            movePos(posBehindRock, direct);
            if (!isInBound(newPos)) {
                /* Rock is at the bound of map, so rock cannot be pushed, so
                 * this move is invalid. */
                return false;
            } else {
                final char spotBehindRock =
                        map[posBehindRock.x][posBehindRock.y];
                /* The spot behind rock is empty, so rock can be pushed, so
                 * this move is valid. Otherwise it's invalid. */
                return (spotBehindRock == EMPTY);
            }
        } // if (spot == ROCK && (direct == DIRECT_RIGHT ||

        // Other situations is valid.
        return true;
    }

    public final void setAudio (final AudioSwitch as) {
        audioSwitch = as;
        // Play theme from begining
        audioSwitch.turnOn(AudioSwitch.THEME);
    }

    @Override
    public final Object clone () {
        GameSimulation o = null;
        try {
            o = (GameSimulation) super.clone();
        } catch (final CloneNotSupportedException e) {
            e.printStackTrace();
        }
        o.map = new char[width][height];
        o.isFalling = new boolean[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                o.map[i][j] = this.map[i][j];
                o.isFalling[i][j] = this.isFalling[i][j];
            }
        }
        o.minerPos = new Point(this.minerPos);

        return o;
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
            case ACTION_WAIT:
                direct = DIRECT_NONE;
                break;
            case ACTION_ABORT:
                direct = DIRECT_NONE;
                break;
            default:
                // Invalid action.
                direct = '\0';
        }
        return direct;
    }

    private void movePos (final Point pos, final char direct) {
        // Move the point along the direct
        final Point action = DIRECT_MAP.get(direct);
        pos.move(pos.x + action.x, pos.y + action.y);
    }

    private char getNewSpot (final Point pos, final char direct) {
        final Point newPos = new Point(pos);
        movePos(newPos, direct);
        if (isInBound(newPos)) {
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
        final char item = getNewSpot(pos, DIRECT_NONE);
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
            // If score is too low, then gameSt over
            gameOver(STATE_FAILED);
        }
    }

    private void gameOver (final int stateType) {
        gameState = stateType;
        // GameSt over, so stop the theme music
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
        if (direct == '\0') {
            // Invalid action regards as wait.
            direct = DIRECT_NONE;
        }
        char newSpot = getNewSpot(minerPos, direct);
        if (newSpot == 0) {
            // if move is illegal, regard this action as wait
            direct = getDirectByAction(ACTION_WAIT);
            newSpot = getNewSpot(minerPos, direct);
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
            final Point rockPos = new Point(minerPos);
            movePos(rockPos, direct);
            final char newRockSpot = getNewSpot(rockPos, direct);
            if (newRockSpot == EMPTY) {
                // Move rock to the next spot
                moveSpot(map, rockPos, direct);
                turnOnAudio(AudioSwitch.ROCK);
                isValidAction = true;
            }
        }

        if (isValidAction) {
            // Move miner to new spot
            moveSpot(map, minerPos, direct);
            // Update current position of miner
            movePos(minerPos, direct);
        }
        // update moves
        numOfMoves++;
    }

    /* update begin *********************************************** */
    private boolean fallDownTest (final Point pos) {
        // Spot under the rock is empty, rock will fall down
        final char spotDown = getNewSpot(pos, DIRECT_DOWN);
        return spotDown == EMPTY;
    }

    private boolean slideRightDownTest (final Point pos) {
        final char spotDown = getNewSpot(pos, DIRECT_DOWN);
        final char spotRight = getNewSpot(pos, DIRECT_RIGHT);
        final char spotRightDown = getNewSpot(pos, DIRECT_RIGHTDOWN);
        return ((spotDown == ROCK) || (spotDown == GOLD))
                && (spotRight == EMPTY) && (spotRightDown == EMPTY);
    }

    private boolean slideLeftDownTest (final Point pos) {
        final char spotDown = getNewSpot(pos, DIRECT_DOWN);
        final char spotRight = getNewSpot(pos, DIRECT_RIGHT);
        final char spotRightDown = getNewSpot(pos, DIRECT_RIGHTDOWN);
        final char spotLeft = getNewSpot(pos, DIRECT_LEFT);
        final char spotLeftDown = getNewSpot(pos, DIRECT_LEFTDOWN);
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
        return (getNewSpot(pos, DIRECT_DOWN) == MINER)
                && (isFalling[pos.x][pos.y]);
    }

    private void updateCurSpot (final char[][] newMap,
            final boolean[][] newIsFalling, final Point pos) {
        final char curSpot = getNewSpot(pos, DIRECT_NONE);
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
                // The miner is crushed. So do not move rock, and gameSt over.
                setSpot(newMap, pos, DIRECT_NONE, curSpot);
                isRockMoved = false;
                gameOver(STATE_FAILED);
            } else {
                // Rock do not move
                setSpot(newMap, pos, DIRECT_NONE, curSpot);
                isRockMoved = false;
            } // if (fallDownTest(minerPos)) {

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
