package NG.Engine;

import NG.ActionHandling.MouseToolCallbacks;
import NG.Camera.Camera;
import NG.Camera.TycoonFixedCamera;
import NG.DataStructures.Generic.Color4f;
import NG.GameMap.GameMap;
import NG.GameMap.TileMap;
import NG.GameState.GameLights;
import NG.GameState.GameLoop;
import NG.GameState.GameState;
import NG.GameState.SingleShadowMapLights;
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A game of planning and making money.
 * <p>
 * This class initializes all gameAspects, allow for starting a game, loading mods and cleaning up afterwards. It
 * provides all aspects of the game engine through the {@link Game} interface.
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class MonsterGame implements Game, ModLoader {
    private static final Version GAME_VERSION = new Version(0, 0);
    private final String MAIN_THREAD;

    private final GameTimer time;
    private final RenderLoop renderer;
    private final Settings settings;
    private final GLFWWindow window;
    private final MouseToolCallbacks inputHandler;
    private final GUIManager frameManager;

    private GameLoop gameState;
    private GameMap gameMap;
    private GameLights gameLights;
    private MainMenu mainMenu;
    private Camera camera;

    private List<Mod> allMods;
    private List<Mod> activeMods = Collections.emptyList();
    private List<Mod> permanentMods;

    public MonsterGame() throws IOException {
        Logger.INFO.print("Starting up the game engine...");
        MAIN_THREAD = Thread.currentThread().getName();

        Logger.DEBUG.print("General debug information: " +
                // manual aligning will do the trick
                "\n\tSystem OS:          " + System.getProperty("os.name") +
                "\n\tJava VM:            " + System.getProperty("java.runtime.version") +
                "\n\tFrame version:      " + getVersion() +
                "\n\tWorking directory:  " + Directory.workDirectory() +
                "\n\tMods directory:     " + Directory.mods.getPath()
        );

        // these two are not GameAspects, and thus the init() rule does not apply.
        settings = new Settings();
        time = new GameTimer(settings.RENDER_DELAY);

        camera = new TycoonFixedCamera(new Vector3f(), 10, 10);
        window = new GLFWWindow(Settings.GAME_NAME, settings, true);
        renderer = new RenderLoop(settings.TARGET_FPS);
        gameState = new GameLoop(Settings.GAME_NAME, settings.TARGET_TPS);
        gameMap = new TileMap(settings.CHUNK_SIZE);
        inputHandler = new MouseToolCallbacks();
        frameManager = new SFrameManager();
        gameLights = new SingleShadowMapLights();

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
        window.init(this);
        renderer.init(this);
        camera.init(this);
        gameState.init(this);
        inputHandler.init(this);
        frameManager.init(this);
        gameMap.init(this);
        gameLights.init(this);

        permanentMods = JarModReader.filterInitialisationMods(allMods, this);

        gameLights.addDirectionalLight(new Vector3f(1, -1.5f, 2), Color4f.WHITE, 0.5f);
        renderer.addHudItem(frameManager::draw);
        mainMenu = new MainMenu(this, this, renderer::stopLoop);
        frameManager.addFrame(mainMenu);
        gameState.start();

        Logger.INFO.print("Finished initialisation\n");
    }

    @Override
    public void initMods(List<Mod> mods) {
        assert activeMods.isEmpty() : "Already mods loaded";
        activeMods = new ArrayList<>(mods);

        // init mods
        for (Mod mod : activeMods) {
            try {
                assert !(mod instanceof InitialisationMod) : "Init mods should not be loaded here";

                mod.init(this);

            } catch (Exception ex) {
                Logger.ERROR.print("Error while loading " + mod.getModName(), ex);
                mods.remove(mod);
            }
        }
    }

    public void root() throws Exception {
        init();
        Logger.INFO.print("Starting game...\n");

        // show main menu
        mainMenu.setVisible(true);
        window.open();
        renderer.run();

        cleanMods();
        cleanup();
    }

    @Override
    public void startGame() {
        mainMenu.setVisible(false);
//        frameManager.setToolBar(toolBar);
        gameState.unPause();
    }

    private void stopGame() {
        gameState.pause();
        gameState.cleanup();
        gameMap.cleanup();
        frameManager.setToolBar(null);
        cleanMods();
        mainMenu.setVisible(true);
    }

    @Override
    public GameTimer timer() {
        return time;
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
    public Version getVersion() {
        return GAME_VERSION;
    }

    @Override
    public GameLights lights() {
        return gameLights;
    }

    @Override
    public void executeOnRenderThread(Runnable action) {
        if (Thread.currentThread().getName().equals(MAIN_THREAD)) {
            action.run();

        } else {
            renderer.defer(action);
        }
    }

    @Override
    public void writeStateToFile(DataOutput out) throws IOException {
        GAME_VERSION.writeToFile(out);

        Collection<Mod> listOfMods = allMods();
        out.writeInt(listOfMods.size());

        for (Mod mod : listOfMods) {
            out.writeUTF(mod.getModName());
            mod.getVersionNumber().writeToFile(out);
        }

        Storable.writeToFile(out, gameState);
        Storable.writeToFile(out, gameMap);
        Storable.writeToFile(out, gameLights);
    }

    @Override
    public void readStateFromFile(DataInput in) throws IOException {
        Version fileVersion = new Version(in);
        Logger.INFO.print("Reading state of version " + fileVersion);

        GameLoop newState;
        GameMap newMap;
        GameLights newLights;

        try {
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

            newState = Storable.readFromFile(in, GameLoop.class);
            newMap = Storable.readFromFile(in, GameMap.class);
            newLights = Storable.readFromFile(in, GameLights.class);

            newState.init(this);
            newMap.init(this);
            newLights.init(this);

            gameState.defer(() -> {
                // clean up all the replaced stuff
                this.gameState.stopLoop();
                this.gameMap.cleanup();
                this.gameLights.cleanup();

                // set new state
                this.gameState = newState;
                this.gameMap = newMap;
                this.gameLights = newLights;
            });

        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public GUIManager gui() {
        return frameManager;
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

    private void cleanup() {
        gameState.stopLoop();
        permanentMods.forEach(Mod::cleanup);

        window.cleanup();
        renderer.cleanup();
        camera.cleanup();
        inputHandler.cleanup();
    }

    @Override
    public void cleanMods() {
        activeMods.forEach(Mod::cleanup);
        activeMods.clear();
    }
}
