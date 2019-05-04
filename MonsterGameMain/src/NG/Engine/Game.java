package NG.Engine;

import NG.GameMap.GameMap;
import NG.Storable;
import NG.Tools.Logger;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * A collection of references to any major element of the game.
 * @author Geert van Ieperen. Created on 16-9-2018.
 */
public interface Game extends Iterable<Object> {

    /**
     * returns an element of the given class. If there are multiple elements, the first element is returned.
     * @param target the class that is sought
     * @param <T>    the type of this class
     * @return an element of the given target class, or null if no such element is found
     */
    <T> T get(Class<T> target);

    /**
     * returns all elements of the given class.
     * @param target the class that is sought
     * @param <T>    the type of this class
     * @return a list of all elements of the given target class, or an empty list if no such element is found.
     */
    <T> List<T> getAll(Class<T> target);

    /**
     * returns whether this Game object has at least one instance of the given class.
     * @param target the class that is sought
     * @return true iff {@link #get(Class)} would return a class
     */
    boolean has(Class<?> target);

    /**
     * adds an element to this server. Added elements can be retrieved using {@link #get(Class)}
     * @param newElement the new element
     */
    void add(Object newElement);

    /** @see #add(Object) */
    default void addAll(Object... elements) {
        for (Object elt : elements) {
            add(elt);
        }
    }

    /**
     * @param original the object to remove
     * @return true iff the element was found and removed
     */
    boolean remove(Object original);

    /**
     * @return the version of the game engine
     */
    Version getVersion();

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

    /**
     * empty all elements, call the cleanup method of all gameAspect elements
     */
    void cleanup();

    default void loadMap(File map) throws Exception {
        FileInputStream fs = new FileInputStream(map);
        DataInputStream input = new DataInputStream(fs);
        GameMap newMap = Storable.read(input, GameMap.class);
        GameMap oldMap = get(GameMap.class);

        newMap.init(this);
        add(newMap);

        remove(oldMap);
        oldMap.cleanup();
    }

    default void init() throws Exception {
        for (GameAspect aspect : getAll(GameAspect.class)) {
            aspect.init(this);
        }
    }

    /**
     * a class that allows run-time switching between game instances
     */
    class Multiplexer implements Game {
        private final Game[] instances;
        private Game current;

        protected Multiplexer(int initial, Game... instances) {
            this.instances = instances;
            current = instances[initial];
        }

        void select(int target) {
            current = instances[target];
        }

        /**
         * returns an element of the given class. If there are multiple elements, the first element is returned.
         * @param target the class that is sought
         * @param <T>    the type of this class
         * @return an element of the given target class, or null if no such element is found
         */
        public <T> T get(Class<T> target) {
            return current.get(target);
        }

        /**
         * returns all elements of the given class.
         * @param target the class that is sought
         * @param <T>    the type of this class
         * @return a list of all elements of the given target class, or an empty list if no such element is found.
         */
        public <T> List<T> getAll(Class<T> target) {
            return current.getAll(target);
        }

        /**
         * adds an element to this server. Added elements can be retrieved using {@link #get(Class)}
         * @param newElement the new element
         */
        public void add(Object newElement) {
            current.add(newElement);
        }

        /**
         * @param original the object to remove
         * @return true iff the element was found and removed
         */
        public boolean remove(Object original) {
            return current.remove(original);
        }

        @Override
        public Version getVersion() {
            return current.getVersion();
        }

        @Override
        public void executeOnRenderThread(Runnable action) {
            current.executeOnRenderThread(action);
        }

        @Override
        public void cleanup() {
            current.cleanup();
        }

        @Override
        public void loadMap(File map) throws Exception {
            current.loadMap(map);
        }

        @Override
        public boolean has(Class<?> target) {
            return current.has(target);
        }

        @Override
        public <V> Future<V> computeOnRenderThread(Callable<V> action) {
            return current.computeOnRenderThread(action);
        }

        @Override
        public Iterator<Object> iterator() {
            return current.iterator();
        }

        public int current() {
            return Arrays.asList(instances).indexOf(current);
        }
    }
}
