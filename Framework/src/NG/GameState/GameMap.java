package NG.GameState;

import NG.ActionHandling.MouseTools.MouseToolListener;
import NG.Engine.GameAspect;
import NG.Rendering.MatrixStack.SGL;
import NG.Tools.Toolbox;
import org.joml.*;

/**
 * An object that represents the world where all other entities stand on. This includes both the graphical and the
 * physical representation.
 * @author Geert van Ieperen. Created on 29-9-2018.
 */
public interface GameMap extends GameAspect, MouseToolListener {
    /**
     * generate a map using the provided generator. This method should be run in a separate thread
     * @param mapGenerator the generator to use for this map.
     */
    void generateNew(MapGeneratorMod mapGenerator);

    /**
     * @param position a position in (x, y) coordinates
     * @return the height of the ground above z=0 on that position, such that vector (x, y, z) lies on the map
     */
    default int getHeightAt(Vector2ic position) {
        return getHeightAt(position.x(), position.y());
    }

    /**
     * @param x an exact x position on the map
     * @param y an exact y position on the map
     * @return the height at position (x, y) on the map
     */
    default float getHeightAt(float x, float y) {
        int ix = (int) x;
        int iy = (int) y;

        float xFrac = x - ix;
        float yFrac = y - iy;
        float x1Lerp = Toolbox.interpolate(getHeightAt(ix, iy), getHeightAt(ix + 1, iy), xFrac);
        float x2Lerp = Toolbox.interpolate(getHeightAt(ix, iy + 1), getHeightAt(ix + 1, iy + 1), xFrac);
        return Toolbox.interpolate(x1Lerp, x2Lerp, yFrac);
    }

    /**
     * <p>{@code \result.x == mapCoord.x && result.y == mapCoord.y && result.z = getHeightAt(mapCoord)}</p>
     * @param mapCoord a coordinate on the map
     * @return a vector with xy equal to {@code mapCoord} and the z-coordinate equal to the height of the map on the
     * given coordinate.
     */
    default Vector3i getPosition(Vector2ic mapCoord) {
        int x = mapCoord.x();
        int y = mapCoord.y();
        return new Vector3i(x, y, getHeightAt(mapCoord));
    }

    /**
     * @param x x-coordinate
     * @param y y-coordinate
     * @return the height on that coordinate
     */
    default Vector3i getPosition(int x, int y) {
        return new Vector3i(x, y, getHeightAt(x, y));
    }

    /**
     * maps a 2D map coordinate to a 3D position. Returns a vector with z == 0 if no map is loaded.
     * <p>{@code \result.x == mapCoord.x && result.y == mapCoord.y}</p>
     * @param mapCoord a 2D map coordinate
     * @return the 2D coordinate mapped to the surface of the inital map.
     */
    default Vector3f getPosition(Vector2fc mapCoord) {
        float yLerp = getHeightAt(mapCoord.x(), mapCoord.y());

        return new Vector3f(
                mapCoord.x(),
                mapCoord.y(),
                yLerp
        );
    }

    /**
     * @param x an exact x position on the map
     * @param y an exact y position on the map
     * @return the height at position (x, y) on the map
     */
    int getHeightAt(int x, int y);

    /**
     * draws the map on the screen.
     * @param gl the gl object to draw with
     */
    void draw(SGL gl);

    /**
     * returns a vector on the map that results from raytracing the given ray.
     * @param origin    the origin of the ray
     * @param direction the (un-normalized) direction of the ray
     * @return a vector p such that {@code p = origin + t * direction} for minimal t such that p lies on the map.
     */
    Vector3f intersectWithRay(Vector3fc origin, Vector3fc direction);

    /**
     * allows objects to listen for when this map is changed, as a result of {@link #generateNew(MapGeneratorMod)} or
     * possibly an internal reason. Any call to {@link #draw(SGL)}, {@link #getHeightAt(int, int)} etc. will represent
     * the new values as soon as this callback is activated.
     * @param listener the object to notify
     */
    void addChangeListener(ChangeListener listener);

    interface ChangeListener {
        /** is called when the map is changed */
        void onMapChange();
    }
}
