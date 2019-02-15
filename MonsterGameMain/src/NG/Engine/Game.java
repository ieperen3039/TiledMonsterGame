package NG.Engine;

import NG.ActionHandling.KeyMouseCallbacks;
import NG.Camera.Camera;
import NG.GameEvent.Event;
import NG.GameMap.GameMap;
import NG.GameState.GameLights;
import NG.GameState.GameState;
import NG.Rendering.GLFWWindow;
import NG.ScreenOverlay.Frames.GUIManager;
import NG.Settings.Settings;
import NG.Tools.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * A collection of references to any major element of the game.
 * @author Geert van Ieperen. Created on 16-9-2018.
 */
public interface Game {

    GameTimer timer();

    Camera camera();

    GameState state();

    GameMap map();

    Settings settings();

    GLFWWindow window();

    KeyMouseCallbacks inputHandling();

    GUIManager gui();

    Version getVersion();

    GameLights lights();

    /**
     * schedules an event on the game loop
     * @param e the event to execute
     */
    void addEvent(Event e);

    /**
     * Schedules the specified action to be executed in the OpenGL context. The action is guaranteed to be executed
     * before two frames have been rendered.
     * @param action the action to execute
     */
    void executeOnRenderThread(Runnable action);

    /**
     * Schedules the specified action to be executed in the OpenGL context. The action is guaranteed to be executed
     * before two frames have been rendered.
     * @param action the action to execute
     * @param <V>    the return type of action
     * @return a reference to obtain the result of the execution, or null if it threw an exception
     */
    default <V> Future<V> computeOnRenderThread(Callable<V> action) {
        FutureTask<V> task = new FutureTask<>(() -> {
            try {
                return action.call();

            } catch (Exception ex) {
                Logger.ERROR.print(ex);
                return null;
            }
        });

        executeOnRenderThread(task);
        return task;
    }

    ;

    /**
     * writes all relevant parts that represent the state of this Game object to the output stream. This can be reverted
     * using {@link #readStateFromFile(DataInput)}.
     * @param out an output stream
     */
    void writeStateToFile(DataOutput out) throws IOException;

    /**
     * reads and restores a state previously written by {@link #writeStateToFile(DataOutput)}. After this method
     * returns, the elements that represent the state of this object are set to the streamed state.
     * @param in an input stream, synchronized with the begin of {@link #writeStateToFile(DataOutput)}
     */
    void readStateFromFile(DataInput in) throws IOException;
}
