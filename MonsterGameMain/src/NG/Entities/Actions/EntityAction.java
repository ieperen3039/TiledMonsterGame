package NG.Entities.Actions;

import org.joml.Vector3f;

/**
 * @author Geert van Ieperen created on 12-2-2019.
 */
public interface EntityAction {
    Float TIME_UNDEFINED = Float.MAX_VALUE;

    /**
     * calculates the position resulting from this action, the given time after the start of this action
     * @param passedTime the time of measurement in seconds
     * @return the position at the given moment in time as described by this action.
     */
    Vector3f getPositionAfter(float passedTime);

    default Vector3f getStartPosition() {
        return getPositionAfter(0);
    }

    default Vector3f getEndPosition() {
        return getPositionAfter(duration());
    }

    /**
     * @return the duration of the action in seconds.
     */
    float duration();

    /**
     * stops this action on the first possibility. This may not happen until the end of this action, and will always
     * result in the end position to be a coordinate. The default behaviour is that noting happens, and {@link
     * #duration()} is returned.
     * @param passedTime the moment the interrupt is activated
     * @return the actual moment this action is interrupted
     */
    default float interrupt(float passedTime) {
        return duration();
    }

    /**
     * checks whether this action may follow the given action
     * @param first an action that happened before this action
     * @return if first happens strictly before this, and the position where first ends is the same as the position
     * where this starts.
     * @throws IllegalArgumentException if first is null
     */
    default boolean follows(EntityAction first) {
        if (first == null) {
            throw new IllegalArgumentException("Action is compared to null");
        }

        Vector3f firstEndPos = first.getEndPosition();
        return firstEndPos.equals(getStartPosition());
    }
}
