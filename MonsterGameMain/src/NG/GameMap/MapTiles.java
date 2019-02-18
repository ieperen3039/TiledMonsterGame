package NG.GameMap;

import NG.Tools.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * @author Geert van Ieperen created on 6-2-2019.
 */
public class MapTiles {
    private static final Pattern SPACES = Pattern.compile(" ");

    public static void readFile(TileThemeSet sourceSet, Path path) throws IOException {
        Path folder = path.getParent();
        Scanner sc = new Scanner(path);

        while (sc.hasNext()) {
            String line = sc.nextLine();
            if (line.trim().isEmpty() || line.charAt(0) == '#') continue; // comments

            String[] elts = SPACES.split(line);

            String fileName = elts[0];
            int pos_pos = Integer.parseInt(elts[1]);
            int pos_neg = Integer.parseInt(elts[2]);
            int neg_neg = Integer.parseInt(elts[3]);
            int neg_pos = Integer.parseInt(elts[4]);
            EnumSet<MapTile.Properties> properties = MapTile.Properties.NONE; // elts[5]
            int height = Integer.parseInt(elts[6]);

            String texture = (elts.length >= 8) ? elts[7] : null;

            Logger.DEBUG.print(fileName, texture, pos_pos, pos_neg, neg_neg, neg_pos, height);
            MapTile.registerTile(fileName, folder.resolve(fileName), texture, pos_pos, pos_neg, neg_neg, neg_pos, properties, height, sourceSet);
        }
    }
}
