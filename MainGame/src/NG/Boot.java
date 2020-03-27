package NG;

import NG.Core.MonsterGame;
import NG.Settings.Settings;
import NG.Tools.Directory;
import NG.Tools.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Boots the Root
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class Boot {
    public static void main(String[] argArray) throws Exception {
        List<String> args = new ArrayList<>(Arrays.asList(argArray));
        Settings settings = new Settings();

        if (args.contains("-debug")) {
            Logger.setLoggingLevel(Logger.DEBUG);
            settings.DEBUG = true;
            settings.DEBUG_SCREEN = true;
            settings.RENDER_HITBOXES = true;
            settings.HIDE_CURSOR_ON_MAP = false;

        } else if (args.contains("-quiet")) {
            Logger.setLoggingLevel(Logger.ERROR);

        } else {
            Logger.setLoggingLevel(Logger.INFO);
        }

        Logger.DEBUG.print("General debug information: " +
                // manual aligning will do the trick
                "\n\tSystem OS:             " + System.getProperty("os.name") +
                "\n\tJava VM:               " + System.getProperty("java.runtime.version") +
                "\n\tGame version:          " + MonsterGame.GAME_VERSION +
                "\n\tMain directory         " + Directory.workDirectory() +
                "\n\tMods directory:        " + Directory.mods.getPath()
        );

        new MonsterGame(settings).root();
    }
}
