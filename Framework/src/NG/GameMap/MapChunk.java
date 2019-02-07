package NG.GameMap;

import NG.Engine.GameTimer;
import NG.Rendering.MatrixStack.SGL;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author Geert van Ieperen created on 3-2-2019.
 */
public interface MapChunk {
    /**
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the MapTile on the given coordinate
     */
    MapTile get(int x, int y);

    /**
     * sets a given position to a new tile
     * @param x the x position
     * @param y the y position
     * @return the previous tile, or null if there was none
     */
    MapTile set(int x, int y, MapTileInstance tile);

    default MapTile set(int x, int y, MapTile newTile, int height, int rotation) {
        return set(x, y, new MapTileInstance(height, rotation, newTile));
    }

    /**
     * return the height of the MapTile on the given position
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the base height / elevation of the tile on the given coordinate, or 0 when the coordinate is out of bounds.
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

    void writeToFile(DataOutput out) throws IOException;

    void readFromFile(DataInput in, MapTile[] mapping) throws IOException;
}
