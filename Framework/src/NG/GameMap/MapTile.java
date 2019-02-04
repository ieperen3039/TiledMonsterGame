package NG.GameMap;

import NG.Rendering.MatrixStack.Mesh;
import NG.Rendering.Shapes.MeshShape;
import NG.Tools.Directory;
import NG.Tools.Logger;

import java.nio.file.Path;
import java.util.*;

/**
 * the immutable type of tile that can be used for any tile
 * @author Geert van Ieperen created on 3-2-2019.
 */
public enum MapTile {
    PLAIN00("plain0000.obj", 0, 0, 0, 0, Properties.NONE),
    PLAIN01("plain0011.obj", 0, 0, 1, 1, Properties.NONE),
    PLAIN02("plain0023.obj", 0, 0, 2, 3, Properties.NONE),
    PLAIN03("plain0113.obj", 0, 1, 1, 3, Properties.NONE),
    PLAIN04("plain0222.obj", 0, 2, 2, 2, Properties.NONE),
    PLAIN05("plain0001.obj", 0, 0, 0, 1, Properties.NONE),
    PLAIN06("plain0012.obj", 0, 0, 1, 2, Properties.NONE),
    PLAIN07("plain0033.obj", 0, 0, 3, 3, Properties.NONE),
    PLAIN08("plain0122.obj", 0, 1, 2, 2, Properties.NONE),
    PLAIN09("plain0223.obj", 0, 2, 2, 3, Properties.NONE),
    PLAIN10("plain0002.obj", 0, 0, 0, 2, Properties.NONE),
    PLAIN11("plain0013.obj", 0, 0, 1, 3, Properties.NONE),
    PLAIN12("plain0111.obj", 0, 1, 1, 1, Properties.NONE),
    PLAIN13("plain0123.obj", 0, 1, 2, 3, Properties.NONE),
    PLAIN14("plain0233.obj", 0, 2, 3, 3, Properties.NONE),
    PLAIN15("plain0003.obj", 0, 0, 0, 3, Properties.NONE),
    PLAIN16("plain0022.obj", 0, 0, 2, 2, Properties.NONE),
    PLAIN17("plain0112.obj", 0, 1, 1, 2, Properties.NONE),
    PLAIN18("plain0133.obj", 0, 1, 3, 3, Properties.NONE),
    PLAIN19("plain0333.obj", 0, 3, 3, 3, Properties.NONE),
    PLAIN20("plain0131.obj", 0, 1, 3, 1, Properties.NONE),
    PLAIN21("plain0032.obj", 0, 0, 3, 2, Properties.NONE),
    PLAIN22("plain0322.obj", 0, 3, 2, 2, Properties.NONE),
    PLAIN23("plain0132.obj", 0, 1, 3, 2, Properties.NONE),
    PLAIN24("plain0231.obj", 0, 2, 3, 1, Properties.NONE),
    PLAIN25("plain0323.obj", 0, 3, 2, 3, Properties.NONE),
    PLAIN26("plain0232.obj", 0, 2, 3, 2, Properties.NONE),
    PLAIN27("plain0331.obj", 0, 3, 3, 1, Properties.NONE),
    PLAIN28("plain0211.obj", 0, 2, 1, 1, Properties.NONE),
    PLAIN29("plain0332.obj", 0, 3, 3, 2, Properties.NONE),
    PLAIN30("plain0212.obj", 0, 2, 1, 2, Properties.NONE),
    PLAIN31("plain0311.obj", 0, 3, 1, 1, Properties.NONE),
    PLAIN32("plain0121.obj", 0, 1, 2, 1, Properties.NONE),
    PLAIN33("plain0213.obj", 0, 2, 1, 3, Properties.NONE),
    PLAIN34("plain0312.obj", 0, 3, 1, 2, Properties.NONE),
    PLAIN35("plain0221.obj", 0, 2, 2, 1, Properties.NONE),
    PLAIN36("plain0313.obj", 0, 3, 1, 3, Properties.NONE),
    PLAIN37("plain0321.obj", 0, 3, 2, 1, Properties.NONE),
    PLAIN38("plain0021.obj", 0, 0, 2, 1, Properties.NONE),
    PLAIN39("plain0031.obj", 0, 0, 3, 1, Properties.NONE),
    ;

    private static final HashMap<Integer, List<MapTile>> tileFinder = new HashMap<>();

    public final RotationFreeFit fit;
    public final Mesh mesh;
    public final EnumSet<Properties> properties;

    /**
     * register a new MapTile instance with relative heights as given
     * @param meshPath       the visual element of this tile
     * @param pos_pos    the relative height of the mesh at (1, 1) [-3, 3]
     * @param pos_neg    the relative height of the mesh at (1, 0) [-3, 3]
     * @param neg_neg    the relative height of the mesh at (0, 0) [-3, 3]
     * @param neg_pos    the relative height of the mesh at (0, 1) [-3, 3]
     * @param properties the properties of this tile
     */
    MapTile(// pp, pn, nn, np
            String meshPath, int pos_pos, int pos_neg, int neg_neg, int neg_pos,
            EnumSet<Properties> properties
    ) {
        Path path = Directory.mapTileModels.getPath(meshPath);
        // the order is important
        this.fit = new RotationFreeFit(pos_pos, pos_neg, neg_neg, neg_pos);
        this.mesh = new MeshShape(path);
        this.properties = properties;
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

            // compensate for base tile height
//            offset += (int) ((a + b + c + d) * 0.25f);
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
        private static final EnumSet<Properties> NONE = EnumSet.noneOf(Properties.class);
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
        if (tileFinder.isEmpty()) {
            for (MapTile value : MapTile.values()) {
                int id = value.fit.id;
                List<MapTile> tiles = tileFinder.computeIfAbsent(id, (k) -> new ArrayList<>());
                tiles.add(value);
                if (tiles.size() != 1) Logger.INFO.print(tiles);
            }
        }

        RotationFreeFit tgtFit = new RotationFreeFit(pos_pos, pos_neg, neg_neg, neg_pos);
        List<MapTile> list = MapTile.tileFinder.get(tgtFit.id);

        if (list == null) {
//            Logger.ASSERT.printf("No tile found for configuration (%d, %d, %d, %d) (%d)", pos_pos, pos_neg, neg_neg, neg_pos, tgtFit.id);
            return new MapTileInstance(neg_neg, tgtFit.offset, PLAIN00);

        } else {
//            int index = random.nextInt(list.size());
            int index = 0;
            MapTile tile = list.get(index);

            int height = tgtFit.offset - tile.fit.offset;
            int rotation = (tile.fit.rotation - tgtFit.rotation) % 4;
            return new MapTileInstance(height, rotation, tile);
        }
    }
}
