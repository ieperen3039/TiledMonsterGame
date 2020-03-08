package NG.Actions;

import NG.Actions.ActionMarkers.ActionMarker;
import NG.Animations.UniversalAnimation;
import NG.Core.Game;
import NG.Core.GameObject;
import NG.Tools.Vectors;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * An immutable action with a fixed start position, end position, animation and duration.
 * @author Geert van Ieperen created on 12-2-2019.
 */
public interface EntityAction extends GameObject {
    float ACCEPTABLE_DIFFERENCE = 1f / 512f;
    float ACCEPTABLE_DIFFERENCE_SQ = ACCEPTABLE_DIFFERENCE * ACCEPTABLE_DIFFERENCE;
    float DIRECTION_DELTA = 1f / 64f;

    @Override
    default void restore(Game game) {
    }

    /**
     * calculates the position of this action, at the given time after the start of this action
     * @param timeSinceStart the time <b>since the start</b> of this action in seconds
     * @return the position at the given moment in time as described by this action.
     */
    Vector3f getPositionAt(float timeSinceStart);

    default Vector3fc getStartPosition() {
        return getPositionAt(0);
    }

    default Vector3fc getEndPosition() {
        return getPositionAt(duration());
    }

    /**
     * @param timeSinceStart the time <b>since the start</b> of this action in seconds
     * @return direction of this action at the given time
     */
    default Vector3f getDerivative(float timeSinceStart) {
        Vector3f a = getPositionAt(timeSinceStart - DIRECTION_DELTA);
        Vector3f b = getPositionAt(timeSinceStart + DIRECTION_DELTA);
        return b.sub(a).div(DIRECTION_DELTA * 2);
    }

    ActionMarker getMarker();

    /**
     * @return the duration of the action in seconds.
     */
    float duration();

    /**
     * checks whether this action may follow the given action
     * @param first an action that happened before this action
     * @return if the position where first ends is the same as the position where this starts.
     * @throws IllegalArgumentException if first is null
     */
    default boolean follows(EntityAction first) {
        if (first == null) {
            throw new IllegalArgumentException("Action is compared to null");
        }

        return Vectors.almostEqual(first.getEndPosition(), getStartPosition());
    }

    /**
     * @return false if this action trivially touches the ground, true if collision detection with the ground is
     * required
     */
    boolean hasWorldCollision();

    /**
     * @return the animation that is played when executing this action
     */
    UniversalAnimation getAnimation();

    /**
     * calculates the yaw rotation of this action, at the given time after the start of this action
     * @param timeSinceStart the time <b>since the start</b> of this action in seconds
     * @return the rotation from the base at the given moment in time as described by this action.
     */
    default Quaternionf getRotationAt(float timeSinceStart) {
        Vector3f dir = getDerivative(timeSinceStart);
        float yaw = Math.atan2(dir.y, dir.x);
        return new Quaternionf().rotateZ(yaw);
    }
}
