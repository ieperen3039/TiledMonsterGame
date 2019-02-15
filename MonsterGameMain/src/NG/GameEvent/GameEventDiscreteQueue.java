package NG.GameEvent;

import NG.Engine.AbstractGameLoop;
import NG.Engine.Game;
import NG.Engine.GameAspect;
import NG.Engine.GameTimer;

import java.util.PriorityQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An object similar to {@link GameEventQueue}, but now the queue is evaluated once every game tick. This allows for
 * external updates to the queue, like with user interaction.
 * @author Geert van Ieperen created on 14-2-2019.
 */
public class GameEventDiscreteQueue extends AbstractGameLoop implements GameAspect {
    private final PriorityQueue<Event> eventQueue; // requires explicit synchronisation
    private final Lock lockEventQueue;
    private Game game;

    private Event next = null;

    /**
     * creates a new, paused event loop
     * @param targetTps the target number of executions of {@link #update(float)} per second
     */
    public GameEventDiscreteQueue(int targetTps) {
        super("Discrete Game Event Loop", targetTps);
        eventQueue = new PriorityQueue<>();
        lockEventQueue = new ReentrantLock();
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
    }

    @Override
    protected void update(float deltaTime) throws Exception {
        GameTimer timer = game.timer();
        timer.updateGameTime();
        float gametime = game.timer().getGametime();

        if (next == null) {
            // update next
            lockEventQueue.lock();
            next = eventQueue.poll();
            lockEventQueue.unlock();
        }

        while (next != null && next.getTime() <= gametime) {
            next.run();

            // update next
            lockEventQueue.lock();
            next = eventQueue.poll();
            lockEventQueue.unlock();
        }
    }

    public void addEvent(Event e) {
        lockEventQueue.lock();
        try {
            eventQueue.add(e);

        } finally {
            lockEventQueue.unlock();
        }
    }

    @Override
    public void cleanup() {
        lockEventQueue.lock();
        eventQueue.clear();
        lockEventQueue.unlock();
    }
}
