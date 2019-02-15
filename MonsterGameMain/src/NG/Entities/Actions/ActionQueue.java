package NG.Entities.Actions;

import NG.Engine.Game;
import org.joml.Vector2ic;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.Iterator;

/**
 * A queue of actions with additional robustness checking and a {@link #getPositionAt(float)} method. If the actions
 * added to this queue have undefined start time, the start time is set to be the closest to the other actions in this
 * queue.
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class ActionQueue extends ArrayDeque<EntityAction> {

    // there is always at least one action in the queue.

    /**
     * an action queue with an initial idle action on the given position
     * @param position
     */
    public ActionQueue(Game game, Vector2ic position) {
        this(new ActionIdle(game, position));
    }

    /**
     * @param initialAction a non-null action, like IDLE
     */
    public ActionQueue(EntityAction initialAction) {
        if (initialAction == null) throw new NullPointerException("initial action was null");
        // avoid checking on the queue content
        super.addLast(initialAction);
    }

    public Vector3f getPositionAt(float currentTime) {
        Iterator<EntityAction> iterator = iterator();

        EntityAction previous = iterator.next();
        while (iterator.hasNext()) {
            EntityAction current = iterator.next();

            if (current.getStartTime() > currentTime) {
                return previous.getPositionAt(currentTime);
            }
            previous = current;
        }

        return previous.getPositionAt(currentTime);
    }

    /**
     * remove all actions from the head of this queue that happen before the given time.
     * @param time the time until where to remove actions, exclusive.
     */
    public void removeUntil(float time) {
        while (size() > 1 && getFirst().getEndTime() < time) {
            pollFirst();
        }

        assert size() > 0;
    }

    @Override
    public EntityAction pollFirst() {
        assert size() > 0;
        if (size() == 1) return peekFirst();
        return super.pollFirst();
    }

    @Override
    public void addLast(EntityAction action) {
        assert size() > 0;
        if (action.isUndefined()) {
            float prevEndTime = getLast().getEndTime();
            action.setStartTime(prevEndTime);

        } else if (!action.follows(getLast())) {
            throw new IllegalArgumentException(
                    "New action " + action + " does not follow the last action known " + getLast());
        }

        super.addLast(action);
    }

    @Override
    public void addFirst(EntityAction action) {
        assert size() > 0;
        if (action.isUndefined()) {
            Float firstStart = getFirst().getStartTime();
            action.setStartTime(firstStart - action.duration());

        } else if (!getFirst().follows(action)) {
            throw new IllegalArgumentException(
                    "New action " + action + " does not precede the first action known " + getFirst());
        }

        super.addFirst(action);
    }

    /**
     * @return this queue as a single action
     */
    public EntityAction toAction() {
        return new CompoundAction(toArray(new EntityAction[0]));
    }
}
