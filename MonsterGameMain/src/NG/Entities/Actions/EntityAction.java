package NG.Entities.Actions;

import org.joml.Vector3f;

import java.util.Objects;

/**
 * @author Geert van Ieperen created on 12-2-2019.
 */
public interface EntityAction {
    Float TIME_UNDEFINED = Float.MAX_VALUE;

    /**
     * @param currentTime the time of measurement in seconds
     * @return true iff the given time is more than {@link #getStartTime()} ()} and less than {@link #getEndTime()}
     */
    default boolean isActive(float currentTime) {
        if (isUndefined()) return false;
        return getStartTime() < currentTime && currentTime < getEndTime();
    }

    default boolean isUndefined() {
        return Objects.equals(getStartTime(), TIME_UNDEFINED);
    }

    /**
     * calculates the position on the given moment in time according to this movement.
     * @param currentTime the time of measurement in seconds
     * @return the position at the given moment in time as described by this action. If currentTime is less or equal
     * than {@link #getStartTime()}, the start position is given. If currentTime is more or equal than {@link
     * #getEndTime()}, the end position is given.
     */
    Vector3f getPositionAt(float currentTime);

    /**
     * @return the duration of the action in seconds.
     */
    float duration();

    /**
     * @return the time at which this action starts in seconds, or null if this is undefined. Whether this is defined
     * depends on the implementation, but it always is after a call to {@link #setStartTime(float)}.
     */
    Float getStartTime();

    /**
     * Sets the start time of this action. {@link #getStartTime()} will be defined after a call to this method.
     * @param time the time at which to start this action.
     */
    void setStartTime(float time);

    /**
     * sets the start time of this action to be after the given action
     * @param first another action with its start and end time defined.
     */
    default void setToFollow(EntityAction first) {
        if (first.isUndefined()) throw new StartTimeUndefinedException("Given action has undefined start time");

        setStartTime(first.getEndTime());
    }

    /**
     * @return the time at which this action ends in seconds, or null when undefined. This is only defined when {@link
     * #getStartTime()} is defined.
     */
    default Float getEndTime() {
        if (isUndefined()) return TIME_UNDEFINED;

        return getStartTime() + duration();
    }

    /**
     * stops this action on the first possibility. This may not happen until the end of this action, and will always
     * result in the end position to be a coordinate. The {@link #getEndTime()} will be updated to match the interrupted
     * end time.
     * @param moment the moment the interrupt is activated
     */
    void interrupt(float moment);

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

        if (isUndefined()) {
            throw new StartTimeUndefinedException("This action has no start time set");
        }

        float thisStartTime = getStartTime();
        if (thisStartTime > first.getStartTime()) {
            return false;
        }

        if (first.isUndefined()) {
            throw new StartTimeUndefinedException("Given action has no start time set");
        }

        float firstEndTime = first.getEndTime();
        if (thisStartTime < firstEndTime) {
            return false;
        }

        Vector3f firstEndPos = first.getPositionAt(firstEndTime);
        Vector3f secondStartPos = getPositionAt(thisStartTime);
        return firstEndPos.equals(secondStartPos);
    }

    class StartTimeUndefinedException extends IllegalStateException {
        StartTimeUndefinedException(String s) {
            super(s);
        }
    }
}
