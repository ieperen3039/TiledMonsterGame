package NG.Actions;

import NG.Animations.UniversalAnimation;
import NG.GameEvent.Event;
import NG.Living.Stimulus;
import NG.Tools.Vectors;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * An immutable action with a fixed start position, end position, animation and duration.
 * @author Geert van Ieperen created on 12-2-2019.
 */
public interface EntityAction extends Stimulus {

    /**
     * calculates the position resulting from this action, the given time after the start of this action
     * @param timeSinceStart the time since the start of this animation in seconds
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
     * @return the duration of the action in seconds.
     */
    float duration();


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
     * @return if the position where first ends is the same as the position where this starts.
     * @throws IllegalArgumentException if first is null
     */
    default boolean follows(EntityAction first) {
        if (first == null) {
            throw new IllegalArgumentException("Action is compared to null");
        }

        return Vectors.almostEquals(first.getEndPosition(), getStartPosition());
    }

    default Event getFinishEvent(float startTime, ActionFinishListener listener) {
        return new ActionFinishEvent(this, startTime, listener);
    }

    default Quaternionf getRotationAt(float timeSinceStart) {
        Vector3fc startPosition = getPositionAt(0);
        Vector3fc endPosition = getPositionAt(duration());
        Vector3f relativeMovement = new Vector3f(startPosition).sub(endPosition);
        return Vectors.getPitchYawRotation(relativeMovement);
    }

    class ActionFinishEvent extends Event {
        private EntityAction action;
        private ActionFinishListener listener;

        public ActionFinishEvent(EntityAction action, float startTime, ActionFinishListener listener) {
            super(startTime + action.duration());
            this.action = action;
            this.listener = listener;
        }

        public void run() {
            listener.onActionFinish(action, eventTime);
        }
    }
}
