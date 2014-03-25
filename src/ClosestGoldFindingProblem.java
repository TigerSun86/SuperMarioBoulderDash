import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClosestGoldFindingProblem extends UCSProblem {
    /* Constant begin **************************************** */
    public static final int COST_ONE = 1;
    public static final int COST_ROCK = 100;
    public static final int COST_INFINITE = 1000000;
    public static final HashMap<Character, Integer> COST_MAP =
            new HashMap<Character, Integer>();
    static {
        initCostMap();
    }

    private static void initCostMap () {
        COST_MAP.put(GameSimulation.MINER, COST_ONE);
        COST_MAP.put(GameSimulation.EARTH, COST_ONE);
        COST_MAP.put(GameSimulation.EMPTY, COST_ONE);
        COST_MAP.put(GameSimulation.OPEN, COST_ONE);
        COST_MAP.put(GameSimulation.GOLD, COST_ONE);
        COST_MAP.put(GameSimulation.ROCK, COST_ROCK);
        COST_MAP.put(GameSimulation.LIFT, COST_INFINITE);
        COST_MAP.put(GameSimulation.WALL, COST_INFINITE);
    }

    private static final char DIRECT_UP = 'U';
    private static final char DIRECT_DOWN = 'D';
    private static final char DIRECT_LEFT = 'L';
    private static final char DIRECT_RIGHT = 'R';
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
    }

    /* Constant end **************************************** */

    // The "state" is the position in the map.
    private final Point initialPos;
    private final char[][] map;

    public ClosestGoldFindingProblem(final Point initialPos2,
            final char[][] map2) {
        this.initialPos = initialPos2;
        this.map = map2;

    }

    @Override
    public final Object getInitialState () {
        return initialPos;
    }

    @Override
    public final List<Successor> getSuccessors (final Object state) {
        final Point curPos = (Point) state;
        final ArrayList<Successor> result = new ArrayList<Successor>();
        for (char direct : DIRECT_MAP.keySet()) {
            // Get the new position by current position and direct
            final Point action = DIRECT_MAP.get(direct);
            final Point newPos = new Point(curPos);
            newPos.move(curPos.x + action.x, curPos.y + action.y);

            // Check whether the new position is out of bound of map.
            if (newPos.x >= 0 && newPos.x < map.length && newPos.y >= 0
                    && newPos.y < map[0].length) {
                // Should not go through the wall and closed lift
                if (map[newPos.x][newPos.y] != GameSimulation.WALL
                        && map[newPos.x][newPos.y] != GameSimulation.LIFT) {
                    // It's a valid new position, add it into result.
                    result.add(new Successor(newPos, action));
                }
            }
        }
        return result;
    }

    @Override
    public final boolean isGoalState (final Object state) {
        final Point pos = (Point) state;
        final char spot = map[pos.x][pos.y];
        // Either the gold or open lift found is the goal.
        return (spot == GameSimulation.GOLD || spot == GameSimulation.OPEN);
    }

    @Override
    public final double getStepCost (final Object srcState,
            final Object desState, final Object action) {
        final Point pos = (Point) desState;
        return (double) COST_MAP.get(map[pos.x][pos.y]);
    }
}
