import java.util.List;

public abstract class Problem {
    public abstract Object getInitialState ();

    public abstract List<Successor> getSuccessors (final Object state);

    public abstract boolean isGoalState (final Object state);

    public abstract double getStepCost (final Object srcState,
            final Object desState, final Object action);

    public abstract double getHeuristicCost (final Object state);
}
