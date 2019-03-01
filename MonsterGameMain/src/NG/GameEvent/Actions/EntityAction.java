package NG.GameEvent.Actions;

import NG.Animations.Animation;
import NG.Animations.BodyModel;
import NG.GameEvent.Event;
import NG.MonsterSoul.Stimulus;
import NG.Tools.Vectors;
import org.joml.Quaternionf;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * An immutable action with a fixed start position, end position and duration.
 * @author Geert van Ieperen created on 12-2-2019.
 */
public interface EntityAction extends Stimulus {

    /**
     * calculates the position resulting from this action, the given time after the start of this action
     * @param timeSinceStart the time since the start of this animation in seconds
     * @return the position at the given moment in time as described by this action.
     */
    Vector3fc getPositionAfter(float timeSinceStart);

    Vector2ic getStartCoordinate();

    Vector2ic getEndCoordinate();

    Animation getAnimation(BodyModel model);

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

        Vector2ic firstEndPos = first.getEndCoordinate();
        return firstEndPos.equals(getStartCoordinate());
    }

    default Event getFinishEvent(float startTime, ActionFinishListener listener) {
        return new Event.Anonymous(startTime + duration(), () -> listener.onActionFinish(this));
    }

    default Quaternionf getRotation(float timeSinceStart) {
        Vector3fc startPosition = getPositionAfter(0);
        Vector3fc endPosition = getPositionAfter(duration());
        Vector3f relativeMovement = new Vector3f(startPosition).sub(endPosition);
        return Vectors.getPitchYawRotation(relativeMovement);
    }
}
