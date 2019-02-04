package NG.GameMap;

import NG.Rendering.MatrixStack.Mesh;

import java.util.EnumSet;

import static NG.Rendering.Shapes.FileShapes.CUBE;

/**
 * the immutable type of tile that can be used for any tile
 * @author Geert van Ieperen created on 3-2-2019.
 */
public enum MapTile {
    PLAIN(CUBE, 0, 0, 0, 0, Properties.NONE);

    public final byte nn, np, pn, pp;
    public final Mesh mesh;
    public final EnumSet<Properties> properties;

    /**
     * register a new MapTile instance with relative heights as given
     * @param mesh       the visual element of this tile
     * @param neg_neg    the relative height of the mesh at (0, 0) [-3, 3]
     * @param neg_pos    the relative height of the mesh at (0, 1) [-3, 3]
     * @param pos_neg    the relative height of the mesh at (1, 0) [-3, 3]
     * @param pos_pos    the relative height of the mesh at (1, 1) [-3, 3]
     * @param properties the properties of this tile
     */
    MapTile(
            Mesh mesh, int neg_neg, int neg_pos, int pos_neg, int pos_pos,
            EnumSet<MapTile.Properties> properties
    ) {
        this.nn = (byte) neg_neg;
        this.np = (byte) neg_pos;
        this.pn = (byte) pos_neg;
        this.pp = (byte) pos_pos;
        this.mesh = mesh;
        this.properties = properties;
    }

    public enum Properties {
        /** this object does not allow light through */
        OPAQUE,
        /** this object does not allow entities through */
        INPASSIBLE,
        /** this object can be destroyed *///TODO what if this happens?
        DESTRUCTIBLE,

        ;
        /** the empty set of properties */
        private static final EnumSet<Properties> NONE = EnumSet.noneOf(Properties.class);
    }

    /**
     * return a random tile that agrees on -most- of the given heights.
     * @param neg_neg the actual height of the mesh at (0, 0) [-3, 3]
     * @param neg_pos the actual height of the mesh at (0, 1) [-3, 3]
     * @param pos_neg the actual height of the mesh at (1, 0) [-3, 3]
     * @param pos_pos the actual height of the mesh at (1, 1) [-3, 3]
     * @return an instance of a randomly chosen tile
     */
    public static MapTileInstance getRandomOf(int neg_neg, int neg_pos, int pos_neg, int pos_pos) {
        return new MapTileInstance(neg_neg, 0, PLAIN);
    }
}
