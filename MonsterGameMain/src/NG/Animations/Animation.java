package NG.Animations;

import NG.Entities.Entity;
import NG.GameEvent.Actions.EntityAction;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import org.joml.Quaternionf;

/**
 * @author Geert van Ieperen created on 1-3-2019.
 */
public interface Animation extends Storable {
    /**
     * Draw the animation, applying to the specified entity, as if it were executing the given action
     * @param gl             the gl instance
     * @param entity         the entity to animate
     * @param action         the action currently being executed by the given entity, started at the same time of this
     *                       animation.
     * @param timeSinceStart time since the start of this animation, and of the given action.
     */
    void draw(SGL gl, Entity entity, EntityAction action, float timeSinceStart);

    /**
     * @param bone           a bone considered by this animation
     * @param timeSinceStart the time since the start of this animation
     * @return the rotation angles of the joint that controls this specific bone, with on each axis the angles that is
     * rotated around that axis.
     * @throws IllegalArgumentException if the given bone is not part of this animation
     */
    Quaternionf rotationOf(AnimationBone bone, float timeSinceStart);
}
