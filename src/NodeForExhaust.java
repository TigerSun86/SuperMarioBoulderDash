import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;

public class NodeForExhaust implements Comparable<NodeForExhaust>, Cloneable {
    // Change INVALID_UTILITY must also change the utility judge method.
    private static final double INVALID_UTILITY = Double.NaN;

    private GameSimulation gameS;
    private char ancestorAction;
    private final Point initPos;
    private int depth;
    private boolean willDie;
    private double utility;

    /* Public Begin ******************************************* */
    public NodeForExhaust(final GameSimulation initGame, final int d) {
        this.gameS = (GameSimulation) initGame.clone();
        // Initial gameS has no action before.
        this.ancestorAction = '\0';
        this.initPos = new Point(initGame.getCurX(), initGame.getCurY());
        this.depth = d;
        this.utility = INVALID_UTILITY;
    }

    public final GameSimulation getGame () {
        return gameS;
    }

    public final void setAncestorAction (final char action) {
        ancestorAction = action;
        willDie = isDefinitDeath();
    }

    public final char getAncestorAction () {
        return ancestorAction;
    }

    public final void performAction (final char action) {
        gameS.play(action);
    }

    public final void depthReduce () {
        depth--;
    }

    public final boolean isEnd () {
        return (gameS.getGameState() != GameSimulation.STATE_RUNNING)
                || (depth == 0);
    }

    public final double getUtility () {
        // If haven't calculated the utility, then calculate it.
        if (Double.isNaN(utility)) {
            utility = calUtility();
        }
        return utility;
    }

    public final Point getMovedPos (final Point pos, final char direct) {
        final Point action = DIRECT_MAP.get(direct);
        final Point newPos = new Point(pos.x + action.x, pos.y + action.y);
        if (gameS.isInBound(newPos)) {
            return newPos;
        } else {
            return null;
        }
    }

    @Override
    public final int compareTo (final NodeForExhaust anotherState) {
        return Double.compare(this.getUtility(), anotherState.getUtility());
    }

    @Override
    public final Object clone () {
        NodeForExhaust o = null;
        try {
            o = (NodeForExhaust) super.clone();
        } catch (final CloneNotSupportedException e) {
            e.printStackTrace();
        }
        o.utility = INVALID_UTILITY;
        o.gameS = (GameSimulation) this.gameS.clone();
        return o;
    }

    /* Public End ******************************************* */

    private boolean isDefinitDeath () {
        final Point posAbove = getMovedPos(initPos, DIRECT_UP);
        if (posAbove != null) {
            final char spot = gameS.getSpot(posAbove.x, posAbove.y);
            if (spot == GameSimulation.ROCK
                    && (ancestorAction == 'U' || ancestorAction == 'W')) {
                return true;
            }
        }
        return false;
    }

    private int getShortestDistanceToGold () {
        final Point minerPos = new Point(gameS.getCurX(), gameS.getCurY());
        final ClosestGoldFindingProblem closestGold =
                new ClosestGoldFindingProblem(minerPos, gameS.getMap());
        final Node result = GraphSearch.search(closestGold);
        assert result != null;
        return (int) result.getPathCost();
    }

    private int oneRockAbove (final Point pos) {
        int result = 0;
        Point posAbove = getMovedPos(pos, DIRECT_UP);

        while (posAbove != null) {
            final char spot = gameS.getSpot(posAbove.x, posAbove.y);
            if (spot == GameSimulation.ROCK) {
                result = 1;
                break;
            } else if (spot != GameSimulation.EMPTY) {
                // It's not empty above the gold
                result = 0;
                break;
            } else {
                // Check the above position.
                posAbove = getMovedPos(posAbove, DIRECT_UP);
            }
        }
        return result;
    }

    private int getKeyRockNum () {
        int keyRockNum = 0;
        final HashSet<Point> goldSet = gameS.getGoldPosSet();
        for (Point goldPos : goldSet) {
            final Point posAboveGold = new Point(goldPos);
            keyRockNum += oneRockAbove(posAboveGold);
        }
        final Point liftPos = gameS.getLiftPos();
        keyRockNum += oneRockAbove(liftPos);
        return keyRockNum;
    }

    private static final char DIRECT_UP = 'U';
    private static final char DIRECT_DOWN = 'D';
    private static final char DIRECT_LEFT = 'L';
    private static final char DIRECT_RIGHT = 'R';
    private static final char DIRECT_NONE = 'W';
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
        DIRECT_MAP.put(DIRECT_NONE, new Point(0, 0));
    }

    private int getTimesOfBeingThePos () {
        if (ancestorAction == 'A') {
            return 0;
        }
        final Point nextPos = getMovedPos(initPos, ancestorAction);
        if (nextPos != null) {
            final int[][] recordMap = ExhaustiveMiner.MOVES_RECORD.get(0);
            return recordMap[nextPos.x][nextPos.y];
        } else {
            return 0;
        }
    }

    private double getStayPunish () {
        final int times = getTimesOfBeingThePos();
        // At first miner can move around, but after several times being this
        // position, it will get huge punishment.
        return UTI_PUNISH_STAY_BASE * times * times * times * times;
    }

    // Even haven't touched the gold, still can smell it when get closer and
    // closer :P But the smell shouldn't be bigger than actually got a gold.
    private static final double UTI_BONUS_POTENTIAL_GOLD = 10;
    private static final double UTI_BONUS_GOTTEN_GOLD = 25;
    private static final double UTI_BONUS_WIN = 1000;
    // This should not be big, otherwise the leaf moves will be all abort.
    private static final double UTI_BONUS_ABORT = 0.1;

    private static final double UTI_PUNISH_LOSE = -1000;
    // Aborting for no reason will be punished.
    private static final double UTI_PUNISH_ABORT = -1000;
    private static final double UTI_PUNISH_MOVES = -0.00001;
    // This is kind of "Rescue gold bonus", should be larger than
    // UTI_BONUS_GOTTEN_GOLD, so miner will be more interest on rescuing gold
    // than catching gold as fast as it can.
    private static final double UTI_PUNISH_ROCK_ABOVE = -50;
    // Should be much smaller than UTI_PUNISH_MOVES, otherwise it will disturb
    // the move.
    private static final double UTI_PUNISH_STAY_BASE = -0.000001;

    private double calUtility () {
        double uti = 0;
        // The more moves made, the more punish on utility.
        uti += gameS.getNumOfMoves() * UTI_PUNISH_MOVES;

        // The more golds gotten, the more bonus on utility.
        uti += gameS.getGoldCollected() * UTI_BONUS_GOTTEN_GOLD;

        uti += getKeyRockNum() * UTI_PUNISH_ROCK_ABOVE;

        uti += getStayPunish();

        if (gameS.getGameState() == GameSimulation.STATE_RUNNING) {
            final int distance = getShortestDistanceToGold();
            assert distance != 0;
            if (distance < ClosestGoldFindingProblem.COST_ROCK) {
                // The shorter distance to gold, the more bonus on utility.
                uti += ((double) UTI_BONUS_POTENTIAL_GOLD / distance);
            }
        } else if (gameS.getGameState() == GameSimulation.STATE_ABORTED) {
            final int distance = getShortestDistanceToGold();
            assert distance != 0;
            if (distance < ClosestGoldFindingProblem.COST_ROCK) {
                // Don't abort if there still is gold can achieve.
                uti += UTI_PUNISH_ABORT;
            } else {
                uti += UTI_BONUS_ABORT;
            }
        } else if (gameS.getGameState() == GameSimulation.STATE_WINNING) {
            uti += UTI_BONUS_WIN;
        } else { // if (gameS.getGameState() == GameSimulation.STATE_FAILED) {
            uti += UTI_PUNISH_LOSE;
        }

        if (willDie) {
            uti += UTI_PUNISH_LOSE;
        }

        return uti;
    }
}
