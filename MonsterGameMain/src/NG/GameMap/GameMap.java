package NG.GameMap;

import NG.CollisionDetection.BoundingBox;
import NG.Engine.Game;
import NG.Engine.GameAspect;
import NG.InputHandling.MouseTools.MouseTool;
import NG.InputHandling.MouseTools.MouseToolListener;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import NG.Tools.Vectors;
import org.joml.*;

import java.util.Collection;

/**
 * An object that represents the world where all other entities stand on. This includes both the graphical and the
 * physical representation. The map considers a difference between coordinates and position, in that a coordinate may be
 * of different magnitude than an equivalent position.
 * @author Geert van Ieperen. Created on 29-9-2018.
 */
public interface GameMap extends GameAspect, MouseToolListener, Storable {
    /**
     * generate a map using the provided generator. This method should be run in a separate thread
     * @param mapGenerator the generator to use for this map.
     */
    void generateNew(MapGeneratorMod mapGenerator);

    /**
     * @param position a position in (x, y) coordinates
     * @return the height h of the ground above z=0 on that position, such that vector (x, y, h) lies on the surface of
     * the map, or 0 when the coordinate is out of bounds
     */
    default int getHeightAt(Vector2ic position) {
        return getHeightAt(position.x(), position.y());
    }

    /**
     * calculates the exact height of a given real position
     * @param x the x position
     * @param y the y position
     * @return the real height at the given position, or 0 when it is out of bounds.
     */
    float getHeightAt(float x, float y);

    /**
     * calculates the coordinate height of a coordinate position
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the coordinate height at position (x, y) on the map
     */
    int getHeightAt(int x, int y);

    /**
     * maps a real position to a coordinate
     * @param position a position in real space
     * @return the coordinate that is closest to the given position.
     */
    Vector2i getCoordinate(Vector3fc position);

    /**
     * <p>{@code \result.x == mapCoord.x && result.y == mapCoord.y && result.z = getHeightAt(mapCoord)}</p>
     * @param mapCoord a coordinate on the map
     * @return a vector with xy equal to {@code mapCoord} and the z-coordinate equal to the height of the map on the
     * given coordinate.
     */
    default Vector3i getCoordinate3D(Vector2ic mapCoord) {
        int x = mapCoord.x();
        int y = mapCoord.y();
        return new Vector3i(x, y, getHeightAt(mapCoord));
    }

    /**
     * @param x x-coordinate
     * @param y y-coordinate
     * @return the height on that coordinate
     */
    default Vector3i getCoordinate3D(int x, int y) {
        return new Vector3i(x, y, getHeightAt(x, y));
    }

    /**
     * maps a coordinate to a real position
     * @param mapCoord a coordinate on the map
     * @return a vector such that {@link #getCoordinate(Vector3fc)} will result in {@code mapCoord}
     */
    default Vector3f getPosition(Vector2ic mapCoord) {
        return getPosition(mapCoord.x(), mapCoord.y());
    }

    /**
     * maps a coordinate to a real position
     * @param x an x-coordinate
     * @param y an y-coordinate
     * @return a vector such that {@link #getCoordinate(Vector3fc)} will result in {@code mapCoord}
     */
    Vector3f getPosition(int x, int y);

    /**
     * draws the map on the screen.
     * @param gl the gl object to draw with
     */
    void draw(SGL gl);

    /** @return the collision position or null if there was no collision before (origin + direction) */
    default Vector3f intersectWithSegment(Vector3fc origin, Vector3fc direction) {
        float t = intersectFraction(origin, direction, 1);
        if (t == 1) return null;

        return new Vector3f(direction).mul(t).add(origin);
    }

    /**
     * allows objects to listen for when this map is changed, as a result of {@link #generateNew(MapGeneratorMod)} or
     * possibly an internal reason. Any call to {@link #draw(SGL)}, {@link #getHeightAt(int, int)} etc. will represent
     * the new values as soon as this callback is activated.
     * @param listener the object to notify
     */
    void addChangeListener(ChangeListener listener);

    /**
     * sets highlighted tiles. Previously highlighted tiles are cleared, and the highlight will stay until a new call to
     * setHighlights
     * @param coordinates the coordinates to highlight. The vectors are assumed not to change, the vectors are not
     *                    copied.
     */
    void setHighlights(Vector2ic... coordinates);

    /**
     * the number of coordinates in x and y direction. The real (floating-point) size can be completely different.
     */
    Vector2ic getSize();

    default boolean checkMouseClick(MouseTool tool, int xSc, int ySc, Game game) {
        Vector3f origin = new Vector3f();
        Vector3f direction = new Vector3f();
        Vectors.windowCoordToRay(game, xSc, ySc, origin, direction);

        Vector3f position = intersectWithSegment(origin, direction);
        if (position == null) return false;

        position.z = getHeightAt(position.x, position.y);
        tool.apply(position);
        return true;
    }

    /**
     * finds a path from A to B.
     * @param beginPosition the A position
     * @param target        the B position
     * @param walkSpeed     the maximum steepness (y over x) this unit can walk
     * @param climbSpeed    a function that maps height of a cliff to climb speed
     * @return the shortest path from A (exclusive) to B (inclusive)
     * @see NG.Tools.AStar
     */ // TODO add shortcut for situations where the tiles are close
    Collection<Vector2i> findPath(
            Vector2ic beginPosition, Vector2ic target, float walkSpeed, float climbSpeed
    );

    /**
     * calculates the lowest fraction t such that (origin + direction * t) lies on this map, for 0 <= t < maximum.
     * @param origin    a local origin of a ray
     * @param direction the direction of the ray
     * @param maximum   the maximum value of t
     * @return fraction t of (origin + direction * t), or maximum if it does not hit.
     */
    float intersectFraction(Vector3fc origin, Vector3fc direction, float maximum);

    /**
     * calculates the lowest fraction t such that 0 <= t <= maximum, and that the hitbox can move (t * direction)
     * starting at origin before hitting this map.
     * @param hitbox    the hitbox to consider
     * @param origin    the relative position of the hitbox, origin of the movement
     * @param direction the direction the hitbox is moving in
     * @param maximum   the maximum value of t
     * @return t, or maximum if it did not hit
     */
    default float intersectFractionBoundingBox(
            BoundingBox hitbox, Vector3fc origin, Vector3fc direction, float maximum
    ) {
        for (Vector3f corner : hitbox.corners()) {
            corner.add(origin);

            float newIntersect = intersectFraction(corner, direction, maximum);
            if (newIntersect < maximum) maximum = newIntersect;
        }

        return maximum;
    }

    interface ChangeListener {
        /** is called when the map is changed */
        void onMapChange();
    }

}
