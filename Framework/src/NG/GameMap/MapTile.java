package NG.GameMap;

import NG.Rendering.MatrixStack.Mesh;
import NG.Rendering.Shapes.MeshShape;
import NG.Tools.Directory;
import NG.Tools.Logger;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * a MapTile represents the mesh with its properties, and is fully immutable. Tiles can be queried with methods like
 * {@link #getRandomOf(Random, int, int, int, int)} and {@link #getByName(String)}.
 * @author Geert van Ieperen created on 3-2-2019.
 * @see MapTileInstance
 */
public class MapTile {
    private static final MapTile DEFAULT_TILE = // circumvent registration due to initialisation of static fields
            new MapTile("default", Directory.mapTileModels.getPath("plain0000.obj"), 0, 0, 0, 0, Properties.NONE, 2);

    private static final Set<String> NAMES = new HashSet<>();
    private static final Set<Path> PATHS = new HashSet<>();
    private static int nextIdentity = 0;

    private static final HashMap<Integer, List<MapTile>> tileFinder = new HashMap<>();

    public final String name;
    public final int tileID;
    public final RotationFreeFit fit;
    public final Mesh mesh;
    public final EnumSet<Properties> properties;
    public final int baseHeight; // height of the middle part, the height the user stands on

    /**
     * register a new MapTile instance with relative heights as given
     * @param name       a unique name for this tile
     * @param meshPath   the path to the visual element of this tile
     * @param pos_pos    the relative height of the mesh at (1, 1) [-3, 3]
     * @param pos_neg    the relative height of the mesh at (1, 0) [-3, 3]
     * @param neg_neg    the relative height of the mesh at (0, 0) [-3, 3]
     * @param neg_pos    the relative height of the mesh at (0, 1) [-3, 3]
     * @param properties the properties of this tile
     * @param baseHeight the height of the middle of the tile
     */
    private MapTile(// pp, pn, nn, np
                    String name, Path meshPath, int pos_pos, int pos_neg, int neg_neg, int neg_pos,
                    EnumSet<Properties> properties, int baseHeight
    ) {
        this.name = name;
        this.tileID = nextIdentity++;
        // the order is important
        this.fit = new RotationFreeFit(pos_pos, pos_neg, neg_neg, neg_pos);
        this.mesh = new MeshShape(meshPath);
        this.properties = properties;
        this.baseHeight = baseHeight;
    }

    public int orientationBytes() {
        return fit.id;
    }

    public static MapTile getByOrientationBit(int orientationBit) {
        List<MapTile> list = tileFinder.get(orientationBit);
        return list == null ? DEFAULT_TILE : list.get(0);
    }

    public static List<MapTile> values() {
        return tileFinder.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static MapTile getByName(String name) {
        return tileFinder.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(tile -> tile.toString().equals(name))
                .findAny()
                .orElse(DEFAULT_TILE);
    }

    public static MapTile registerTile(
            String name, String fileName, int pos_pos, int pos_neg, int neg_neg, int neg_pos,
            EnumSet<Properties> properties, int baseHeight
    ) {
        Path path = Directory.mapTileModels.getPath(fileName);
        return registerTile(name, path, pos_pos, pos_neg, neg_neg, neg_pos, properties, baseHeight);
    }

    public static MapTile registerTile(
            String name, Path meshPath, int pos_pos, int pos_neg, int neg_neg, int neg_pos,
            EnumSet<Properties> properties, int baseHeight
    ) {
        // ensure uniqueness in mesh
        if (PATHS.contains(meshPath)) {
            Logger.WARN.print("Tile " + meshPath + " was already loaded");
            return null;
        }

        // ensure uniqueness in name
        if (NAMES.contains(name)) {
            Logger.WARN.print("A tile with name " + name + " already exists. This will cause problems when saving / loading files");
            int i = 2;
            do {
                name += i++;
            } while (NAMES.contains(name));
            Logger.INFO.print("Renamed tile to " + name);
        }

        NAMES.add(name);
        PATHS.add(meshPath);

        MapTile tile = new MapTile(name, meshPath, pos_pos, pos_neg, neg_neg, neg_pos, properties, baseHeight);

        List<MapTile> tiles = tileFinder.computeIfAbsent(tile.fit.id, (k) -> new ArrayList<>());
        tiles.add(tile);

        return tile;
    }

    /**
     * calculate an id that is the same regardless of how the parameters are sorted, such that any object with the same
     * or a rotation of the parameters gives the same id.
     */
    private static class RotationFreeFit {
        private int id;
        private int rotation;
        private int offset;

        RotationFreeFit(int a, int b, int c, int d) {
            // make all candidates positive, with minimum == 0
            offset = a;
            if (b < offset) offset = b;
            if (c < offset) offset = c;
            if (d < offset) offset = d;
            a -= offset;
            b -= offset;
            c -= offset;
            d -= offset;

            assert a < 127 && b < 127 && c < 127 && d < 127;
            final int[] candidates = new int[4];

            add(a, b, c, d, candidates);
            a = a << 8;
            b = b << 8;
            c = c << 8;
            d = d << 8;
            add(d, a, b, c, candidates);
            a = a << 8;
            b = b << 8;
            c = c << 8;
            d = d << 8;
            add(c, d, a, b, candidates);
            a = a << 8;
            b = b << 8;
            c = c << 8;
            d = d << 8;
            add(b, c, d, a, candidates);

            // choose one deterministically ; pick the maximum
            int max = 0; // all candidates are positive
            rotation = 0;
            for (int i = 0; i < 4; i++) {
                int cand = candidates[i];
                if (cand > max) {
                    max = cand;
                    rotation = 4 - i;
                }
            }
            id = max;
        }

        private static void add(int a, int b, int c, int d, int[] candidates) {
            candidates[0] += a;
            candidates[1] += b;
            candidates[2] += c;
            candidates[3] += d;
        }
    }


    public enum Properties {
        /** this object allows light through */
        TRANSPARENT,
        /** this object does not allow entities through */
        OBSTRUCTING,
        /** this object can be destroyed *///TODO what if this happens?
        DESTRUCTIBLE,

        ;
        /** the empty set of properties */
        public static final EnumSet<Properties> NONE = EnumSet.noneOf(Properties.class);
    }

    /**
     * return a random tile that agrees on -most- of the given heights.
     * @param random  the random generator to use for this process
     * @param pos_pos the height at (1, 1)
     * @param pos_neg the height at (1, 0)
     * @param neg_neg the height at (0, 0)
     * @param neg_pos the height at (0, 1)
     * @return an instance of a randomly chosen tile
     */
    public static MapTileInstance getRandomOf(
            Random random, int pos_pos, int pos_neg, int neg_neg, int neg_pos
    ) {
        RotationFreeFit tgtFit = new RotationFreeFit(pos_pos, pos_neg, neg_neg, neg_pos);
        List<MapTile> list = MapTile.tileFinder.get(tgtFit.id);

        if (list == null) {
            Logger.ASSERT.printf("No tile found for configuration (%d, %d, %d, %d)", pos_pos, pos_neg, neg_neg, neg_pos);
            return new MapTileInstance(neg_neg, tgtFit.offset, DEFAULT_TILE);

        } else {
            int index = random.nextInt(list.size());
            MapTile tile = list.get(index);

            int height = tgtFit.offset - tile.fit.offset;
            int rotation = (tile.fit.rotation - tgtFit.rotation) % 4;
            return new MapTileInstance(height, rotation, tile);
        }
    }
}
