/**
 * Author: Xunhu Sun, sunx2013@my.fit.edu
 * Author: Zachary McHenry, zmchenry2011@my.fit.edu
 * Course: CSE 4051, Fall 2013
 * Project: proj09, Lambda Lift Agent
 * Date: Dec/01/2013
 * Describe: An Agent which can play boulder dash game automatically.
 * */

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ExhaustiveMiner {
    private static final int SEARCH_DEPTH = 5;
    private static final ArrayList<Character> ACTION_SET =
            new ArrayList<Character>();
    static {
        ACTION_SET.add('U');
        ACTION_SET.add('D');
        ACTION_SET.add('L');
        ACTION_SET.add('R');
        ACTION_SET.add('W');
        ACTION_SET.add('A');
    }
    public static final ArrayList<int[][]> MOVES_RECORD =
            new ArrayList<int[][]>();

    private static void initMovesRecord (final char[][] map) {
        if (MOVES_RECORD.isEmpty()) {
            final int[][] recordMap = new int[map.length][map[0].length];
            for (int i = 0; i < map.length; i++) {
                Arrays.fill(recordMap[i], 0);
            }
            MOVES_RECORD.add(recordMap);
        }
    }

    public static final char next (final int row, final int col,
            final char[][] map) {
        initMovesRecord(map);
        final GameSimulation initGame = new GameSimulation(map);
        final NodeForExhaust initNode =
                new NodeForExhaust(initGame, SEARCH_DEPTH);
        final ArrayList<NodeForExhaust> resultSet =
                new ArrayList<NodeForExhaust>();
        // Use stack because depth first search will be space efficient.
        final ArrayDeque<NodeForExhaust> frontierStack =
                new ArrayDeque<NodeForExhaust>();

        for (char action : ACTION_SET) {
            final NodeForExhaust firstStepNode = getSuccessor(initNode, action);
            if (firstStepNode != null) {
                firstStepNode.setAncestorAction(action);
                frontierStack.push(firstStepNode);
            }
        }

        while (!frontierStack.isEmpty()) {
            final NodeForExhaust curNode = frontierStack.pop();
            if (curNode.isEnd()) {
                resultSet.add(curNode);
            } else {
                // Get the successor of current node.
                for (char action : ACTION_SET) {
                    final NodeForExhaust nextStepNode =
                            getSuccessor(curNode, action);
                    if (nextStepNode != null) {
                        frontierStack.push(nextStepNode);
                    }
                }
            } // if (curState.isEnd()) {
        } // while (!frontierStack.isEmpty()) {

        final NodeForExhaust bestNode = Collections.max(resultSet, null);
        updateRecordMap(bestNode, row, col);
        return bestNode.getAncestorAction();
    }

    private static void updateRecordMap (final NodeForExhaust bestNode,
            final int row, final int col) {
        if (bestNode.getAncestorAction() == 'A') {
            // Do not record the position of abort.
            return;
        }
        final Point initPos = new Point(row, col);
        final Point nextPos =
                bestNode.getMovedPos(initPos, bestNode.getAncestorAction());
        if (nextPos != null) {
            final int[][] recordMap = MOVES_RECORD.get(0);
            recordMap[nextPos.x][nextPos.y]++;
        }
    }

    private static NodeForExhaust getSuccessor (final NodeForExhaust parent,
            final char action) {
        if (parent.getGame().isValidMove(action)) {
            final NodeForExhaust child = (NodeForExhaust) parent.clone();
            child.performAction(action);
            child.depthReduce();
            return child;
        } else {
            // It's a invalid action, don't count on it.
            return null;
        }
    }
}
