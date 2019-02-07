package NG.GameMap;

import java.io.File;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Geert van Ieperen created on 6-2-2019.
 */
public class TileDirectoryReader {
    private static final Pattern NUMBERS = Pattern.compile("%d%d%d%d");

    public static void readDirectory(File file) {

        // recursively travel subdirectories
        if (file.isDirectory()) {
            // skip certain folders
            switch (file.getName()) {
                case ".git":
                case ".idea":
                case "out":
                case "jar":
                    return;
            }

            File[] files = Objects.requireNonNull(file.listFiles());
            for (File subDir : files) {
                readDirectory(subDir);
            }
        }

        readFile(file);
    }

    public static void readFile(File file) {
        String[] nameParts = file.getName().split("\\.");
        if (nameParts.length < 2 || !nameParts[1].equals("obj")) return;

        String name = nameParts[0];
        Matcher match = NUMBERS.matcher(name);
        if (!match.find()) return;

        int mBegin = match.start();
        int pos_pos = Integer.parseInt(String.valueOf(name.charAt(mBegin++)));
        int pos_neg = Integer.parseInt(String.valueOf(name.charAt(mBegin++)));
        int neg_neg = Integer.parseInt(String.valueOf(name.charAt(mBegin++)));
        int neg_pos = Integer.parseInt(String.valueOf(name.charAt(mBegin)));

        int possibleOffset = (int) ((pos_pos + pos_neg + neg_neg + neg_pos) * 0.25f);

        MapTile.registerTile(name, file.toPath(), pos_pos, pos_neg, neg_neg, neg_pos, MapTile.Properties.NONE, possibleOffset);
    }
}
