/**
 * Author: Xunhu Sun, sunx2013@my.fit.edu
 * Author: Zachary McHenry, zmchenry2011@my.fit.edu
 * Course: CSE 4051, Fall 2013
 * Project: proj09, Lambda Lift Agent
 * Date: Nov/23/2013
 * Describe: An Agent which can play boulder dash game automatically.
 * */

import java.awt.event.KeyAdapter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class Lift {
    private static final String DEFAULT_MAP = "contest10.map";
    private static Object miner = null;
    private static Method nextMethod = null;
    private static AudioPlayer audioPlayer = null;
    private static String mapName = null;

    private static Object getMiner (final String[] args) {
        Object minerInstance;
        final String className;
        if (args.length > 1) {
            className = args[1];
            if (className.equals("ArrowMiner")
                    && !(System.getProperty("display").equals("graphics"))) {
                // ArrowMiner can only work in graphics
                return null;
            }
        } else { // Default is the miner with AI.
            className = "ExhaustiveMiner";
        }

        try {
            final Class<?> minerType = Class.forName(className);
            final String methodName = "next";
            final Class<?>[] parameters =
                    new Class[] { int.class, int.class, char[][].class };
            // Set global nextMethod
            nextMethod = minerType.getDeclaredMethod(methodName, parameters);
            // Get the Miner instance
            minerInstance = minerType.newInstance();
        } catch (final InstantiationException e) {
            minerInstance = null;
        } catch (final IllegalAccessException e) {
            minerInstance = null;
        } catch (final ClassNotFoundException e) {
            minerInstance = null;
        } catch (final NoSuchMethodException e) {
            minerInstance = null;
        } catch (final SecurityException e) {
            minerInstance = null;
        }
        return minerInstance;
    }

    private static ArrayList<String> getMap (final String[] args) {
        ArrayList<String> mapList = null;
        // Store map file name for graphics use
        if (args.length > 0) {
            mapName = args[0];
        } else {
            mapName = DEFAULT_MAP;
        }

        Scanner input = null;
        try {
            String resourcePath =
                    Thread.currentThread().getContextClassLoader()
                            .getResource("Maps/").toString();
            final URL url = new URL(resourcePath + mapName);
            input = new Scanner(url.openStream());
            mapList = new ArrayList<String>();
            // Put map in list for transferring it into char[][] later
            while (input.hasNextLine()) {
                mapList.add(input.nextLine());
            }
        } catch (final IOException e) {
            // Input error or file does not exist
            mapList = null;
        } finally {
            if (input != null) {
                input.close();
            }
        }

        return mapList;
    }

    private static Gui getGui (final Game game) {
        final Gui gui;
        final String commandProperty = System.getProperty("display");
        if (commandProperty.equals("graphics")) {
            gui = new Gui(Gui.DISPLAY_GRAPHICS, game);
            if (miner.getClass().getName().equals("ArrowMiner")) {
                gui.setMiner((KeyAdapter) miner);
            }
            gui.setMapName(mapName);
        } else if (commandProperty.equals("text")) {
            gui = new Gui(Gui.DISPLAY_TEXT, game);
        } else {
            gui = new Gui(Gui.DISPLAY_NONE, game);
        }

        return gui;
    }

    private static AudioPlayer getAudioPlayer (final Game game) {
        if (System.getProperty("display").equals("graphics")) {
            final AudioSwitch audioSwitch = new AudioSwitch();
            game.setAudio(audioSwitch);
            final AudioPlayer ap = new AudioPlayer(audioSwitch);
            return ap;
        } else {
            return null;
        }
    }

    public static void main (final String[] args) {
        if (System.getProperty("display") == null) { // Default is graphics mode.
            System.setProperty("display", "graphics");
        }
        
        miner = getMiner(args);
        if (miner == null) {
            System.out.println("Please input correct Miner type.");
            return;
        }
        final ArrayList<String> mapList = getMap(args);
        if (mapList == null) {
            System.out.println("Please input correct Map file path.");
            return;
        }
        final Game game = new Game(mapList);

        final Gui gui = getGui(game);
        gui.display(game);
        audioPlayer = getAudioPlayer(game);

        while (game.getGameState() == Game.STATE_RUNNING) {
            final char next =
                    getNextAction(new Object[] { game.getCurX(),
                            game.getCurY(), game.getMap() });
            if (next == '\0') {
                System.out.println("Get action failed.");
                break;
            }

            game.play(next);
            gui.display(game);
            if (audioPlayer != null) {
                audioPlayer.play();
            }
        }
    }

    private static char getNextAction (final Object[] args) {
        char next;
        try {
            next = (Character) nextMethod.invoke(miner, args);
        } catch (final IllegalAccessException e) {
            next = '\0';
        } catch (final IllegalArgumentException e) {
            next = '\0';
        } catch (final InvocationTargetException e) {
            next = '\0';
        }
        return next;
    }
}
