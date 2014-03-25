public class Successor {
    private final Object state;
    private final Object action;

    public Successor(final Object state2, final Object action2) {
        this.state = state2;
        this.action = action2;
    }

    public final Object getState () {
        return state;
    }

    public final Object getAction () {
        return action;
    }
}
