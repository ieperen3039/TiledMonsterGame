package NG.Actions;

import NG.Core.Game;
import NG.Core.GameObject;
import NG.DataStructures.Generic.Pair;
import NG.Tools.AutoLock;
import NG.Tools.Toolbox;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static NG.Actions.EntityAction.ACCEPTABLE_DIFFERENCE_SQ;

/**
 * A queue of actions with additional robustness checking and a {@link #getPositionAt(float)} method. If the actions
 * added to this queue have undefined start time, the start time is set to be the closest to the other actions in this
 * queue. This collection is not synchronized.
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class ActionQueue extends AbstractQueue<Pair<EntityAction, Float>> implements GameObject {
    private ArrayDeque<Float> startTimes;
    private ArrayDeque<EntityAction> actions;

    private final AutoLock lockQueueRead;
    private final AutoLock lockQueueEdit;

    private float lastActionStart;
    private float lastActionEnd;

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
        ReadWriteLock rwl = new ReentrantReadWriteLock(true);
        lockQueueRead = new AutoLock.Wrapper(rwl.readLock());
        lockQueueEdit = new AutoLock.Wrapper(rwl.writeLock());

        startTimes = new ArrayDeque<>();
        actions = new ArrayDeque<>();
        setLast(initialAction, actionStartTime);
    }

    /** @see #offer(EntityAction, float) */
    @Override
    public boolean offer(Pair<EntityAction, Float> pair) {
        return offer(pair.left, pair.right);
    }

    /**
     * Appends the specified element to this queue. This method is generally preferable to {@link #add}, which can fail
     * to insert an element only by throwing an exception.
     * @param action    the element to add
     * @param startTime the start time of the action
     * @return {@code true} if the element was added to this queue, {@code false} if the given action does not follow
     * the previous last action.
     */
    public boolean offer(EntityAction action, float startTime) {
        if (startTime < lastActionStart) return false;

        Vector3f position = actions.getLast().getPositionAt(startTime - lastActionStart);
        if (position.distanceSquared(action.getStartPosition()) > ACCEPTABLE_DIFFERENCE_SQ) {
            return false;
        }

        lockQueueEdit.lock();
        setLast(action, startTime);
        lockQueueEdit.unlock();

        return true;
    }

    @Override
    public Pair<EntityAction, Float> poll() {
        if (actions.size() == 1) return peek();

        lockQueueEdit.lock();
        Pair<EntityAction, Float> pair = new Pair<>(actions.poll(), startTimes.poll());
        lockQueueEdit.unlock();
        return pair;
    }

    @Override
    public Pair<EntityAction, Float> peek() {
        return new Pair<>(actions.peek(), startTimes.peek());
    }

    public boolean removeFirst() {
        if (isEmpty()) return false;
        lockQueueEdit.lock();
        startTimes.removeFirst();
        actions.removeFirst();
        lockQueueEdit.unlock();
        return true;
    }

    @Override
    public int size() {
        return actions.size();
    }

    @Override
    public boolean isEmpty() {
        return actions.size() == 1;
    }

    /** replace the lastAction, lastActionStart and lastActionEnd field */
    private void setLast(EntityAction action, float startTime) {
        actions.add(action);
        startTimes.add(startTime);
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
     * remove all actions from the head of this queue, that happen before the given time.
     * @param time the time until where to remove actions, exclusive.
     */
    public void removeUntil(float time) {
        if (time < startTimes.getFirst() || isEmpty()) return;

        if (time > lastActionStart) {
            time = lastActionStart;
        }

        // a currently executing action...
        // count how many actions start earlier, then remove all but one of those
        lockQueueEdit.lock();
        int i = 0;
        for (Float startTime : startTimes) {
            if (startTime < time) i++;
        }
        while (i > 1) {
            startTimes.removeFirst();
            actions.removeFirst();
            i--;
        }

        lockQueueEdit.unlock();
    }


    /**
     * sets the action to execute at the given start time, removing any action remaining in the queue. The given action
     * may be delayed to let the executing action finish.
     * @param action    the action to do
     * @param startTime the moment of interrupt
     */
    public void insert(EntityAction action, float startTime) {
        assert action != null;

        if (startTime >= lastActionEnd) {
            offer(action, startTime);
            return;
        }

        if (startTime <= firstActionStart()) {
            lockQueueEdit.lock();
            clear();
            setLast(action, startTime);
            lockQueueEdit.unlock();
            return;
        }

        // a currently executing action...
        lockQueueEdit.lock();
        EntityAction previous;
        float prevStart;

        if (actions.size() == 1) {
            previous = actions.getLast();
            prevStart = lastActionStart;

        } else {
            Iterator<Float> times = startTimes.descendingIterator();
            Iterator<EntityAction> things = actions.descendingIterator();

            prevStart = times.next();
            while (prevStart > startTime) {
                times.remove();
                things.next();
                things.remove();
                prevStart = times.next();
            }
            previous = things.next();
        }

        Vector3f position = previous.getPositionAt(startTime - prevStart);
        if (position.distanceSquared(action.getStartPosition()) > ACCEPTABLE_DIFFERENCE_SQ) {
            lockQueueEdit.unlock();
            throw new BrokenMovementException(previous, action, startTime - prevStart);
        }

        setLast(action, startTime);

        lockQueueEdit.unlock();
    }

    /**
     * finds the action that is executing at the given time, and calculates how far this action is executed.
     * <p>
     * Note that the eventual execution of this action on this time is not certain, as calls to {@link
     * #insert(EntityAction, float)} and {@link #remove()} may change this.
     * @param gameTime a moment in time after the start of the first action
     * @return a pair with on left the action that should be taking place under normal circumstances, and on right the
     * time passed since the start of this action.
     */
    public Pair<EntityAction, Float> getActionAt(float gameTime) {
        assert gameTime >= startTimes.getFirst();
        assert !actions.isEmpty();

        if (gameTime >= lastActionStart) {
            return new Pair<>(actions.getLast(), gameTime - lastActionStart);
        }

        EntityAction action;
        float actionStart;

        try (AutoLock.Section section = lockQueueRead.open()) {
            // a currently executing action...
            Iterator<Float> times = startTimes.iterator();
            Iterator<EntityAction> things = actions.iterator();

            float nextActionStart = times.next();

            do {
                action = things.next();
                actionStart = nextActionStart;
                nextActionStart = times.next();
            } while (nextActionStart < gameTime);
        }

        return new Pair<>(action, gameTime - actionStart);
    }

    /**
     * adds a fixed idling after executing all actions in the queue
     * @param duration the exact duration of the idling. Must be positive
     */
    public void addWait(float duration) {
        assert duration >= 0;
        Vector3fc position = actions.getLast().getEndPosition();
        addLast(new ActionIdle(position, duration));
    }

    /** adds the given action to the end of the queue */
    private void addLast(EntityAction action) {
        offer(action, lastActionEnd);
    }

    /** the action executed the earliest */
    public EntityAction firstAction() {
        return actions.peek();
    }

    /** the start time of the earliest action */
    public float firstActionStart() {
        return startTimes.getFirst();
    }

    public Iterable<EntityAction> actionsBetween(float startTime, float endTime) {
        return () -> {
            int exitPoint = -1;
            // if it turns out that this syncing is not enough, consider using toArray to make local copies to iterate on
            try (AutoLock.Section section = lockQueueRead.open()) { // locks while building an iterator
                Iterator<Float> times = startTimes.iterator();
                Iterator<EntityAction> things = actions.iterator();

                times.next(); // time of action 0
                float time;

                if (times.hasNext()) {
                    time = times.next(); // time of action 1

                    // i = 0
                    while (time < startTime) { // (action i is in progress) == ((time of action i+1) > now)
                        if (!times.hasNext()) {
                            // action i+1 is the last, but starts before startTime
                            things.next(); // action i
                            EntityAction nextAction = things.next(); // action i+1

                            // if it is still in progress at start, return it.
                            if (time + nextAction.duration() > startTime) {
                                return Toolbox.singletonIterator(nextAction);

                            } else { // otherwise return nothing
                                return Collections.emptyIterator();
                            }
                        }

                        time = times.next(); // time of action i+2
                        things.next(); // skip action i
                        // i++
                    }

                } else {
                    // only one action
                    return things; // guaranteed to also have only one element
                }

                if (time > endTime) {
                    return Toolbox.singletonIterator(things.next());
                }

                assert time >= startTime && time <= endTime;

                return new Iterator<>() {
                    private boolean hasNext = true;
                    private boolean hasTwo = true;

                    @Override
                    public boolean hasNext() {
                        return hasNext && things.hasNext();
                    }

                    @Override
                    public EntityAction next() {
                        hasNext = hasTwo;
                        hasTwo = (times.hasNext() && times.next() < endTime);
                        return things.next();
                    }
                };
            }
        };
    }

    @Override
    public Iterator<Pair<EntityAction, Float>> iterator() {
        return new Iterator<>() {
            Iterator<Float> times = startTimes.iterator();
            Iterator<EntityAction> things = actions.iterator();

            @Override
            public boolean hasNext() {
                return things.hasNext();
            }

            @Override
            public Pair<EntityAction, Float> next() {
                return new Pair<>(things.next(), times.next());
            }

            @Override
            public void remove() {
                times.remove();
                things.remove();
            }
        };
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

    @Override
    public void restore(Game game) {
        for (EntityAction action : actions) {
            action.restore(game);
        }
    }
}
