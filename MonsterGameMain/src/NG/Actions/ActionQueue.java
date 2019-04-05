package NG.Actions;

import NG.Engine.Game;
import NG.MonsterSoul.Commands.CompoundAction;
import org.joml.Vector2ic;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.List;

/**
 * A queue of actions with additional robustness checking and a {@link #getPositionAt(float)} method. If the actions
 * added to this queue have undefined start time, the start time is set to be the closest to the other actions in this
 * queue. This collection is not synchronized.
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class ActionQueue extends ArrayDeque<EntityAction> {
// there is always at least one action in the queue.

    /** start time of the next action */
    private float firstActionStart;

    /** end time of the last action */
    private float lastActionEnd;

    /**
     * an action queue with an initial idle action on the given position
     * @param position initial position
     */
    public ActionQueue(Game game, Vector2ic position) {
        this(new ActionIdle(game, position, 0), 0f);
    }

    /**
     * @param initialAction   a non-null action, like {@link ActionIdle}
     * @param actionStartTime the time at which this action is started.
     */
    public ActionQueue(EntityAction initialAction, float actionStartTime) {
        if (initialAction == null) throw new NullPointerException("initial action was null");
        setToAction(initialAction, actionStartTime);
    }

    /**
     * returns the position according to the action executing at the given time
     * @param currentTime the moment to query
     * @return the position described by this action queue.
     */
    public Vector3f getPositionAt(float currentTime) {
        EntityAction action = getActionAt(currentTime);
        return action.getPositionAt(currentTime);
    }

    /**
     * remove all actions from the head of this queue that happen before the given time.
     * @param time the time until where to remove actions, exclusive.
     */
    public void removeUntil(float time) {
        if (time < firstActionStart || size() == 1) return;

        if (time > lastActionEnd) { // size > 1
            EntityAction remaining = peekLast();
            clear();
            super.addLast(remaining);
            // recalculate start time of the last action
            firstActionStart = remaining.startTime();
            return;
        }

        while (size() > 1) {
            float firstStart = peekFirst().startTime();
            if (firstStart > time) return;
            pollFirst();
        }
    }

    @Override
    public void addFirst(EntityAction action) {
        assert size() > 0;
        if (!getFirst().follows(action)) {
            throw new IllegalArgumentException(
                    "New action " + action + " does not precede the first action known " + getFirst());
        }

        firstActionStart = action.startTime();
        super.addFirst(action);
    }

    @Override
    public void addLast(EntityAction action) {
        assert size() > 0;
        if (!action.follows(getLast())) {
            throw new IllegalArgumentException(
                    "New action " + action + " does not follow the last action known " + getLast());
        }

        lastActionEnd = action.endTime();
        super.addLast(action);
    }

    @Override
    public EntityAction pollFirst() {
        assert size() > 0;
        if (size() == 1) return peekFirst();

        EntityAction firstAction = super.pollFirst();
        //noinspection ConstantConditions
        firstActionStart = firstAction.startTime();

        return firstAction;
    }

    @Override
    public EntityAction pollLast() {
        assert size() > 0;
        if (size() == 1) return peekLast();

        EntityAction lastAction = super.pollLast();
        //noinspection ConstantConditions
        lastActionEnd = lastAction.endTime();

        return lastAction;
    }

    /**
     * adds the given action to the queue, executing not earlier, but possibly later than the given time. The action
     * will always be placed last in execution. If there are any action in this queue executing any later than the given
     * start time, then this action will execute only after the last of those actions are executed.
     * @param startTime the minimal start time of this action.
     * @param action    the action to add to the queue
     */
    public void addAfter(float startTime, EntityAction action) {
        if (startTime > lastActionEnd) {
            addWait(startTime - lastActionEnd);
        }

        addLast(action);
    }

    /**
     * adds the given actions to the queue, executing not earlier, but possibly later than the given time. The new
     * actions all happen at the end of the existing queue of actions. If actions is empty, nothing will happen and the
     * startTime is ignored.
     * @param actions   a number of actions.
     * @param startTime the start time of the first action in the list.
     */
    public void addAllAfter(List<EntityAction> actions, float startTime) {
        if (actions.isEmpty()) return;

        if (startTime > lastActionEnd) {
            addWait(startTime - lastActionEnd);
        }

        addAll(actions);
    }

    /**
     * sets the action to execute at the given start time, removing any action remaining in the queue. The given action
     * may be delayed to let the executing action finish.
     * @param action    the action to do
     * @param startTime the moment of interrupt
     */
    public void insert(EntityAction action, float startTime) {
        if (startTime > lastActionEnd) {
            addAfter(startTime, action);
            return;
        }

        if (startTime < firstActionStart) {
            setToAction(action, startTime);
            return;
        }

        EntityAction found = removeLast();

        // remove future actions until we found the target action
        while (found.startTime() > startTime) {
            found = removeLast();
        }

        // return the action executing on the given time
        addLast(found);
        addLast(action);
    }

    /**
     * finds the action that is executing at the given time, and calculates how far this action is executed.
     * <p>
     * Note that the eventual execution of this action on this time is not certain, as calls to {@link
     * #insert(EntityAction, float)} and {@link #removeLast()} may change this.
     * @param currentTime a moment in time
     * @return the action that should be taking place under normal circumstances.
     */
    public EntityAction getActionAt(float currentTime) {
        if (currentTime <= firstActionStart) {
            return peekFirst();

        } else if (currentTime >= lastActionEnd) {
            return peekLast();
        }

        // a currently executing action...

        for (EntityAction action : this) {
            if (action.endTime() >= currentTime) {
                assert action.startTime() <= currentTime;
                return action;
            }
        }

        throw new AssertionError("currentTime >= lastActionEnd");
    }

    /**
     * sets the content of this queue to contain only the given action, starting at the given time.
     * @param action    an action
     * @param startTime a start time of this action
     */
    private void setToAction(EntityAction action, float startTime) {
        clear();
        super.addLast(action);
        this.firstActionStart = startTime;
        lastActionEnd = action.endTime();
    }

    /**
     * adds a fixed idling after executing all actions in the queue
     * @param duration the exact duration of the idling. Must be positive
     */
    public void addWait(float duration) {
        assert duration >= 0;
    }

    /**
     * @return this queue as a single action
     */
    public EntityAction toAction() {
        return new CompoundAction(toArray(new EntityAction[0]));
    }

    @Override
    public String toString() {
        return String.format("ActionQueue (time:[%1.02f, %1.02f]: actions %s)", firstActionStart, lastActionEnd, super.toString());
    }
}
