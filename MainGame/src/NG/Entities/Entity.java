package NG.Entities;

import NG.CollisionDetection.BoundingBox;
import NG.Engine.GameTimer;
import NG.GameMap.GameMap;
import NG.Rendering.MatrixStack.SGL;
import NG.Settings.Settings;
import org.joml.Vector3f;
import org.joml.Vector3fc;

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
     * Executes when the user clicks on this entity. When {@code button == GLFW_LEFT_MOUSE_BUTTON} is clicked, an {@link
     * NG.GUIMenu.Components.SFrame} with information or settings of this Entity is usually opened, and when
     * {@code button == GLFW_RIGHT_MOUSE_BUTTON} is clicked, the 'active' state of this entity may toggle.
     * @param button the button that is clicked as defined in {@link NG.InputHandling.MouseRelativeClickListener}
     */
    void onClick(int button);

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
    BoundingBox hitbox();

    /**
     * @param other another entity
     * @return false if this entity does not respond on a collision with the other entity. In that case, the other
     * entity should also not respond on a collision with this.
     */
    default boolean canCollideWith(Entity other) {
        return other != this;
    }

    /**
     * process a collision with the other entity, happening at collisionTime. The other entity will be called with this
     * same function, as {@code other.collideWith(this, collisionTime)}.
     * <p>
     * Should not be called if either {@code this.}{@link #canCollideWith(Entity) canCollideWith}{@code (other)} or
     * {@code other.}{@link #canCollideWith(Entity) canCollideWith}{@code (this)}
     * @param other         another entity
     * @param collisionTime the moment of collision
     */
    void collideWith(Object other, float collisionTime);

    default void checkMapCollision(GameMap map, float startTime, float endTime) {
        Vector3fc startPos = getPositionAt(startTime);
        Vector3fc endPos = getPositionAt(endTime);

        float intersect = map.gridMapIntersection(startPos, new Vector3f(endPos).sub(startPos), 1);
        if (intersect == 1) return;

        // collision found
        float collisionTime = startTime + intersect * (endTime - startTime);
        Vector3fc midPos = getPositionAt(collisionTime);

        // only accept if the found position is sufficiently close to a checked point
        while (Math.min(startPos.distanceSquared(midPos), endPos.distanceSquared(midPos)) > Settings.MIN_COLLISION_CHECK_SQ) {
            intersect = map.gridMapIntersection(startPos, new Vector3f(midPos).sub(startPos), 1);

            if (intersect < 1) {
                collisionTime = startTime + intersect * (collisionTime - startTime);
                endPos = midPos;

            } else { // wrong half, repeat with other half
                intersect = map.gridMapIntersection(midPos, new Vector3f(endPos).sub(midPos), 1);
                collisionTime = collisionTime + intersect * (endTime - collisionTime);
                startPos = midPos;
            }
        }

        collideWith(map, collisionTime);
    }
}
