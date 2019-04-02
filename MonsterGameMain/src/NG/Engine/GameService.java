package NG.Engine;

import NG.Camera.Camera;
import NG.DataStructures.Generic.PairList;
import NG.GameEvent.Event;
import NG.GameEvent.EventLoop;
import NG.GameMap.ClaimRegistry;
import NG.GameMap.GameMap;
import NG.GameState.GameLights;
import NG.GameState.GameState;
import NG.InputHandling.KeyMouseCallbacks;
import NG.Rendering.GLFWWindow;
import NG.Rendering.RenderLoop;
import NG.ScreenOverlay.Frames.GUIManager;
import NG.Settings.Settings;
import NG.Storable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A Service-Oriented-Architecture for games, based on classes
 * @author Geert van Ieperen created on 2-4-2019.
 */
public class GameService implements Game {
    private final PairList<Object, Class> elements;
    private final String mainThreadName;
    private Version version;

    /**
     * @param version the version of the game engine
     * @param initial an array of elements that are initially in this game
     */
    public GameService(Version version, Object... initial) throws Exception {
        this.version = version;
        this.elements = new PairList<>(initial.length);

        for (Object e : initial) {
            if (e instanceof GameAspect) {
                GameAspect aspect = (GameAspect) e;
                aspect.init(this);
            }
            elements.add(e, e.getClass());
        }
        mainThreadName = "main";
    }

    /**
     * returns an element of the given class. If there are multiple elements, the first element is returned.
     * @param target the class that is sought
     * @param <T>    the type of this class
     * @return an element of the given target class, or null if no such element is found
     */
    public <T> T get(Class<T> target) {
        for (int i = 0; i < elements.size(); i++) {
            if (target.isAssignableFrom(elements.right(i))) {
                //noinspection unchecked
                return (T) elements.left(i);
            }
        }

        return null;
    }

    /**
     * returns all elements of the given class.
     * @param target the class that is sought
     * @param <T>    the type of this class
     * @return a list of all elements of the given target class, or an empty list if no such element is found.
     */
    public <T> List<T> getAll(Class<T> target) {
        List<T> results = new ArrayList<>();

        for (int i = 0; i < elements.size(); i++) {
            if (target.isAssignableFrom(elements.right(i))) {
                //noinspection unchecked
                results.add((T) elements.left(i));
            }
        }

        return results;
    }

    /**
     * adds an element to this server. Added elements can be retrieved using {@link #get(Class)}
     * @param newElement the new element
     */
    public void add(Object newElement) {
        elements.add(newElement, newElement.getClass());
    }

    /**
     * @param original the object to remove
     * @return true iff the element was found and removed
     */
    public boolean remove(Object original) {
        for (int i = 0; i < elements.size(); i++) {
            if (elements.left(i) == original) { // pointer equality
                elements.remove(i);
                return true;
            }
        }

        return false;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public GameTimer timer() {
        return get(GameTimer.class);
    }

    @Override
    public Camera camera() {
        return get(Camera.class);
    }

    @Override
    public GameState entities() {
        return get(GameState.class);
    }

    @Override
    public GameMap map() {
        return get(GameMap.class);
    }

    @Override
    public Settings settings() {
        return get(Settings.class);
    }

    @Override
    public GLFWWindow window() {
        return get(GLFWWindow.class);
    }

    @Override
    public KeyMouseCallbacks inputHandling() {
        return get(KeyMouseCallbacks.class);
    }

    @Override
    public GUIManager gui() {
        return get(GUIManager.class);
    }

    @Override
    public GameLights lights() {
        return get(GameLights.class);
    }

    @Override
    public ClaimRegistry claims() {
        return get(ClaimRegistry.class);
    }

    @Override
    public void addEvent(Event e) {
        get(EventLoop.class).addEvent(e);
    }

    @Override
    public void executeOnRenderThread(Runnable action) {
        boolean thisIsMainThread = Thread.currentThread().getName().equals(mainThreadName);

        if (thisIsMainThread) {
            action.run();

        } else {
            get(RenderLoop.class).defer(action);
        }
    }

    @Override
    public void writeStateToFile(DataOutput out) throws IOException {
        List<Storable> targets = new ArrayList<>();

        for (int i = 0; i < elements.size(); i++) {
            Object elt = elements.left(i);
            if (elt instanceof Storable) {
                targets.add((Storable) elt);
            }
        }

        Storable.writeCollection(out, targets);
    }

    @Override
    public void readStateFromFile(DataInput in) throws Exception {
        cleanup();

        List<Storable> box = Storable.readCollection(in, Storable.class);
        box.forEach(this::add);
    }

    public void cleanup() {
        for (GameAspect aspect : getAll(GameAspect.class)) {
            aspect.cleanup();
        }

        elements.clear();
    }

    @Override
    public void loadMap(File map) throws Exception {
        FileInputStream fs = new FileInputStream(map);
        DataInput input = new DataInputStream(fs);
        GameMap newMap = Storable.read(input, GameMap.class);

        newMap.init(this);
        add(newMap);

        GameMap oldMap = get(GameMap.class);
        remove(oldMap);
        oldMap.cleanup();
    }
}
