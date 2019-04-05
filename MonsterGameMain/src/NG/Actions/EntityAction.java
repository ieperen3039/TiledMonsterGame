package NG.Actions;

import NG.Animations.UniversalAnimation;
import NG.GameEvent.Event;
import NG.MonsterSoul.Stimulus;
import NG.Tools.Vectors;
import org.joml.Quaternionf;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * An immutable action that is completely set in time and space
 * @author Geert van Ieperen created on 12-2-2019.
 */
public interface EntityAction extends Stimulus {

    /**
     * calculates the position resulting from this action, the given time after the start of this action
     * @param currentTime the current time in seconds
     * @return the position at the given moment in time as described by this action.
     */
    Vector3f getPositionAt(float currentTime);

    Vector2ic getStartCoordinate();

    Vector2ic getEndCoordinate();

    /**
     * @return the timestamp when the action starts
     */
    float startTime();

    /**
     * @return the timestamp when the action stops
     */
    float endTime();

    @Override
    default float getMagnitude(Vector3fc position) {
        return 1;
    }

    /**
     * @return the animation that is played when executing this action
     */
    UniversalAnimation getAnimation();

    /**
     * checks whether this action may follow the given action
     * @param first an action that happened before this action
     * @return if the position where first ends is the same as the position
     * where this starts.
     * @throws IllegalArgumentException if first is null
     */
    default boolean follows(EntityAction first) {
        if (first == null) {
            throw new IllegalArgumentException("Action is compared to null");
        }

        Vector2ic firstEndPos = first.getEndCoordinate();
        return firstEndPos.equals(getStartCoordinate());
    }

    default Event getFinishEvent(ActionFinishListener listener) {
        return new Event.Anonymous(endTime(), () -> listener.onActionFinish(this));
    }

    default Quaternionf getRotationAt(float currentTime) {
        Vector3fc startPosition = getPositionAt(startTime());
        Vector3fc endPosition = getPositionAt(endTime());
        Vector3f relativeMovement = new Vector3f(endPosition).sub(startPosition);
        return Vectors.getPitchYawRotation(relativeMovement);
    }

    default float progressAt(float currentTime) {
        return (currentTime - startTime()) / (endTime() - startTime());
    }
}
