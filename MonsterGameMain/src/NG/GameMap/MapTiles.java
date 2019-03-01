package NG.GameMap;

import NG.Tools.Toolbox;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Scanner;

/**
 * @author Geert van Ieperen created on 6-2-2019.
 */
public class MapTiles {

    public static void readFile(TileThemeSet sourceSet, Path path) throws IOException {
        Path folder = path.getParent();
        Scanner sc = new Scanner(path);

        while (sc.hasNext()) {
            String line = sc.nextLine();
            if (line.trim().isEmpty() || line.charAt(0) == '#') continue; // comments

            String[] elts = Toolbox.WHITESPACE_PATTERN.split(line);

            String fileName = elts[0];
            int[] heights = new int[]{
                    Integer.parseInt(elts[1]),
                    Integer.parseInt(elts[2]),
                    Integer.parseInt(elts[3]),
                    Integer.parseInt(elts[4]),
                    Integer.parseInt(elts[5]),
                    Integer.parseInt(elts[6]),
                    Integer.parseInt(elts[7]),
                    Integer.parseInt(elts[8]),
            };
            EnumSet<MapTile.Properties> properties = MapTile.Properties.NONE; // elts[9]
            int height = Integer.parseInt(elts[10]);

            String texture = (elts.length >= 12) ? elts[11] : null;

            MapTile.registerTile(fileName, folder.resolve(fileName), texture, heights, properties, height, sourceSet);
        }
    }
}
