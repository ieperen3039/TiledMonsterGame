package NG;

import NG.ActionHandling.ClickShader;
import NG.ActionHandling.MouseToolCallbacks;
import NG.ActionHandling.MouseTools.MouseTool;
import NG.Camera.Camera;
import NG.Camera.TycoonFixedCamera;
import NG.DataStructures.Storable;
import NG.Engine.Game;
import NG.Engine.GameTimer;
import NG.Engine.Version;
import NG.Entities.Entity;
import NG.GameMap.GameMap;
import NG.GameMap.TileMap;
import NG.GameState.GameLights;
import NG.GameState.GameState;
import NG.GameState.SingleShadowMapLights;
import NG.Rendering.GLFWWindow;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.RenderLoop;
import NG.Rendering.Shapes.Primitives.Collision;
import NG.ScreenOverlay.Frames.GUIManager;
import NG.ScreenOverlay.Frames.SFrameManager;
import NG.Settings.Settings;
import NG.Tools.Logger;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author Geert van Ieperen created on 6-2-2019.
 */
class DecoyGame implements Game {
    private static final Version VERSION = new Version(0, 1);
    private final Settings settings;
    private final GLFWWindow window;
    private final MouseToolCallbacks inputHandler;
    private final GUIManager frameManager;
    private final GameLights gameLights;
    private GameTimer timer;
    private Camera camera;
    private GameMap theMap;
    private GameState gameState;
    private RenderLoop renderloop;

    public DecoyGame(String title, RenderLoop renderloop, Settings settings) {
        this.renderloop = renderloop;
        Logger.INFO.print("Starting up a partial game engine...");
        this.settings = settings;
        this.timer = new GameTimer(settings.RENDER_DELAY);

        this.window = new GLFWWindow(title, settings, true);
        this.inputHandler = new MouseToolCallbacks();
        this.frameManager = new SFrameManager();
        this.gameLights = new SingleShadowMapLights();
        this.theMap = new TileMap(settings.CHUNK_SIZE);
        this.camera = new TycoonFixedCamera(new Vector3f(), 0, 10);
        this.gameState = new StaticState();
    }

    public void init() throws Exception {
        window.init(this);
        inputHandler.init(this);
        frameManager.init(this);
        gameLights.init(this);
        theMap.init(this);
        camera.init(this);
        gameState.init(this);
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
    public GameState state() {
        return gameState;
    }

    @Override
    public GameMap map() {
        return theMap;
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
    public void executeOnRenderThread(Runnable action) {
        renderloop.defer(action);
    }

    private class StaticState implements GameState {
        private final List<Entity> entities;

        public StaticState() {
            entities = Collections.synchronizedList(new ArrayList<>());
        }

        public StaticState(DataInput in) throws IOException, ClassNotFoundException {
            int size = in.readInt();
            ArrayList<Entity> list = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                Entity entity = Storable.readFromFile(in, Entity.class);
                list.add(entity);
            }

            entities = Collections.synchronizedList(list);
        }

        @Override
        public void addEntity(Entity entity) {
            entities.add(entity);
        }

        @Override
        public void draw(SGL gl) {
            entities.forEach(e -> e.draw(gl));
        }

        @Override
        public Collision getEntityCollision(Vector3fc from, Vector3fc to) {
            return null;
        }

        @Override
        public boolean checkMouseClick(MouseTool tool, int xSc, int ySc) {
            FutureTask<Entity> identifier = new FutureTask<>(() -> ClickShader.getEntity(DecoyGame.this, xSc, ySc));
            executeOnRenderThread(identifier);

            try {
                Entity entity = identifier.get();
                if (entity == null) return false;

                tool.apply(entity, xSc, ySc);
                return true;

            } catch (InterruptedException | ExecutionException ex) {
                Logger.ERROR.print(ex);
                return false;
            }
        }

        @Override
        public void writeToFile(DataOutput out) throws IOException {
            // properties of the list
            out.writeInt(entities.size());
            for (Entity e : entities) {
                if (e instanceof Storable) {
                    Storable.writeToFile((Storable) e, out);
                }
            }
        }

        @Override
        public void init(Game game) throws Exception {
        }

        @Override
        public void cleanup() {
        }
    }
}
