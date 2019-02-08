package NG.GameMap;

import NG.Tools.Logger;

import java.util.EnumSet;

/**
 * @author Geert van Ieperen created on 6-2-2019.
 */
public class PlainTiles {
    public static void loadAll() {
        // in no specific order
        register("plain0000.obj", 0, 0, 0, 0, MapTile.Properties.NONE, 2);
        register("plain0011.obj", 0, 0, 1, 1, MapTile.Properties.NONE, 2);
        register("plain0023.obj", 0, 0, 2, 3, MapTile.Properties.NONE, 3);
        register("plain0113.obj", 0, 1, 1, 3, MapTile.Properties.NONE, 3);
        register("plain0222.obj", 0, 2, 2, 2, MapTile.Properties.NONE, 3);
        register("plain0001.obj", 0, 0, 0, 1, MapTile.Properties.NONE, 2);
        register("plain0012.obj", 0, 0, 1, 2, MapTile.Properties.NONE, 2);
        register("plain0033.obj", 0, 0, 3, 3, MapTile.Properties.NONE, 3);
        register("plain0122.obj", 0, 1, 2, 2, MapTile.Properties.NONE, 3);
        register("plain0223.obj", 0, 2, 2, 3, MapTile.Properties.NONE, 3);
        register("plain0002.obj", 0, 0, 0, 2, MapTile.Properties.NONE, 2);
        register("plain0013.obj", 0, 0, 1, 3, MapTile.Properties.NONE, 2);
        register("plain0111.obj", 0, 1, 1, 1, MapTile.Properties.NONE, 2);
        register("plain0123.obj", 0, 1, 2, 3, MapTile.Properties.NONE, 3);
        register("plain0233.obj", 0, 2, 3, 3, MapTile.Properties.NONE, 3);
        register("plain0003.obj", 0, 0, 0, 3, MapTile.Properties.NONE, 2);
        register("plain0022.obj", 0, 0, 2, 2, MapTile.Properties.NONE, 3);
        register("plain0112.obj", 0, 1, 1, 2, MapTile.Properties.NONE, 2);
        register("plain0133.obj", 0, 1, 3, 3, MapTile.Properties.NONE, 3);
        register("plain0333.obj", 0, 3, 3, 3, MapTile.Properties.NONE, 4);
        register("plain0131.obj", 0, 1, 3, 1, MapTile.Properties.NONE, 3);
        register("plain0032.obj", 0, 0, 3, 2, MapTile.Properties.NONE, 3);
        register("plain0322.obj", 0, 3, 2, 2, MapTile.Properties.NONE, 3);
        register("plain0132.obj", 0, 1, 3, 2, MapTile.Properties.NONE, 3);
        register("plain0231.obj", 0, 2, 3, 1, MapTile.Properties.NONE, 3);
        register("plain0323.obj", 0, 3, 2, 3, MapTile.Properties.NONE, 3);
        register("plain0232.obj", 0, 2, 3, 2, MapTile.Properties.NONE, 3);
        register("plain0331.obj", 0, 3, 3, 1, MapTile.Properties.NONE, 3);
        register("plain0211.obj", 0, 2, 1, 1, MapTile.Properties.NONE, 2);
        register("plain0332.obj", 0, 3, 3, 2, MapTile.Properties.NONE, 3);
        register("plain0212.obj", 0, 2, 1, 2, MapTile.Properties.NONE, 3);
        register("plain0311.obj", 0, 3, 1, 1, MapTile.Properties.NONE, 3);
        register("plain0121.obj", 0, 1, 2, 1, MapTile.Properties.NONE, 2);
        register("plain0213.obj", 0, 2, 1, 3, MapTile.Properties.NONE, 3);
        register("plain0312.obj", 0, 3, 1, 2, MapTile.Properties.NONE, 3);
        register("plain0221.obj", 0, 2, 2, 1, MapTile.Properties.NONE, 3);
        register("plain0313.obj", 0, 3, 1, 3, MapTile.Properties.NONE, 3);
        register("plain0321.obj", 0, 3, 2, 1, MapTile.Properties.NONE, 3);
        register("plain0021.obj", 0, 0, 2, 1, MapTile.Properties.NONE, 2);
        register("plain0031.obj", 0, 0, 3, 1, MapTile.Properties.NONE, 2);
        register("plain0242.obj", 0, 2, 4, 2, MapTile.Properties.NONE, 4);
        register("plain0101.obj", 0, 1, 0, 1, MapTile.Properties.NONE, 2);
        register("plain0201.obj", 0, 2, 0, 1, MapTile.Properties.NONE, 2);
        register("plain0202.obj", 0, 2, 0, 2, MapTile.Properties.NONE, 3);

        Logger.INFO.print("Registered plain MapTiles");
    }

    private static void register(
            String meshPath, int pos_pos, int pos_neg, int neg_neg, int neg_pos,
            EnumSet<MapTile.Properties> properties, int baseHeight
    ) {
        String name = meshPath.replace(".obj", "");
        MapTile.registerTile(name, meshPath, pos_pos, pos_neg, neg_neg, neg_pos, properties, baseHeight, TileThemeSet.PLAIN);
    }
}
