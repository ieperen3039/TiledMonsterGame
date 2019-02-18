package NG.GameState;

import NG.ActionHandling.MouseToolCallbacks;
import NG.Camera.Camera;
import NG.Camera.TycoonFixedCamera;
import NG.Engine.Game;
import NG.Engine.GameTimer;
import NG.Engine.Version;
import NG.GameEvent.Event;
import NG.GameMap.ClaimRegistry;
import NG.GameMap.GameMap;
import NG.GameMap.TileMap;
import NG.Rendering.GLFWWindow;
import NG.Rendering.RenderLoop;
import NG.ScreenOverlay.Frames.GUIManager;
import NG.ScreenOverlay.Frames.SFrameManager;
import NG.Settings.Settings;
import NG.Tools.Logger;
import org.joml.Vector3f;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;

/**
 * @author Geert van Ieperen created on 6-2-2019.
 */
class TestGame implements Game {
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

    public TestGame(RenderLoop renderloop, Settings settings) {
        Logger.INFO.print("Starting up a partial game engine...");
        MAIN_THREAD = Thread.currentThread().getName();

        this.renderloop = renderloop;
        this.settings = settings;
        this.timer = new GameTimer(settings.RENDER_DELAY);

        this.window = new GLFWWindow("Testgame", settings, true);
        this.inputHandler = new MouseToolCallbacks();
        this.frameManager = new SFrameManager();
        this.gameLights = new SingleShadowMapLights();
        this.gameMap = new TileMap(settings.CHUNK_SIZE);
        this.camera = new TycoonFixedCamera(new Vector3f(), 0, 10);
        this.gameState = new StaticState();
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
    public ClaimRegistry claims() {
        return null;
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
    }

    @Override
    public void readStateFromFile(DataInput in) throws Exception {
    }

    @Override
    public void loadMap(File map) throws Exception {

    }

}
