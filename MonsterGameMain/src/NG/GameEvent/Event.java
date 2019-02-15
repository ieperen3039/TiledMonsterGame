package NG.GameEvent;

import NG.Engine.Game;
import NG.Tools.Logger;
import NG.Tools.Toolbox;

/**
 * a generic event that happens on a predetermined time.
 */
public abstract class Event implements Comparable<Event>, Runnable {
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
     * runnable.
     */
    public static class Anonymous extends Event {
        private Runnable action;

        public Anonymous(float eventTime, Runnable action) {
            super(eventTime);
            this.action = action;
        }

        @Override
        public void run() {
            if (action != null) action.run();
        }

        public void cancel() {
            action = null;
        }
    }

    public static class DebugEvent extends Event {
        private final float recurrence;
        private Game game;
        private final int id;
        private static int nextID = 0;

        /**
         * an event that does nothing but triggering a debug statement. This may schedule itself repetitively
         * @param game       the game instance
         * @param eventTime  the time to schedule the event
         * @param recurrence the recurrence period, or -1 if no recurrence is wanted. The actual recurrence period is up
         *                   to 1 above the given value.
         */
        public DebugEvent(Game game, float eventTime, float recurrence) {
            super(eventTime);
            Logger.DEBUG.print("Scheduled debug timer at " + eventTime);
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
    }
}
