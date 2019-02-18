package NG.Engine;

import NG.ActionHandling.MouseToolCallbacks;
import NG.Camera.Camera;
import NG.Camera.TycoonFixedCamera;
import NG.DataStructures.Generic.Color4f;
import NG.GameEvent.Event;
import NG.GameEvent.EventLoop;
import NG.GameEvent.GameEventDiscreteQueue;
import NG.GameMap.ClaimRegistry;
import NG.GameMap.GameMap;
import NG.GameMap.TileMap;
import NG.GameState.GameLights;
import NG.GameState.GameState;
import NG.GameState.SingleShadowMapLights;
import NG.GameState.StaticState;
import NG.Mods.InitialisationMod;
import NG.Mods.Mod;
import NG.Rendering.GLFWWindow;
import NG.Rendering.RenderLoop;
import NG.ScreenOverlay.Frames.GUIManager;
import NG.ScreenOverlay.Frames.SFrameManager;
import NG.ScreenOverlay.Menu.MainMenu;
import NG.Settings.Settings;
import NG.Storable;
import NG.Tools.Directory;
import NG.Tools.Logger;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * This class initializes all gameAspects, allow for starting a game, loading mods and cleaning up afterwards. It
 * provides all aspects of the game engine through the {@link Game} interface.
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class MonsterGame implements ModLoader {
    private static final Version GAME_VERSION = new Version(0, 2);
    private final String MAIN_THREAD;

    private final RenderLoop renderer;
    private final Settings settings;
    private final GLFWWindow window;
    private final MouseToolCallbacks inputHandler;
    private final GUIManager frameManager;
    private MainMenu mainMenu;

    private SubGame pocketGame;
    private SubGame worldGame;
    private Game.Multiplexer combinedGame;
    private boolean currentIsPocket;

    private List<Mod> allMods;
    private List<Mod> activeMods = Collections.emptyList();
    private List<Mod> permanentMods;

    public MonsterGame() throws IOException {
        Logger.INFO.print("Starting up the game engine...");
        MAIN_THREAD = Thread.currentThread().getName();

        Logger.DEBUG.print("General debug information: " +
                // manual aligning will do the trick
                "\n\tSystem OS:             " + System.getProperty("os.name") +
                "\n\tJava VM:               " + System.getProperty("java.runtime.version") +
                "\n\tGame version:          " + GAME_VERSION +
                "\n\tWorking directory:     " + Directory.workDirectory().toAbsolutePath() +
                "\n\tMods directory:        " + Directory.mods.getPath()
        );

        // these are not GameAspects, and thus the init() rule does not apply.
        settings = new Settings();

        window = new GLFWWindow(Settings.GAME_NAME, settings, true);
        renderer = new RenderLoop(settings.TARGET_FPS);
        inputHandler = new MouseToolCallbacks();
        frameManager = new SFrameManager();

        Camera pocketView = new TycoonFixedCamera(new Vector3f(), 10, 10);
        EventLoop pocketGameLoop = new GameEventDiscreteQueue(settings.TARGET_TPS);
        GameState pocketGameState = new StaticState();
        GameLights pocketLights = new SingleShadowMapLights();
        GameMap pocketMap = new TileMap(settings.CHUNK_SIZE);
        pocketGame = new SubGame(pocketGameLoop, pocketGameState, pocketMap, pocketLights, pocketView);

        Camera worldView = new TycoonFixedCamera(new Vector3f(), 10, 10);
        EventLoop worldGameLoop = new GameEventDiscreteQueue(settings.TARGET_TPS);
        GameState worldGameState = new StaticState();
        GameLights worldLights = new SingleShadowMapLights();
        GameMap worldMap = new TileMap(settings.CHUNK_SIZE);
        worldGame = new SubGame(worldGameLoop, worldGameState, worldMap, worldLights, worldView);

        combinedGame = new Game.Multiplexer(0, pocketGame, worldGame);
        currentIsPocket = true;

        // load mods
        allMods = JarModReader.loadMods(Directory.mods);
    }

    /**
     * start all elements required for showing the main frame of the game.
     * @throws Exception when the initialisation fails.
     */
    public void init() throws Exception {
        Logger.DEBUG.print("Initializing...");
        // init all fields
        window.init(combinedGame);
        renderer.init(combinedGame);
        inputHandler.init(combinedGame);
        frameManager.init(combinedGame);
        pocketGame.init();
        worldGame.init();

        renderer.addHudItem(frameManager::draw);

        permanentMods = JarModReader.filterInitialisationMods(allMods, combinedGame);

        mainMenu = new MainMenu(pocketGame, worldGame, this, renderer::stopLoop);
        frameManager.addFrame(mainMenu);

        inputHandler.addKeyPressListener(k -> {
            if (k == GLFW.GLFW_KEY_SPACE) {
                combinedGame.executeOnRenderThread(() -> {
                    combinedGame.select(currentIsPocket ? 1 : 0);
                    currentIsPocket = !currentIsPocket;
                });
            }
        });

        pocketGame.thisLights.addDirectionalLight(new Vector3f(1, 1, 2), Color4f.rgb(255, 241, 224), 0.8f);
        worldGame.thisLights.addDirectionalLight(new Vector3f(2, 1.5f, 0.5f), Color4f.WHITE, 0.5f);

        Logger.INFO.print("Finished initialisation");
    }

    @Override
    public void initMods(List<Mod> mods) {
        assert activeMods.isEmpty() : "Already mods loaded";
        activeMods = new ArrayList<>(mods);

        // init mods
        for (Mod mod : activeMods) {
            try {
                assert !(mod instanceof InitialisationMod) : "Init mods should not be loaded here";

                mod.init(combinedGame);

            } catch (Exception ex) {
                Logger.ERROR.print("Error while loading " + mod.getModName(), ex);
                mods.remove(mod);
            }
        }
    }

    public void root() throws Exception {
        init();
        Logger.DEBUG.newLine();
        Logger.DEBUG.print("Opening game window...");

        // show main menu
        mainMenu.setVisible(true);
        window.open();
        renderer.run();

        Logger.INFO.newLine();
        Logger.INFO.print("Closing game window and start cleanup...");

        cleanMods();
        cleanup();

        Logger.INFO.print("Game engine is stopped");
        Logger.INFO.newLine();
    }

    @Override
    public void startGame() {
        Logger.INFO.newLine();
        Logger.INFO.print("Starting game...");

        mainMenu.setVisible(false);
        frameManager.setToolBar(mainMenu.getToolBar(combinedGame));

        pocketGame.start();
        worldGame.start();
    }

    private void stopGame() {
        Logger.INFO.print(); // new line
        Logger.INFO.print("Stopping game...");

        pocketGame.stop();
        worldGame.stop();

        frameManager.setToolBar(null);
        cleanMods();
        mainMenu.setVisible(true);

        Logger.DEBUG.print("Game stopped");
    }

    @Override
    public List<Mod> allMods() {
        return Collections.unmodifiableList(allMods);
    }

    @Override
    public Mod getModByName(String name) {
        for (Mod mod : allMods) {
            if (mod.getModName().equals(name)) {
                return mod;
            }
        }
        return null;
    }

    @Override
    public void cleanMods() {
        activeMods.forEach(Mod::cleanup);
        activeMods.clear();
    }

    private void cleanup() {
        permanentMods.forEach(Mod::cleanup);

        pocketGame.cleanup();
        worldGame.cleanup();

        window.cleanup();
        renderer.cleanup();
        inputHandler.cleanup();
    }

    private class SubGame implements Game {
        private final GameTimer gameTimer;
        private Camera thisCamera;

        private EventLoop thisLoop;
        private GameState thisState;
        private GameMap thisMap;
        private GameLights thisLights;
        private ClaimRegistry thisClaims;

        private SubGame(
                EventLoop gameLoop, GameState gameState, GameMap gameMap, GameLights gameLights,
                Camera thisCamera
        ) {
            this.thisLoop = gameLoop;
            this.thisState = gameState;
            this.thisMap = gameMap;
            this.thisLights = gameLights;
            this.thisCamera = thisCamera;
            this.gameTimer = new GameTimer(settings.RENDER_DELAY);
            this.thisClaims = new ClaimRegistry();
            gameTimer.pause();
        }

        public void init() throws Exception {
            thisLoop.init(this);
            thisState.init(this);
            thisMap.init(this);
            thisLights.init(this);
            thisClaims.init(this);
            thisCamera.init(combinedGame);

            thisLoop.start();
        }

        public void start() {
            thisLoop.unPause();
            gameTimer.unPause();
        }

        public void stop() {
            thisLoop.pause();
            gameTimer.pause();
        }

        @Override
        public GameTimer timer() {
            return gameTimer;
        }

        @Override
        public Camera camera() {
            return thisCamera;
        }

        @Override
        public GameState entities() {
            return thisState;
        }

        @Override
        public GameMap map() {
            return thisMap;
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
        public GUIManager gui() {
            return frameManager;
        }

        @Override
        public MouseToolCallbacks inputHandling() {
            return inputHandler;
        }

        @Override
        public Version getVersion() {
            return GAME_VERSION;
        }

        @Override
        public GameLights lights() {
            return thisLights;
        }

        @Override
        public ClaimRegistry claims() {
            return thisClaims;
        }

        @Override
        public void addEvent(Event e) {
            thisLoop.addEvent(e);
        }

        @Override
        public void executeOnRenderThread(Runnable action) {
            boolean thisIsMainThread = Thread.currentThread().getName().equals(MAIN_THREAD);

            if (thisIsMainThread) {
                action.run();

            } else {
                renderer.defer(action);
            }
        }

        @Override
        public void writeStateToFile(DataOutput out) throws IOException {
            GAME_VERSION.writeToFile(out);

            // store timestamp
            out.writeFloat(gameTimer.getGametime());

            // write mods
            Collection<Mod> listOfMods = allMods();
            out.writeInt(listOfMods.size());

            for (Mod mod : listOfMods) {
                out.writeUTF(mod.getModName());
                mod.getVersionNumber().writeToFile(out);
            }

            Storable.writeToFile(out, thisLoop);
            Storable.writeToFile(out, thisState);
            Storable.writeToFile(out, thisMap);
            Storable.writeToFile(out, thisLights);
            Storable.writeToFile(out, thisClaims);
        }

        @Override
        public void readStateFromFile(DataInput in) throws Exception {
            Version fileVersion = new Version(in);
            Logger.INFO.print("Reading state of version " + fileVersion);

            float restoredTime = in.readFloat();

            int nOfMods = in.readInt();
            if (nOfMods > 0) Logger.INFO.print("Required mods:");

            for (int i = 0; i < nOfMods; i++) {
                String modName = in.readUTF();
                Mod targetMod = getModByName(modName);
                Version version = new Version(in);

                boolean hasMod = targetMod != null;
                Logger logger = hasMod ? Logger.INFO : Logger.WARN;
                logger.printf("\t%s %s (%s)", modName, version, hasMod ? ("PROVIDED " + targetMod.getVersionNumber()) : "MISSING");
            }

            EventLoop newLoop = Storable.readFromFile(in, EventLoop.class);
            GameState newState = Storable.readFromFile(in, GameState.class);
            GameMap newMap = Storable.readFromFile(in, GameMap.class);
            GameLights newLights = Storable.readFromFile(in, GameLights.class);
            ClaimRegistry newClaims = Storable.readFromFile(in, ClaimRegistry.class);

            newLights.init(this);
            newState.init(this);
            newMap.init(this);
            newLights.init(this);

            // replace at end of gameloop
            thisLoop.defer(() -> {
                // clean up all the replaced stuff
                this.thisLoop.stopLoop();
                this.thisState.cleanup();
                this.thisMap.cleanup();
                this.thisLights.cleanup();
                this.thisClaims.cleanup();

                // set new state
                this.thisLoop = newLoop;
                this.thisState = newState;
                this.thisMap = newMap;
                this.thisLights = newLights;
                this.thisClaims = newClaims;

                gameTimer.set(restoredTime);
            });
        }

        @Override
        public void loadMap(File map) throws Exception {
            FileInputStream fs = new FileInputStream(map);
            DataInput input = new DataInputStream(fs);
            GameMap newMap = Storable.readFromFile(input, GameMap.class);

            newMap.init(this);
            this.thisMap.cleanup();
            this.thisMap = newMap;
        }

        public void cleanup() {
            thisLoop.stopLoop();
            thisState.cleanup();
            thisMap.cleanup();
            thisLights.cleanup();
            thisCamera.cleanup();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }
}
