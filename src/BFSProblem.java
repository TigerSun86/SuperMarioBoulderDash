/**
 * A problem can be solved by Breadth First Search needs formally four
 * components:
 * 1) Initial State.
 * 2) Successor Function.
 * 3) Goal Test.
 * 4) Path Cost(Constant cost in each step).
 */
public abstract class BFSProblem extends Problem {
    @Override
    public final double getStepCost (final Object srcState, final Object desState,
            final Object action) {
        return 1;
    }

    @Override
    public final double getHeuristicCost (final Object state) {
        return 0;
    }
}
