import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class Gui {
    public static final int DISPLAY_NONE = 0;
    public static final int DISPLAY_GRAPHICS = 1;
    public static final int DISPLAY_TEXT = 2;
    private static final int VISUAL_DEFAULT_WIDTH = 800;
    private static final int VISUAL_DEFAULT_HEIGHT = 600;
    private static final int SCORE_BOARD_HEIGHT = 2;
    private static final int SCORE_BOARD_WIDTH = 4;
    private static final char FAILED_MINER = 'F';
    private static final int INPUT_WAITING_TIME_SHORT = 200;
    private static final int INPUT_WAITING_TIME_LONG = 500;

    private final int displayOption;
    private String mapName = null;
    private JFrame frame;
    private JPanel scoreBoard;
    private JPanel visual;
    private JLabel gameOverLabel;
    private int iconWidth;
    private int iconHeight;
    private int mapWidth;
    private int mapHeight;
    private int visualWidth = VISUAL_DEFAULT_WIDTH;
    private int visualHeight = VISUAL_DEFAULT_HEIGHT;
    private boolean isFirstTimeToDisplay = false;
    private long lastRunTime;
    private HashMap<Character, Image> imageMap = null;
    private volatile HashMap<Character, Image> resizedImgMap = null;

    public Gui(final int thatDisplayOption, final Game game) {
        displayOption = thatDisplayOption;
        if (displayOption == DISPLAY_GRAPHICS) {
            InitVideo();
            initGraphics(game);
            isFirstTimeToDisplay = true;
        }
        lastRunTime = System.currentTimeMillis();
    }

    private void waitTime () {
        if (isFirstTimeToDisplay) {
            isFirstTimeToDisplay = false;
            // Wait a little time for gui initial display.
            try {
                Thread.sleep(INPUT_WAITING_TIME_LONG);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            lastRunTime = System.currentTimeMillis();
        } else {
            final long thisRunTime = System.currentTimeMillis();
            final long duration = thisRunTime - lastRunTime;
            if (duration >= 0 && duration < INPUT_WAITING_TIME_SHORT) {
                // Wait a little time for preventing too soon to invoke another
                // display.
                try {
                    Thread.sleep(INPUT_WAITING_TIME_SHORT - duration);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
            lastRunTime = System.currentTimeMillis();
        }
    }

    public final void display (final Game game) {
        if (displayOption == DISPLAY_GRAPHICS) {
            displayGraphics(game);
            waitTime();
        } else if (displayOption == DISPLAY_TEXT) {
            displayText(game);
            waitTime();
        } else {
            displayNone(game);
        }
    }

    public final void setMiner (final KeyAdapter arrowMiner) {
        // Add Arrow key listener
        frame.addKeyListener(arrowMiner);
    }

    public final void setMapName (final String name) {
        mapName = name;
    }

    private final void InitVideo () {
        System.out.println("Initializing video...");
        final String resourcePath = getClass().getResource("/resources/").toString();
        imageMap = new HashMap<Character, Image>();
        // Load all image resource into memory at once
        initImageMap(Game.MINER, resourcePath + "miner.png");
        initImageMap(Game.WALL, resourcePath + "wall.png");
        initImageMap(Game.ROCK, resourcePath + "rock.png");
        initImageMap(Game.GOLD, resourcePath + "gold.png");
        initImageMap(Game.LIFT, resourcePath + "lift.png");
        initImageMap(Game.OPEN, resourcePath + "open.png");
        initImageMap(Game.EARTH, resourcePath + "earth.png");
        initImageMap(Game.EMPTY, resourcePath + "empty.png");
        initImageMap(FAILED_MINER, resourcePath + "minerfailed.png");

        resizedImgMap = new HashMap<Character, Image>();
        System.out.println("Done.");
    }

    private void resizeImages () {
        for (Map.Entry<Character, Image> entry : imageMap.entrySet()) {
            final Image img = entry.getValue();
            final Image newImg =
                    img.getScaledInstance(iconWidth, iconHeight,
                            Image.SCALE_DEFAULT);
            resizedImgMap.put(entry.getKey(), newImg);
        }
    }

    private void initImageMap (final char c, final String resourcePath) {
        try {
            final Image img = ImageIO.read(new URL(resourcePath));
            imageMap.put(c, img);
        } catch (final IOException e) {
            System.err.println("Couldn't find file: " + resourcePath);
        }

    }

    private void initGraphics (final Game game) {
        mapHeight = game.getHeight();
        mapWidth = game.getWidth();
        // Display score board
        scoreBoard =
                new JPanel(
                        new GridLayout(SCORE_BOARD_HEIGHT, SCORE_BOARD_WIDTH));
        // Display map
        initIconWidthAndHeight();
        visual = new JPanel(new GridLayout(mapHeight, mapWidth));

        frame = new JFrame("Super Mario Boulder Dash");
        frame.setLayout(new BorderLayout());
        frame.getContentPane().add(scoreBoard, BorderLayout.PAGE_START);
        frame.getContentPane().add(visual, BorderLayout.CENTER);
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized (final ComponentEvent evt) {
                visualWidth = visual.getWidth();
                visualHeight = visual.getHeight();
                iconWidth = visualWidth / mapWidth;
                iconHeight = visualHeight / mapHeight;
                displayGraphics(game);
            }
        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void displayGraphics (final Game game) {
        // Update score board
        setScoreBoard(game, scoreBoard);

        // Update map
        setVisualPanel(game, visual);

        // Display game over information
        gameOverLabel = getGameOverLabel(game);
        if (gameOverLabel != null) {
            frame.getContentPane().add(gameOverLabel, BorderLayout.PAGE_END);
        }

        frame.pack();
        frame.repaint();
        frame.setVisible(true);
    }

    private void initIconWidthAndHeight () {
        // Make sure Icon size is square
        final int temp =
                Math.min(visualWidth / mapWidth, visualHeight / mapHeight);
        iconWidth = temp;
        iconHeight = temp;
    }

    private void setScoreBoard (final Game game, final JPanel sB) {
        sB.removeAll();
        sB.add(getLabel("MARIO"));
        sB.add(getLabel("COIN"));
        sB.add(getLabel("WORLD"));
        sB.add(getLabel("TIME"));
        sB.add(getLabel(Integer.toString(game.getScore())));
        sB.add(getLabel(Integer.toString(game.getGoldCollected())));
        final String name;
        if (mapName != null) {
            name = mapName;
        } else {
            name = "MAP_NAME";
        }
        sB.add(getLabel(name));
        sB.add(getLabel(Integer.toString(game.getNumOfMoves())));
    }

    private void setVisualPanel (final Game game, final JPanel panel) {
        // Resize the Images for display
        resizeImages();
        // Create JPanel to hold images
        panel.removeAll();
        panel.setBackground(Color.black);
        for (int y = mapHeight - 1; y >= 0; y--) {
            for (int x = 0; x < mapWidth; x++) {
                char spot = game.getSpot(x, y);
                if (spot == Game.MINER
                        && game.getGameState() == Game.STATE_FAILED) {
                    // If miner failed, display a failed image
                    spot = FAILED_MINER;
                }
                final ImageIcon imgIcon =
                        new ImageIcon(resizedImgMap.get(spot));
                final JLabel label = new JLabel(imgIcon);
                panel.add(label);
            }
        }
    }

    private JLabel getGameOverLabel (final Game game) {
        final JLabel jLabel;
        if (game.getGameState() == Game.STATE_WINNING) {
            jLabel = getLabel("GAME OVER. YOU WIN.");
        } else if (game.getGameState() == Game.STATE_FAILED) {
            jLabel = getLabel("GAME OVER. YOU LOSE.");
        } else if (game.getGameState() == Game.STATE_ABORTED) {
            jLabel = getLabel("GAME OVER. YOU ABORT.");
        } else {
            jLabel = null;
        }
        return jLabel;
    }

    private JLabel getLabel (final String str) {
        final JLabel label;
        if (!str.isEmpty()) {
            label = new JLabel(str, SwingConstants.CENTER);
        } else {
            label = new JLabel();
        }
        label.setForeground(Color.WHITE);
        label.setBackground(Color.BLACK);
        label.setOpaque(true);
        return label;
    }

    private void displayText (final Game game) {
        final int height = game.getHeight();
        final int width = game.getWidth();
        System.out.printf("Score = %d%n", game.getScore());
        // Display as the task requirement
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                System.out.print(game.getSpot(x, y));
            }
            System.out.println();
        }
        if (game.getGameState() != Game.STATE_RUNNING) {
            final String finalSentence;
            if (game.getGameState() == Game.STATE_WINNING) {
                finalSentence = "Win";
            } else if (game.getGameState() == Game.STATE_FAILED) {
                finalSentence = "Lose";
            } else { // if (game.getState() == game.STATE_ABORT)
                finalSentence = "Abort";
            }
            System.out.printf("Game Over. You %s.\n", finalSentence);
        }
    }

    private void displayNone (final Game game) {
        System.out.printf("Score = %d%n", game.getScore());
        if (game.getGameState() != Game.STATE_RUNNING) {
            final String finalSentence;
            if (game.getGameState() == Game.STATE_WINNING) {
                finalSentence = "Win";
            } else if (game.getGameState() == Game.STATE_FAILED) {
                finalSentence = "Lose";
            } else { // if (game.getState() == game.STATE_ABORT)
                finalSentence = "Abort";
            }
            System.out.printf("Game Over. You %s.\n", finalSentence);
        }
    }
}
