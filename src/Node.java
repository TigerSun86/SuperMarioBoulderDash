public class Node implements Comparable<Node> {
    private final Object state;
    private final Node parent;
    private final int depth;
    private Object action;
    private double pathCost;
    private double heuristicCost;
    private double totalCost;

    public Node(final Object state2) {
        this.state = state2;
        this.parent = null;
        this.depth = 0;
        this.action = null;
        this.pathCost = 0;
        this.heuristicCost = 0;
        this.totalCost = 0;
    }

    public Node(final Object state2, final Node parent2) {
        this.state = state2;
        this.parent = parent2;
        this.depth = parent2.getDepth() + 1;
    }

    public final Object getState () {
        return state;
    }

    public final Node getParent () {
        return parent;
    }

    public final boolean hasParent () {
        return parent != null;
    }

    public final int getDepth () {
        return depth;
    }

    public final Object getAction () {
        return action;
    }

    public final void setAction (final Object action2) {
        this.action = action2;
    }

    public final double getPathCost () {
        return pathCost;
    }

    public final double getHeuristicCost () {
        return heuristicCost;
    }

    public final double getTotalCost () {
        return totalCost;
    }

    public final void setCost (final double stepCost,
            final double heuristicCost2) {
        this.pathCost = this.parent.pathCost + stepCost;
        this.heuristicCost = heuristicCost2;
        this.totalCost = this.pathCost + this.heuristicCost;
    }

    @Override
    public final int compareTo (final Node anotherNode) {
        return Double.compare(this.totalCost, anotherNode.getTotalCost());
    }

}
