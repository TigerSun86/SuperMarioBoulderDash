import java.applet.Applet;
import java.applet.AudioClip;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class AudioPlayer {
    private static final HashMap<Integer, AudioClip> AUDIO_MAP =
            new HashMap<Integer, AudioClip>();
    private AudioSwitch audioSwitch;

    public AudioPlayer(final AudioSwitch as) {
        audioSwitch = as;

        System.out.println("Initializing audio...");
        final String resourcePath = getClass().getResource("/resources/").toString();
        AUDIO_MAP.put(AudioSwitch.THEME, createAudioClip(resourcePath
                + "theme.wav"));
        AUDIO_MAP.put(AudioSwitch.COIN, createAudioClip(resourcePath
                + "coin.wav"));
        AUDIO_MAP.put(AudioSwitch.EARTH, createAudioClip(resourcePath
                + "earth.wav"));
        AUDIO_MAP.put(AudioSwitch.ROCK, createAudioClip(resourcePath
                + "rock.wav"));
        AUDIO_MAP.put(AudioSwitch.WIN,
                createAudioClip(resourcePath + "win.wav"));
        AUDIO_MAP.put(AudioSwitch.DIE,
                createAudioClip(resourcePath + "die.wav"));
        AUDIO_MAP.put(AudioSwitch.ABORT, createAudioClip(resourcePath
                + "abort.wav"));
        System.out.println("Done.");
        if (audioSwitch.get(AudioSwitch.THEME)) {
            // Play theme from the begining
            AUDIO_MAP.get(AudioSwitch.THEME).loop();
        }
    }

    public final void play () {
        if (!audioSwitch.get(AudioSwitch.THEME)) {
            // Game over, so turn off the theme
            AUDIO_MAP.get(AudioSwitch.THEME).stop();
        }

        for (int i = AudioSwitch.THEME + 1; i < AudioSwitch.MAX; i++) {
            if (audioSwitch.get(i)) {
                // Switch of this sound is on, so play it
                AUDIO_MAP.get(i).play();
                // Then turn off this switch
                audioSwitch.turnOff(i);
            }
        }
    }

    /** Returns an AudioClip, or null if the path was invalid. */
    protected final AudioClip createAudioClip (final String path) {
        AudioClip ac = null;
        try {
            final URL fileURL = new URL(path);
            ac = Applet.newAudioClip(fileURL);
        } catch (final IOException e) {
            System.err.println("Couldn't find file: " + path);
        }

        return ac;

    }
}
