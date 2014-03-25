import java.util.Scanner;
import java.util.regex.Pattern;

public class DefaultMiner {
    private static final Scanner STDIN = new Scanner(System.in)
            .useDelimiter("");

    private static final Pattern DOT = Pattern.compile(".", Pattern.DOTALL);

    // Return next character in input stream, or
    // raise null pointer exception (on EOF)
    public static char nextChar (final Scanner s) {
        return s.findWithinHorizon(DOT, 1).charAt(0);
    }

    public static final char next (final int row, final int col, final char[][] map) {
        final char ch = nextChar(STDIN);
        return ch;
    }
}
