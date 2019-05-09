package NG.GameMap;

import NG.Engine.GameTimer;
import NG.Rendering.MatrixStack.SGL;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 3-2-2019.
 */
public interface MapChunk {
    /**
     * @param x the x coordinate relative to this chunk
     * @param y the y coordinate relative to this chunk
     * @return the MapTile on the given coordinate
     */
    MapTile.Instance get(int x, int y);

    /**
     * sets a given position to a new tile
     * @param x the x coordinate relative to this chunk
     * @param y the y coordinate relative to this chunk
     * @return the previous tile, or null if there was none
     */
    MapTile set(int x, int y, MapTile.Instance tile);

    /**
     * return the height of the MapTile on the given position
     * @param x the x coordinate relative to this chunk
     * @param y the y coordinate relative to this chunk
     * @return the base height / elevation of the tile on the given coordinate, or 0 when the coordinate is out of
     * bounds.
     */
    int getHeightAt(int x, int y);

    /**
     * Draws this chunk using the provided SGL object. This method may only be called from the rendering loop, and
     * should not change the internal representation of this object. Possible animations should be based on {@link
     * GameTimer#getRendertime()}. Material must be set using {@link SGL#getShader()}.
     * @param gl the graphics object to be used for rendering. It is initialized at world's origin. (no translation or
     *           scaling has been applied)
     */
    void draw(SGL gl);

    /**
     * adds a highlight to the given relative coordinate in this chunk
     * @param x the x coordinate relative to this chunk
     * @param y the y coordinate relative to this chunk
     */
    void highlight(int x, int y);

    /**
     * toggles whether the tiles previously set by {@link #highlight(int, int)} should be highlighted or not. Calls to
     * this method do not change the set of tiles that are highlighted.
     * @param doHighlight when true, highights are made visible,. when false, highlight are made not visible
     */
    void setHighlight(boolean doHighlight);

    /**
     * clears all highlights off the tiles.
     */
    void clearHighlight();

    void writeToFile(DataOutputStream out) throws IOException;

    void readFromFile(DataInputStream in, Map<Integer, MapTile> mapping) throws IOException;

    Extremes getMinMax();

    class Extremes {
        private float min = Float.POSITIVE_INFINITY;
        private float max = Float.NEGATIVE_INFINITY;

        public void check(float value) {
            if (value > max) max = value;
            if (value < min) min = value;
        }

        public float getMin() {
            return min;
        }

        public float getMax() {
            return max;
        }
    }
}
