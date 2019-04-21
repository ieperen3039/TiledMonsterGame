package NG.Actions;

import NG.DataStructures.Generic.Pair;
import NG.Engine.Game;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.Iterator;

/**
 * A queue of actions with additional robustness checking and a {@link #getPositionAt(float)} method. If the actions
 * added to this queue have undefined start time, the start time is set to be the closest to the other actions in this
 * queue. This collection is not synchronized.
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class ActionQueue extends AbstractQueue<Pair<Float, EntityAction>> {
    private ArrayDeque<Float> startTimes;
    private ArrayDeque<EntityAction> actions;

    private float lastActionEnd;
    private EntityAction lastAction;
    private float lastActionStart;

    /**
     * an action queue with an initial idle action on the given position
     * @param position initial position
     */
    public ActionQueue(Game game, Vector2ic position) {
        this(new ActionIdle(game, position), 0f);
    }

    /**
     * @param initialAction   a non-null action, like {@link ActionIdle}
     * @param actionStartTime the time at which this action is started.
     */
    public ActionQueue(EntityAction initialAction, float actionStartTime) {
        if (initialAction == null) throw new NullPointerException("initial action was null");
        setLast(initialAction, actionStartTime);

        startTimes = new ArrayDeque<>();
        actions = new ArrayDeque<>();
    }

    @Override
    public boolean offer(Pair<Float, EntityAction> pair) {
        return offer(pair.left, pair.right);
    }

    /**
     * Inserts the specified element into this queue. This method is generally preferable to {@link #add}, which can
     * fail to insert an element only by throwing an exception.
     * @param action    the element to add
     * @param startTime the start time of the action
     * @return {@code true} if the element was added to this queue, {@code false} if the given action does not follow
     * the previous last action.
     */
    public boolean offer(float startTime, EntityAction action) {
        Vector3f position = lastAction.getPositionAt(startTime);
        if (!position.equals(action.getStartPosition())) {
            return false;
        }

        // shift last actions into the queue
        startTimes.offer(lastActionStart);
        actions.offer(lastAction);

        setLast(action, startTime);
        return true;
    }

    /**
     * adds the given action to the queue, executing not earlier, but possibly later than the given time. The action
     * will always be placed last in execution. If there are any action in this queue executing any later than the given
     * start time, then this action will execute only after the last of those actions are executed.
     * @param startTime the minimal start time of this action.
     * @param action    the action to add to the queue
     * @return true
     */
    public boolean add(float startTime, EntityAction action) {
        boolean success = offer(startTime, action);
        if (!success) throw new IllegalArgumentException("Action " + action + " does not follow " + lastAction);
        return true;
    }

    @Override
    public Pair<Float, EntityAction> poll() {
        return new Pair<>(startTimes.poll(), actions.poll());
    }

    @Override
    public Pair<Float, EntityAction> peek() {
        return new Pair<>(startTimes.peek(), actions.peek());
    }

    public boolean removeFirst() {
        if (actions.isEmpty()) return false;
        startTimes.poll();
        actions.poll();
        return true;
    }

    @Override
    public int size() {
        return actions.size();
    }

    @Override
    public boolean isEmpty() {
        return actions.isEmpty();
    }

    @Override
    public Iterator<Pair<Float, EntityAction>> iterator() {
        return new Iterator<>() {
            Iterator<Float> times = startTimes.iterator();
            Iterator<EntityAction> things = actions.iterator();
            boolean hasSeenLast = false;

            @Override
            public boolean hasNext() {
                return times.hasNext() && !hasSeenLast;
            }

            @Override
            public Pair<Float, EntityAction> next() {
                if (!times.hasNext()) {
                    assert !hasSeenLast;
                    hasSeenLast = true;
                    return new Pair<>(lastActionStart, lastAction);
                }

                return new Pair<>(times.next(), things.next());
            }

            @Override
            public void remove() {
                if (hasSeenLast) {
                    setLast(actions.peekLast(), startTimes.peekLast());
                }
                times.remove();
                things.remove();
            }
        };
    }

    /** replace the lastAction, lastActionStart and lastActionEnd field */
    private void setLast(EntityAction action, float startTime) {
        lastAction = action;
        lastActionStart = startTime;
        lastActionEnd = startTime + action.duration();
    }

    /**
     * returns the position according to the action executing at the given time
     * @param currentTime the moment to query
     * @return the position described by this action queue.
     */
    public Vector3f getPositionAt(float currentTime) {
        Pair<EntityAction, Float> pair = getActionAt(currentTime);
        return pair.left.getPositionAt(pair.right);
    }

    /**
     * remove all actions from the head of this queue, which happen before the given time.
     * @param time the time until where to remove actions, exclusive.
     */
    public void removeUntil(float time) {
        if (time < firstActionStart() || size() == 0) return;

        if (time > lastActionEnd()) {
            clear();
            return;
        }

        // a currently executing action...
        Iterator<Float> times = startTimes.iterator();
        Iterator<EntityAction> things = actions.iterator();

        while (times.hasNext() && times.next() < time) {
            times.remove();
            things.next();
            things.remove();
        }
    }


    /**
     * sets the action to execute at the given start time, removing any action remaining in the queue. The given action
     * may be delayed to let the executing action finish.
     * @param action    the action to do
     * @param startTime the moment of interrupt
     */
    public void insert(EntityAction action, float startTime) {
        if (startTime > lastActionEnd) {
            add(startTime, action);
            return;
        }

        if (startTime < firstActionStart()) {
            clear();
            setLast(action, startTime);
            return;
        }

        // a currently executing action...
        Iterator<Float> times = startTimes.descendingIterator();
        Iterator<EntityAction> things = actions.descendingIterator();

        while (times.hasNext() && times.next() > startTime) {
            times.remove();
            things.remove();
        }

        EntityAction newLast = things.next();
        Vector3f position = newLast.getPositionAt(startTime);
        if (!position.equals(action.getStartPosition())) {
            throw new IllegalArgumentException("Action " + action + " does not follow " + newLast);
        }

        if (lastActionStart < startTime) {
            startTimes.offer(lastActionStart);
            actions.offer(lastAction);
        }

        // shift last actions into the queue
        setLast(action, startTime);
    }

    /**
     * finds the action that is executing at the given time, and calculates how far this action is executed.
     * <p>
     * Note that the eventual execution of this action on this time is not certain, as calls to {@link
     * #insert(EntityAction, float)} and {@link #remove()} may change this.
     * @param gameTime a moment in time
     * @return a pair with on left the action that should be taking place under normal circumstances, and on right the
     * time passed since the start of this action. If no action has started, return the first action, with right == 0.
     * If all actions are finished, return the last action with right == action.duration().
     */
    public Pair<EntityAction, Float> getActionAt(float gameTime) {
        if (gameTime < firstActionStart()) {
            Vector3fc position = firstAction().getStartPosition();
            return new Pair<>(new ActionIdle(position), 0f);

        } else if (gameTime > lastActionEnd) {
            Vector3fc position = lastAction.getEndPosition();
            float duration = lastActionEnd - gameTime;
            return new Pair<>(new ActionIdle(position), duration);

        } else if (actions.isEmpty()) {
            if (lastActionEnd < gameTime) {
                float waitTime = gameTime - lastActionEnd;
                return new Pair<>(new ActionIdle(lastAction.getEndPosition(), Float.POSITIVE_INFINITY), waitTime);
            }

            return new Pair<>(lastAction, gameTime - lastActionStart);
        }

        return getActionAtUnsafe(gameTime);
    }

    /**
     * @return the result of {@link #getActionAt(float)}, knowing that {@code firstActionStart < gameTime <
     * lastActionEnd}, and there is at least one action in {@code actions}
     */
    private Pair<EntityAction, Float> getActionAtUnsafe(float gameTime) {
        // a currently executing action...
        Iterator<Float> times = startTimes.iterator();
        Iterator<EntityAction> things = actions.iterator();

        EntityAction action;
        float actionStart;
        float nextActionStart = times.next(); // !actions.isEmpty()

        do {
            action = things.next();
            actionStart = nextActionStart;

            if (times.hasNext()) {
                nextActionStart = times.next();

            } else {
                nextActionStart = lastActionStart;

                if (nextActionStart < gameTime) { // basically another iteration
                    action = lastAction;
                    actionStart = nextActionStart;
                    nextActionStart = Float.POSITIVE_INFINITY;
                }

                break; // redundant
            }
        } while (nextActionStart < gameTime);

        float actionEnd = actionStart + action.duration();
        if (actionEnd < gameTime) {
            float waitDuration = nextActionStart - actionEnd;
            float waitTime = gameTime - actionEnd;
            return new Pair<>(new ActionIdle(action.getEndPosition(), waitDuration), waitTime);
        }

        return new Pair<>(action, gameTime - actionStart);
    }

    /**
     * adds a fixed idling after executing all actions in the queue
     * @param duration the exact duration of the idling. Must be positive
     */
    public void addWait(float duration) {
        assert duration >= 0;
        addLast(new ActionIdle(lastAction.getEndPosition(), duration));
    }

    /** adds the given action to the end of the queue */
    private void addLast(EntityAction action) {
        offer(lastActionEnd, action);
    }

    /** the action executed the earliest */
    public EntityAction firstAction() {
        EntityAction action = actions.peek();
        return action == null ? lastAction : action;
    }

    /** the start time of the earliest action */
    public float firstActionStart() {
        Float time = startTimes.peek();
        return time == null ? lastActionStart : time;
    }

    /** the action executed the latest */
    public EntityAction lastAction() {
        return lastAction;
    }

    /** the end time of the latest action */
    public float lastActionEnd() {
        return lastActionEnd;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("ActionQueue: ");

        Iterator<Float> times = startTimes.iterator();
        Iterator<EntityAction> things = actions.iterator();

        while (times.hasNext()) {
            str.append("[");
            str.append(times.next());
            str.append(" : ");
            str.append(things.next());
            str.append("], ");
        }

        str.delete(str.length() - 2, str.length());

        return str.toString();
    }
}
