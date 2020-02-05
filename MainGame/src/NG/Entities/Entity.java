package NG.Entities;

import NG.CollisionDetection.BoundingBox;
import NG.Core.GameTimer;
import NG.GameMap.GameMap;
import NG.Rendering.MatrixStack.SGL;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

/**
 * An entity is anything that is in the world, excluding the ground itself. Particles and other purely visual elements.
 * @author Geert van Ieperen. Created on 14-9-2018.
 */
public interface Entity {

    /**
     * Draws this entity using the provided SGL object. This method may only be called from the rendering loop, and
     * should not change the internal representation of this object. Possible animations should be based on {@link
     * GameTimer#getRendertime()}. Material must be set using {@link SGL#getShader()}.
     * @param gl the graphics object to be used for rendering. It is initialized at world's origin. (no translation or
     *           scaling has been applied)
     */
    void draw(SGL gl);

    /**
     * updates the control and actions of this entity
     * @param gameTime the current game time
     */
    void update(float gameTime);

    /**
     * The position of this entity. If the entity does not exist on the given time, the result is null.
     * @param currentTime
     * @return the real position of this entity at the given time, or null if the entity does not exist at the given
     * time
     */
    Vector3f getPositionAt(float currentTime);

    /**
     * Marks the track piece to be invalid, such that the {@link #isDisposed()} method returns true.
     */
    void dispose();

    /**
     * @return true iff this unit should be removed from the game world.
     */
    boolean isDisposed();

    /**
     * @return the relative (local-space) bounding box of this entity
     */
    BoundingBox getHitbox();

    /**
     * given a point on position {@code origin} and a direction of {@code direction}, calculates the fraction t in [0
     * ... 1] such that (origin + direction * t) is the first point on this entity
     * @param origin    the origin of the line
     * @param direction the direction and extend of the line
     * @param gameTime
     * @return the value t such that (origin + direction * t) is the first point on this entity, or 1 if it does not
     * hit.
     */
    float getIntersection(Vector3fc origin, Vector3fc direction, float gameTime);

    /**
     * returns the points of the shape of this entity at the given moment in time
     * @param gameTime the moment when to retrieve this entity's points
     * @return a list of the exact wolrd-positions of the vertices of the shape of this object. Changes in the list are
     * not reflected in this object.
     */
    default List<Vector3f> getShapePoints(float gameTime) {
        return getShapePoints(new ArrayList<>(), gameTime);
    }

    /**
     * returns the points of the shape of this entity at the given moment in time, and store the result in the vectors
     * of dest.
     * @param dest     a list of vectors. If the result requires more or less elements, this parameter may be ignored.
     * @param gameTime the moment when to retrieve this entity's points
     * @return a list of the exact world-positions of the vertices of the shape of this object. Changes in the list are
     * not reflected in this object.
     */
    default List<Vector3f> getShapePoints(List<Vector3f> dest, float gameTime) {
        if (dest.size() > 8) {
            dest.clear();
        }
        if (dest.size() < 8) {
            for (int i = dest.size(); i < 8; i++) {
                dest.add(new Vector3f());
            }
        }

        Vector3f pos = getPositionAt(gameTime);
        int i = 0;
        for (Vector3f corner : new BoundingBox(getHitbox(), pos).corners()) {
            dest.get(i).set(corner);
            i++;
        }
        return dest;
    }

    /**
     * @param other another entity
     * @return false if this entity does not respond on a collision with the other entity. In that case, the other
     * entity should also not respond on a collision with this.
     */
    default boolean canCollideWith(Entity other) {
        return (other != this && other instanceof MovingEntity);
    }

    /**
     * process a collision with the other entity, happening at collisionTime. The other entity will be called with this
     * same function, as {@code other.collideWith(this, collisionTime)}. This function should take care of dealing damage
     * and applying effects.
     * <p>
     * Should not be called if either {@code this.}{@link #canCollideWith(Entity) canCollideWith}{@code (other)} or
     * {@code other.}{@link #canCollideWith(Entity) canCollideWith}{@code (this)}
     * @param other         another entity
     * @param collisionTime the moment of collision
     */
    void collideWith(Entity other, float collisionTime);

    /**
     * process a collision with the map, happening at collisionTime.
     * @param map         the map
     * @param collisionTime the moment of collision
     */
    void collideWith(GameMap map, float collisionTime);

}
