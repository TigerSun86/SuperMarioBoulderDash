public class AudioSwitch {
    public static final int THEME = 0;
    public static final int COIN = 1;
    public static final int EARTH = 2;
    public static final int ROCK = 3;
    public static final int WIN = 4;
    public static final int DIE = 5;
    public static final int ABORT = 6;
    public static final int MAX = 7;

    public boolean[] aSwitch = new boolean[MAX];

    public final boolean get (final int i) {
        return aSwitch[i];
    }

    public final void turnOn (final int i) {
        aSwitch[i] = true;
    }

    public final void turnOff (final int i) {
        aSwitch[i] = false;
    }
}
