package NG.GameMap;

import NG.ActionHandling.MouseTools.MouseToolListener;
import NG.Engine.GameAspect;
import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import org.joml.*;

import java.util.ArrayList;
import java.util.List;

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
     * <p>{@code \result.x == mapCoord.x && result.y == mapCoord.y && result.z = getHeightAt(mapCoord)}</p>
     * @param mapCoord a coordinate on the map
     * @return a vector with xy equal to {@code mapCoord} and the z-coordinate equal to the height of the map on the
     * given coordinate.
     */
    default Vector3i getCoordinate(Vector2ic mapCoord) {
        int x = mapCoord.x();
        int y = mapCoord.y();
        return new Vector3i(x, y, getHeightAt(mapCoord));
    }

    /**
     * @param x x-coordinate
     * @param y y-coordinate
     * @return the height on that coordinate
     */
    default Vector3i getCoordinate(int x, int y) {
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
     * @param x an exact x position on the map
     * @param y an exact y position on the map
     * @return the height at position (x, y) on the map
     */
    int getHeightAt(int x, int y);

    /**
     * maps a real position to a coordinate
     * @param position a position in real space
     * @return a coordinate that is closest to the given position.
     */
    Vector3i getCoordinate(Vector3fc position);

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

    /**
     * returns a vector on the map that results from raytracing the given ray.
     * @param origin    the origin of the ray
     * @param direction the (un-normalized) direction of the ray
     * @return a vector p such that {@code p = origin + t * direction} for minimal t such that p lies on the map.
     */
    Vector3f intersectWithRay(Vector3fc origin, Vector3fc direction);

    /**
     * try to assign the given entity to the given coordinate. If there is already an entity on the coordinate, no claim
     * is made and false is returned
     * @param coordinate a coordinate on the map
     * @param entity     the entity that wants to enter the coordinate
     * @return true if the claim was made, false if the claim could not be made.
     * @throws NullPointerException when entity or coordinate is null
     * @see #dropClaim(Vector2ic, Entity)
     */
    boolean createClaim(Vector2ic coordinate, Entity entity);

    /**
     * queries what entity owns the claim on the given coordinate.
     * @param coordinate a coordinate on the map
     * @return the entity that claimed the coordinate, and didn't drop it's claim yet
     */
    Entity getClaim(Vector2ic coordinate);

    /**
     * release a previously made claim
     * @param coordinate a coordinate on the map
     * @param entity     the entity that has claimed the given coordinate, or null to drop it regardless
     * @return true if the entity previously owned the claim, or if entity is null. When false is returned, the claim
     * register is not updated
     */
    boolean dropClaim(Vector2ic coordinate, Entity entity);

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
     * the number of tiles in x and y direction
     */
    Vector2ic getSize();

    /**
     * finds a path from A to B.
     * @param beginPosition the A position
     * @param target        the B position
     * @return a path from A (exclusive) to B (inclusive) where the height differences are at most 1.
     */
    static List<Vector2i> findPath(Vector2ic beginPosition, Vector2ic target) {
        return new ArrayList<>();
    }

    interface ChangeListener {
        /** is called when the map is changed */
        void onMapChange();
    }
}
