package NG.GameEvent;

import NG.Core.GameAspect;

/**
 * Executes events in a loop. To subscribe to event callbacks, see {@link NG.InputHandling.EventCallbacks}
 * @author Geert van Ieperen created on 18-4-2019.
 */
public interface EventLoop extends Runnable, GameAspect {
    void addEvent(Event e);
}
