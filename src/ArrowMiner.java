import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ArrowMiner extends KeyAdapter {
    private static final int INPUT_WAITING_TIME = 100;
    private char nextMove = 0;

    @Override
    public final void keyPressed (final KeyEvent ke) {
        final int key = ke.getKeyCode();
        switch (key) {
            case KeyEvent.VK_KP_UP:
            case KeyEvent.VK_UP:
                nextMove = 'U';
                break;
            case KeyEvent.VK_KP_DOWN:
            case KeyEvent.VK_DOWN:
                nextMove = 'D';
                break;
            case KeyEvent.VK_KP_LEFT:
            case KeyEvent.VK_LEFT:
                nextMove = 'L';
                break;
            case KeyEvent.VK_KP_RIGHT:
            case KeyEvent.VK_RIGHT:
                nextMove = 'R';
                break;
            case KeyEvent.VK_A:
                nextMove = 'A';
                break;
            case KeyEvent.VK_W:
            case KeyEvent.VK_SPACE:
                nextMove = 'W';
                break;
            default:
                break;
        }
    }

    public final char next (final int row, final int col, final char[][] map) {
        while (nextMove == 0) {
            try {
                Thread.sleep(INPUT_WAITING_TIME);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Get the move
        final char ret = nextMove;
        // Empty the move
        nextMove = 0;
        return ret;
    }
}
