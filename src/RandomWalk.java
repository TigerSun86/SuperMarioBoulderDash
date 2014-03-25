
public class RandomWalk {
    private static final int FACTOR_3 = 3;
    private static final int FACTOR_4 = 4;

    public static final char next (final int row, final int col,
            final char[][] map) {
        final int rand = ((int) Math.round(Math.random() * FACTOR_4));
        char ret = 'W';
        if (rand == 0) {
            ret = 'U';
        } else if (rand == 1) {
            ret = 'D';
        } else if (rand == 2) {
            ret = 'L';
        } else if (rand == FACTOR_3) {
            ret = 'R';
        }
        return ret;
    }
}

