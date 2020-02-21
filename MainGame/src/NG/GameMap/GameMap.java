package NG.GameMap;

import NG.Actions.EntityAction;
import NG.CollisionDetection.BoundingBox;
import NG.Core.GameAspect;
import NG.Entities.Entity;
import NG.Entities.MovingEntity;
import NG.InputHandling.MouseTools.MouseToolListener;
import NG.Rendering.MatrixStack.SGL;
import NG.Settings.Settings;
import NG.Storable;
import org.joml.*;

import java.lang.Math;
import java.util.Collection;

/**
 * @author Geert van Ieperen created on 5-5-2019.
 */
public interface GameMap extends GameAspect, Entity, MouseToolListener, Storable {
    /**
     * generate a map using the provided generator. This method can be run in a separate thread
     * @param mapGenerator the generator to use for this map.
     */
    void generateNew(MapGeneratorMod mapGenerator);

    /**
     * @param position a position in (x, y) coordinates
     * @return the height h of the ground above z=0 on that position, such that vector (x, y, h) lies on the surface of
     * the map, or 0 when the coordinate is out of bounds
     */
    int getHeightAt(Vector2ic position);

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
     * maps a real position to the nearest coordinate. This coordinate may not exist.
     * @param position a position in real space
     * @return the coordinate that is closest to the given position.
     * @see #getCoordinate(Vector3fc)
     * @see #getCoordinate3D(int, int)
     */
    Vector2i getCoordinate(Vector3fc position);

    /**
     * @param x x-coordinate
     * @param y y-coordinate
     * @return a vector of (x, y, z), with z being the height on (x, y)
     */
    Vector3i getCoordinate3D(int x, int y);

    /**
     * maps a coordinate to a real position
     * @param mapCoord a coordinate on the map
     * @return a vector such that {@link #getCoordinate(Vector3fc)} will result in {@code mapCoord}
     */
    Vector3f getPosition(Vector2ic mapCoord);

    /**
     * maps a coordinate to a real position
     * @param x an x-coordinate
     * @param y an y-coordinate
     * @return a vector such that {@link #getCoordinate(Vector3fc)} will result in {@code x} and {@code y}
     */
    Vector3f getPosition(int x, int y);

    /**
     * draws the map on the screen.
     * @param gl the gl object to draw with
     */
    void draw(SGL gl);

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

    /**
     * finds a path from A to B.
     * @param beginPosition the A position
     * @param target        the B position
     * @param walkSpeed     the maximum steepness (y over x) this unit can walk
     * @param climbSpeed    a function that maps height of a cliff to climb speed
     * @return the shortest path from A (exclusive) to B (inclusive)
     * @see NG.Tools.AStar
     */
    Collection<Vector2i> findPath(
            Vector2ic beginPosition, Vector2ic target, float walkSpeed, float climbSpeed
    ); // TODO add shortcut for situations where the tiles are close

    /**
     * calculates the lowest fraction t such that (origin + direction * t) lies on this map, for 0 <= t < maximum.
     * @param origin    a local origin of a ray
     * @param direction the direction of the ray
     * @return fraction t of (origin + direction * t), or maximum if it does not hit.
     */
    default float gridMapIntersection(Vector3fc origin, Vector3fc direction) {
        return getIntersection(origin, direction, 0);
    }

    /**
     * calculates the lowest fraction t such that 0 <= t <= maximum, and such that that the hitbox can move (t * direction)
     * starting at origin before hitting this map.
     * @param hitbox    the hitbox to consider
     * @param origin    the relative position of the hitbox, origin of the movement
     * @param direction the direction the hitbox is moving in
     * @param maximum   the maximum value of t
     * @return t, or maximum if it did not hit
     */
    float intersectFractionBoundingBox(
            BoundingBox hitbox, Vector3fc origin, Vector3fc direction, float maximum
    );

    default float checkCollision(MovingEntity entity, float startTime, float endTime) {
        Vector3fc startPos = entity.getPositionAt(startTime);
        Vector3fc endPos = entity.getPositionAt(endTime);

        float intersect = gridMapIntersection(startPos, new Vector3f(endPos).sub(startPos));
        if (intersect == 1) return 1;

        // collision found
        float collisionTime = startTime + intersect * (endTime - startTime);
        Vector3fc midPos = entity.getPositionAt(collisionTime);

        // only accept if the found position is sufficiently close to a checked point
        while (Math.min(startPos.distanceSquared(midPos), endPos.distanceSquared(midPos)) > Settings.MIN_COLLISION_CHECK_SQ) {
            intersect = gridMapIntersection(startPos, new Vector3f(midPos).sub(startPos));

            if (intersect < 1) {
                collisionTime = startTime + intersect * (collisionTime - startTime);
                endPos = midPos;

            } else { // wrong half, repeat with other half
                intersect = gridMapIntersection(midPos, new Vector3f(endPos).sub(midPos));
                collisionTime = collisionTime + intersect * (endTime - collisionTime);
                startPos = midPos;
            }
        }

        return intersect;
    }

    default boolean isOnFloor(Vector3fc position) {
        return Math.abs(getHeightAt(position.x(), position.y()) - position.z()) < EntityAction.ACCEPTABLE_DIFFERENCE;
    }

    interface ChangeListener {
        /** is called when the map is changed */
        void onMapChange();
    }
}
