package NG.GameState;

import NG.ActionHandling.MouseToolCallbacks;
import NG.Camera.Camera;
import NG.Camera.TycoonFixedCamera;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.Game;
import NG.Engine.GameTimer;
import NG.Engine.Version;
import NG.GameEvent.Event;
import NG.GameMap.GameMap;
import NG.GameMap.TileMap;
import NG.Rendering.GLFWWindow;
import NG.Rendering.RenderLoop;
import NG.ScreenOverlay.Frames.GUIManager;
import NG.ScreenOverlay.Frames.SFrameManager;
import NG.Settings.Settings;
import NG.Storable;
import NG.Tools.Logger;
import org.joml.Vector3f;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author Geert van Ieperen created on 6-2-2019.
 */
class DecoyGame implements Game {
    private static final Version VERSION = new Version(0, 2);
    private final String MAIN_THREAD;

    private final Settings settings;
    private final GLFWWindow window;
    private final MouseToolCallbacks inputHandler;
    private final GUIManager frameManager;
    private GameLights gameLights;
    private GameTimer timer;
    private Camera camera;
    private GameMap gameMap;
    private GameState gameState;
    private RenderLoop renderloop;

    public DecoyGame(String title, RenderLoop renderloop, Settings settings) {
        Logger.INFO.print("Starting up a partial game engine...");
        MAIN_THREAD = Thread.currentThread().getName();

        this.renderloop = renderloop;
        this.settings = settings;
        this.timer = new GameTimer(settings.RENDER_DELAY);

        this.window = new GLFWWindow(title, settings, true);
        this.inputHandler = new MouseToolCallbacks();
        this.frameManager = new SFrameManager();
        this.gameLights = new SingleShadowMapLights();
        this.gameMap = new TileMap(settings.CHUNK_SIZE);
        this.camera = new TycoonFixedCamera(new Vector3f(), 0, 10);
        this.gameState = new StaticState();
    }

    public void init() throws Exception {
        window.init(this);
        inputHandler.init(this);
        frameManager.init(this);
        gameLights.init(this);
        gameMap.init(this);
        camera.init(this);
        gameState.init(this);

        gameLights.addDirectionalLight(new Vector3f(1, -1.5f, 2), Color4f.WHITE, 0.5f);
    }

    @Override
    public GameTimer timer() {
        return timer;
    }

    @Override
    public Camera camera() {
        return camera;
    }

    @Override
    public GameState entities() {
        return gameState;
    }

    @Override
    public GameMap map() {
        return gameMap;
    }

    @Override
    public Settings settings() {
        return settings;
    }

    @Override
    public GLFWWindow window() {
        return window;
    }

    @Override
    public MouseToolCallbacks inputHandling() {
        return inputHandler;
    }

    @Override
    public GUIManager gui() {
        return frameManager;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

    @Override
    public GameLights lights() {
        return gameLights;
    }

    @Override
    public void addEvent(Event e) {
        new Thread(() -> { // works
            try {
                Thread.sleep((long) (e.getTime() * 1000));
            } catch (InterruptedException ex) {
                Logger.ERROR.print(ex);
            }

            e.run();
        }, "Wait to do action " + e).start();
    }

    @Override
    public void executeOnRenderThread(Runnable action) {
        if (Thread.currentThread().getName().equals(MAIN_THREAD)) {
            action.run();

        } else {
            renderloop.defer(action);
        }
    }

    @Override
    public void writeStateToFile(DataOutput out) throws IOException {
        Storable.writeToFile(out, gameMap);
        Storable.writeToFile(out, gameState);
        Storable.writeToFile(out, gameLights);
    }

    @Override
    public void readStateFromFile(DataInput in) throws Exception {
        GameMap newMap = Storable.readFromFile(in, GameMap.class);
        GameState newState = Storable.readFromFile(in, GameState.class);
        GameLights newLights = Storable.readFromFile(in, GameLights.class);

        newState.init(this);
        newMap.init(this);
        newLights.init(this);

        // clean up all the replaced stuff
        this.gameState.cleanup();
        this.gameMap.cleanup();
        this.gameLights.cleanup();

        // set new state
        this.gameState = newState;
        this.gameMap = newMap;
        this.gameLights = newLights;
    }

}
