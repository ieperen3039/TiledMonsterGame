package NG.GameEvent;

import NG.Engine.AbstractGameLoop;
import NG.Engine.GameAspect;
import NG.Storable;

/**
 * @author Geert van Ieperen created on 16-2-2019.
 */
public abstract class EventLoop extends AbstractGameLoop implements GameAspect, Storable {
    /**
     * creates a new, paused gameloop
     * @param name      the name as displayed in {@link #toString()}
     * @param targetTps the target number of executions of {@link #update(float)} per second
     */
    public EventLoop(String name, int targetTps) {
        super(name, targetTps);
    }

    public abstract void addEvent(Event e);
}
