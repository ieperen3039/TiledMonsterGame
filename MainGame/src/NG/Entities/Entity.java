package NG.Entities;

import NG.CollisionDetection.BoundingBox;
import NG.Core.GameObject;
import NG.Core.GameTimer;
import NG.GameMap.GameMap;
import NG.Rendering.MatrixStack.SGL;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * An entity is anything that is in the world, excluding the ground itself, particles and other purely visual elements.
 * @author Geert van Ieperen. Created on 14-9-2018.
 */
public interface Entity extends GameObject {

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
     * @return the real position of this entity at the given time, or null if the entity does not exist at the given
     * time
     */
    default Vector3f getPositionAt(float gameTime) {
        BoundingBox hitbox = getHitbox(gameTime);
        if (hitbox == null) return null;
        return new Vector3f((hitbox.maxX - hitbox.minX) / 2, (hitbox.maxY - hitbox.minY) / 2, (hitbox.maxZ - hitbox.minX) / 2);
    }

    /**
     * @return a (modifiable) world-space hitbox of this entity
     */
    BoundingBox getHitbox(float gameTime);

    /**
     * Marks the track piece to be invalid, such that the {@link #isDisposed()} method returns true.
     */
    void dispose();

    /**
     * @return true iff this unit should be removed from the game world.
     */
    boolean isDisposed();

    /**
     * calculates the smallest t = [0 ... 1] such that origin + (t * direction) lies on this entity.
     * @param origin    the origin of the ray to cast
     * @param direction the direction of the ray
     * @param gameTime  the time at which to measure
     * @return t = [0 ... 1], with 1 if the given ray piece does not hit this entity
     */
    default float getIntersection(Vector3fc origin, Vector3fc direction, float gameTime) {
        return getHitbox(gameTime).intersectRay(origin, direction);
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
     * same function, as {@code other.collideWith(this, collisionTime)}. This function should take care of dealing
     * damage and applying effects.
     * <p>
     * Should not be called if either {@code this.}{@link #canCollideWith(Entity) canCollideWith}{@code (other)} or
     * {@code other.}{@link #canCollideWith(Entity) canCollideWith}{@code (this)}
     * @param other         another entity
     * @param collisionTime the moment of collision
     */
    default void collideWith(Entity other, float collisionTime) {
    }

    /**
     * process a collision with the map, happening at collisionTime.
     * @param map           the map
     * @param collisionTime the moment of collision
     */
    default void collideWith(GameMap map, float collisionTime) {
    }

    ;
}
