package NG.GameMap;

import NG.Engine.Game;
import NG.Engine.GameAspect;
import NG.Entities.Entity;
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
     * calculates the height of a given real position
     * @param x the x position
     * @param y the y position
     * @return the real height at the given position, or 0 when it is out of bounds.
     */
    float getHeightAt(float x, float y);

    /**
     * @param x an exact x position on the map
     * @param y an exact y position on the map
     * @return the coordinate height at position (x, y) on the map
     */
    int getHeightAt(int x, int y);

    /**
     * maps a real position to a coordinate
     * @param position a position in real space
     * @return a coordinate that is closest to the given position.
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
     * maps a 2D map coordinate to a 3D position. Returns a vector with z == 0 if no map is loaded.
     * <p>{@code \result.x == mapCoord.x && result.y == mapCoord.y}</p>
     * @param position a position on the map
     * @return the 2D coordinate mapped to the surface of the game map.
     */
    default Vector3f getPosition(Vector2fc position) {
        return new Vector3f(
                position.x(),
                position.y(),
                getHeightAt(position.x(), position.y())
        );
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

    default Vector3f intersectWithRay(Vector3fc origin, Vector3fc direction) {
        float t = intersectFraction(origin, direction, 1);
        if (t == 1) return null;

        Vector3f temp = new Vector3f();
        return origin.add(direction.mul(t, temp), temp);
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

        Vector3f position = intersectWithRay(origin, direction);
        if (position == null) return false;

        tool.apply(position);
        return true;
    }

    /**
     * finds a path from A to B.
     * @param beginPosition the A position
     * @param target        the B position
     * @param walkSpeed the maximum steepness (y over x) this unit can walk
     * @param climbSpeed a function that maps height of a cliff to climb speed
     * @return the shortest path from A (exclusive) to B (inclusive)
     * @see NG.Tools.AStar
     */ // TODO add shortcut for situations where the tiles are close
    Collection<Vector2i> findPath(
            Vector2ic beginPosition, Vector2ic target, float walkSpeed, float climbSpeed
    );

    float intersectFraction(Vector3fc origin, Vector3fc direction, float maximum);

    default void checkEntityCollision(Entity monster, float startTime, float endTime) {
        Vector3fc startPos = monster.getPositionAt(startTime);
        Vector3fc endPos = monster.getPositionAt(endTime);

        float intersect = intersectFraction(startPos, new Vector3f(endPos).sub(startPos), 1);
        if (intersect == 1) return;

        float collisionTime = startTime + intersect * (endTime - startTime);
        monster.collideWith(this, collisionTime);
    }

    interface ChangeListener {
        /** is called when the map is changed */
        void onMapChange();
    }

}
