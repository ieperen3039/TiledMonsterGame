package NG.GameEvent.Actions;

import NG.GameEvent.Event;
import NG.MonsterSoul.Stimulus;
import org.joml.Vector2ic;
import org.joml.Vector3fc;

/**
 * An immutable action with a fixed start position, end position and duration.
 * @author Geert van Ieperen created on 12-2-2019.
 */
public interface EntityAction extends Stimulus {

    /**
     * calculates the position resulting from this action, the given time after the start of this action
     * @param passedTime the time of measurement in seconds
     * @return the position at the given moment in time as described by this action.
     */
    Vector3fc getPositionAfter(float passedTime);

    Vector2ic getStartPosition();

    Vector2ic getEndPosition();

    /**
     * @return the duration of the action in seconds.
     */
    float duration();

    @Override
    default float getMagnitude(Vector3fc position) {
        return 1;
    }

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

        Vector2ic firstEndPos = first.getEndPosition();
        return firstEndPos.equals(getStartPosition());
    }

    default Event getFinishEvent(float startTime, ActionFinishListener listener) {
        return new Event.Anonymous(startTime + duration(), () -> listener.onActionFinish(this));
    }
}
