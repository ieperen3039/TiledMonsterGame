package NG.GameMap;

import NG.Actions.EntityAction;
import NG.CollisionDetection.BoundingBox;
import NG.Core.GameAspect;
import NG.Entities.Entity;
import NG.InputHandling.MouseTools.MouseToolListener;
import NG.Rendering.MatrixStack.SGL;
import NG.Settings.Settings;
import NG.Tools.Logger;
import org.joml.*;

import java.io.Externalizable;
import java.lang.Math;
import java.util.Collection;

import static NG.Actions.EntityAction.ACCEPTABLE_DIFFERENCE;
import static NG.Actions.EntityAction.DIRECTION_DELTA;

/**
 * @author Geert van Ieperen created on 5-5-2019.
 */
public interface GameMap extends GameAspect, Entity, MouseToolListener, Externalizable {

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

    /** returns the floating-point transformation to coordinates */
    Vector2f getCoordPosf(Vector3fc position);

    /** returns the floating-point transformation to coordinates */
    Vector2f getCoordDirf(Vector3fc direction);

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
     * calculates the lowest fraction t such that (origin + direction * t) lies on this map, for 0 <= t < 1.
     * @param origin    a local origin of a ray
     * @param direction the direction of the ray
     * @return fraction t of (origin + direction * t), or null if it does not hit.
     */
    Float gridMapIntersection(Vector3fc origin, Vector3fc direction);

    @Override
    default float getIntersection(Vector3fc origin, Vector3fc direction, float gameTime) {
        Float intersect = gridMapIntersection(origin, direction);
        return intersect == null ? 1 : intersect;
    }

    /**
     * calculates the lowest fraction t such that 0 <= t <= maximum, and such that that the hitbox can move (t *
     * direction) starting at origin before hitting this map.
     * @param hitbox    the hitbox to consider
     * @param origin    the relative position of the hitbox, origin of the movement
     * @param direction the direction the hitbox is moving in
     * @param maximum   the maximum value of t
     * @return t, or null if it did not hit
     */
    float intersectFractionBoundingBox(
            BoundingBox hitbox, Vector3fc origin, Vector3fc direction, float maximum
    );

    /**
     * calculates when the given action hits this map using newton's iterations
     * @param action     the action describing a movement
     * @param lowerBound lower bound of the time since the start of the action to consider, larger than 0
     * @param upperBound upper bound on the time since the start of the action to consider, smaller than
     *                   action.duration()
     * @return the relative collision time of action with the map, or null if no collision is found.
     */
    default Float getActionCollision(EntityAction action, float lowerBound, float upperBound) {
        assert lowerBound >= 0;
        assert upperBound <= action.duration();

        // perform iterative checks of MAX_COLLISION_DELTA
        Float intersect = null;
        while (upperBound > lowerBound + Settings.MAX_COLLISION_DELTA_TIME && intersect == null) {
            intersect = getActionCollision(action, lowerBound, lowerBound + Settings.MAX_COLLISION_DELTA_TIME);
            lowerBound = lowerBound + Settings.MAX_COLLISION_DELTA_TIME;
        }

        Vector3fc startPos = action.getPositionAt(lowerBound);
        Vector3fc endPos = action.getPositionAt(upperBound);

        intersect = gridMapIntersection(startPos, new Vector3f(endPos).sub(startPos));

        // edge case: immediate collision, but still legal
        if (intersect != null && intersect * (upperBound - lowerBound) < DIRECTION_DELTA) {
            Vector3fc delta = action.getPositionAt(lowerBound + DIRECTION_DELTA);
            if (delta.z() > getHeightAt(delta.x(), delta.y())) {
                intersect = gridMapIntersection(startPos, new Vector3f(endPos).sub(startPos));
            }
        }

        if (intersect == null) {
            // additional check for ground-to-fall
            if (isOnFloor(startPos)) {
                boolean isBelowGround = endPos.z() < getHeightAt(endPos.x(), endPos.y());
                if (isBelowGround) {
                    Logger.WARN.print(endPos, getHeightAt(endPos.x(), endPos.y()));
                    return 0f;
                }
            }

            return null;
        }

        // collision found
        float collisionTime = lowerBound + intersect * (upperBound - lowerBound);
        Vector3fc midPos = action.getPositionAt(collisionTime);

        // only accept if the found position is sufficiently close to a checked point
        while (Math.min(startPos.distanceSquared(midPos), endPos.distanceSquared(midPos)) > Settings.MIN_COLLISION_CHECK_DISTANCE) {
            intersect = gridMapIntersection(startPos, new Vector3f(midPos).sub(startPos));

            if (intersect != null) {
                collisionTime = lowerBound + intersect * (collisionTime - lowerBound);
                endPos = midPos;

            } else { // wrong half, repeat with other half
                intersect = gridMapIntersection(midPos, new Vector3f(endPos).sub(midPos));
                if (intersect == null) return null; // after smoothing, no collision is found

                collisionTime = collisionTime + intersect * (upperBound - collisionTime);
                startPos = midPos;
            }

            midPos = action.getPositionAt(collisionTime);
        }

        return collisionTime;
    }

    default boolean isOnFloor(Vector3fc position) {
        return Math.abs(getHeightAt(position.x(), position.y()) - position.z()) < ACCEPTABLE_DIFFERENCE;
    }

    /** increases the x or y coordinate in the given direction */
    static void expandCoord(Vector2i coordinate, Vector3f direction) {
        if (Math.abs(direction.x) > Math.abs(direction.y)) {
            if (direction.x > 0) {
                coordinate.x++;
            } else {
                coordinate.x--;
            }
        } else {
            if (direction.y > 0) {
                coordinate.y++;
            } else {
                coordinate.y--;
            }
        }
    }

    interface ChangeListener {
        /** is called when the map is changed */
        void onMapChange();
    }
}
