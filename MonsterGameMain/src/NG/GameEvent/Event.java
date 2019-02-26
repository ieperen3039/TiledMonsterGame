package NG.GameEvent;

import NG.Engine.Game;
import NG.MonsterSoul.Stimulus;
import NG.MonsterSoul.Type;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.Vector3fc;

import static NG.MonsterSoul.BaseStimulus.UNKNOWN;

/**
 * a generic event that happens on a predetermined time. Triggering of the event should result in a stimulus being
 * broadcasted.
 * @see Anonymous Event.Anonymous
 */
public abstract class Event implements Comparable<Event>, Runnable, Stimulus {
    private final float eventTime;

    public Event(float eventTime) {
        this.eventTime = eventTime;
    }

    public float getTime() {
        return eventTime;
    }

    @Override
    public int compareTo(Event other) {
        return Float.compare(eventTime, other.eventTime);
    }

    /**
     * a class that triggers a runnable when triggered. May be cancelled, such that it no longer executes the given
     * runnable. As stimulus, this event won't be noticed.
     */
    public static class Anonymous extends Event {
        private Runnable action;

        public Anonymous(float eventTime, Runnable action) {
            super(eventTime);
            assert action != null;
            this.action = action;
        }

        @Override
        public void run() {
            if (action != null) action.run();
        }

        public void cancel() {
            action = null;
        }

        @Override
        public Type getType() {
            return UNKNOWN;
        }
    }

    /**
     * an event that does nothing but triggering a debug statement. This may schedule itself repetitively
     */
    public static class DebugEvent extends Event {
        private final float recurrence;
        private Game game;
        private final int id;
        private static int nextID = 0;

        /**
         * an event of a debug statement
         * @param game       the game instance
         * @param eventTime  the time to schedule the event
         * @param recurrence the recurrence period, or -1 if no recurrence is wanted. The actual recurrence period is up
         *                   to 1 above the given value.
         */
        public DebugEvent(Game game, float eventTime, float recurrence) {
            super(eventTime);
            Logger.DEBUG.printFrom(2, "Scheduled debug timer at " + eventTime);
            this.game = game;
            this.recurrence = recurrence;
            this.id = nextID++;
        }

        @Override
        public void run() {
            Logger.DEBUG.print(this + " triggered at " + game.timer().getGametime());
            if (recurrence < 0) return;

            float next = Toolbox.randomBetween(getTime() + recurrence, getTime() + recurrence + 1);
            DebugEvent nextEvent = new DebugEvent(game, next, recurrence);
            game.addEvent(nextEvent);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " " + id;
        }

        @Override
        public Type getType() {
            return UNKNOWN;
        }

        @Override
        public float getMagnitude(Vector3fc position) {
            return 0;
        }
    }
}
