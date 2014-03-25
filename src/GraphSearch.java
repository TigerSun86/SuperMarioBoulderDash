import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

public class GraphSearch {
    public static final Node search (final Problem problem) {
        final PriorityQueue<Node> frontier = new PriorityQueue<Node>();
        final HashSet<Object> explored = new HashSet<Object>();
        frontier.add(new Node(problem.getInitialState()));

        while (!frontier.isEmpty()) {
            final Node node = frontier.remove();
            if (problem.isGoalState(node.getState())) {
                return node;
            }
            explored.add(node.getState());

            expandNode(problem, node, frontier, explored);
        }
        return null;
    }

    /* for each action in problem.ACTIONS(node.STATE) do
     * child<-CHILD-NODE(problem, node, action)
     * if child.STATE is not in explored or frontier then
     * frontier<-INSERT(child, frontier)
     * else if child.STATE is in frontier with higher PATH-COST then
     * replace that frontier node with child */
    private static void expandNode (final Problem problem, final Node node,
            final PriorityQueue<Node> frontier, final HashSet<Object> explored) {
        // Get all successors of the node.
        final List<Successor> successorSet =
                problem.getSuccessors(node.getState());
        for (Successor successor : successorSet) {
            if (!explored.contains(successor.getState())) {
                /* The successor has not been explored.
                 * Then check whether the successor is in the frontier. */
                Node nodeInFrontier = null;
                for (Node fNode : frontier) {
                    if (fNode.getState().equals(successor.getState())) {
                        // The successor has already existed in frontier.
                        nodeInFrontier = fNode;
                        break;
                    }
                }
                // Generate child node.
                final Node childNode =
                        generateChildNode(problem, node, successor);

                if (nodeInFrontier == null) {
                    // The successor is not in frontier nor explored, so add it.
                    frontier.add(childNode);
                } else if (Double.compare(childNode.getTotalCost(),
                        nodeInFrontier.getTotalCost()) < 0) {
                    /* The successor has lower cost than the same one in the
                     * frontier, so replace it. */
                    frontier.remove(nodeInFrontier);
                    frontier.add(childNode);
                }
            } // if (!explored.contains(successorState)) {
        } // for (Successor successor : successorSet) {
    }

    private static Node generateChildNode (final Problem problem,
            final Node parent, final Successor successor) {
        // Set childNode's state, parent, depth
        final Node childNode = new Node(successor.getState(), parent);

        // Set childNode's action
        childNode.setAction(successor.getAction());

        /* Set cost for node sorting.
         * If it's not AStar search, heuristic cost is zero. */
        final double stepCost =
                problem.getStepCost(parent.getState(), successor.getState(),
                        successor.getAction());
        final double heuristicCost =
                problem.getHeuristicCost(successor.getState());
        // Set childNode's step cost, path cost, total cost.
        childNode.setCost(stepCost, heuristicCost);
        return childNode;
    }
}
