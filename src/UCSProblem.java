/**
 * A problem can be solved by Uniform Cost Search needs formally four
 * components:
 * 1) Initial State.
 * 2) Successor Function.
 * 3) Goal Test.
 * 4) Path Cost(Variable cost in each step).
 */
public abstract class UCSProblem extends Problem {
    @Override
    public final double getHeuristicCost (final Object state) {
        return 0;
    }
}
