package NG.Engine;

import NG.Camera.Camera;
import NG.Camera.TycoonFixedCamera;
import NG.DataStructures.Generic.Color4f;
import NG.GameEvent.EventLoop;
import NG.GameEvent.GameEventQueueLoop;
import NG.GameMap.EmptyMap;
import NG.GameMap.GameMap;
import NG.GameMap.TileMap;
import NG.GameState.GameLights;
import NG.GameState.GameState;
import NG.GameState.SingleShadowMapLights;
import NG.GameState.StaticState;
import NG.InputHandling.MouseToolCallbacks;
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * This class initializes all gameAspects, allow for starting a game, loading mods and cleaning up afterwards. It
 * provides all aspects of the game engine through the {@link Game} interface.
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class MonsterGame implements ModLoader {
    private static final Version GAME_VERSION = new Version(0, 3);

    private final RenderLoop renderer;
    private final GLFWWindow window;
    private final MouseToolCallbacks inputHandler;
    private final GUIManager frameManager;
    private MainMenu mainMenu;

    private Game pocketGame;
    private Game worldGame;
    private Game.Multiplexer combinedGame;
    private boolean currentIsPocket;

    private List<Mod> allMods;
    private List<Mod> activeMods = Collections.emptyList();
    private List<Mod> permanentMods;

    public MonsterGame() throws IOException {
        Logger.INFO.print("Starting up the game engine...");
        String mainThreadName = Thread.currentThread().getName();

        Logger.DEBUG.print("General debug information: " +
                // manual aligning will do the trick
                "\n\tSystem OS:             " + System.getProperty("os.name") +
                "\n\tJava VM:               " + System.getProperty("java.runtime.version") +
                "\n\tGame version:          " + GAME_VERSION +
                "\n\tMain directory         " + Directory.workDirectory() +
                "\n\tMods directory:        " + Directory.mods.getPath()
        );

        // these are not GameAspects, and thus the init() rule does not apply.
        Settings settings = new Settings();
        window = new GLFWWindow(Settings.GAME_NAME, settings, true);

        renderer = new RenderLoop(settings.TARGET_FPS);
        inputHandler = new MouseToolCallbacks();
        frameManager = new SFrameManager();

        Camera pocketView = new TycoonFixedCamera(new Vector3f(), 10, 10);
        EventLoop pocketGameLoop = new GameEventQueueLoop(settings.TARGET_TPS);
        GameState pocketGameState = new StaticState();
        GameLights pocketLights = new SingleShadowMapLights();
        GameMap pocketMap = new EmptyMap();
        pocketGame = new GameService(GAME_VERSION, mainThreadName,
                pocketGameLoop, pocketGameState, pocketMap, pocketLights, pocketView,
                settings, window, renderer, inputHandler, frameManager
        );

        Camera worldView = new TycoonFixedCamera(new Vector3f(), 10, 10);
        EventLoop worldGameLoop = new GameEventQueueLoop(settings.TARGET_TPS);
        GameState worldGameState = new StaticState();
        GameLights worldLights = new SingleShadowMapLights();
        GameMap worldMap = new TileMap(settings.CHUNK_SIZE);
        worldGame = new GameService(GAME_VERSION, mainThreadName,
                worldGameLoop, worldGameState, worldMap, worldLights, worldView,
                settings, window, renderer, inputHandler, frameManager
        );

        combinedGame = new Game.Multiplexer(1, worldGame, pocketGame);
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
        renderer.init(combinedGame);
        inputHandler.init(combinedGame);
        frameManager.init(combinedGame);
        pocketGame.init();
        worldGame.init();

        Logger.DEBUG.print("Initial world processing...");

        renderer.addHudItem(frameManager::draw);

        permanentMods = JarModReader.filterInitialisationMods(allMods, combinedGame);

        mainMenu = new MainMenu(worldGame, pocketGame, this, renderer::stopLoop);
        frameManager.addFrame(mainMenu);

        inputHandler.addKeyPressListener(k -> {
            if (k == GLFW.GLFW_KEY_SPACE) {
                combinedGame.executeOnRenderThread(() -> {
                    combinedGame.select(currentIsPocket ? 1 : 0);
                    currentIsPocket = !currentIsPocket;
                });
            }
        });

        pocketGame.get(GameLights.class).addDirectionalLight(new Vector3f(1, 1, 2), Color4f.rgb(255, 241, 224), 0.8f);
        worldGame.get(GameLights.class).addDirectionalLight(new Vector3f(2, 1.5f, 0.5f), Color4f.WHITE, 0.5f);

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
//        mainMenu.testWorld(); // immediately start test world, for debugging purposes
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

        pocketGame.get(GameTimer.class).unPause();
        worldGame.get(GameTimer.class).unPause();
    }

    private void stopGame() {
        Logger.INFO.print(); // new line
        Logger.INFO.print("Stopping game...");

        pocketGame.get(GameTimer.class).pause();
        worldGame.get(GameTimer.class).pause();

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

    /**
     * writes all relevant parts that represent the state of this Game object to the output stream. This can be reverted
     * using {@link #readStateFromFile(DataInput)}.
     * @param out an output stream
     */
    public void writeStateToFile(DataOutput out) throws IOException {
        GAME_VERSION.writeToDataStream(out);

        // store timestamp
        out.writeFloat(combinedGame.get(GameTimer.class).getGametime());

        // write mods
        Collection<Mod> listOfMods = allMods();
        out.writeInt(listOfMods.size());

        for (Mod mod : listOfMods) {
            out.writeUTF(mod.getModName());
            mod.getVersionNumber().writeToDataStream(out);
        }

        filterAndStore(out, pocketGame);
        filterAndStore(out, worldGame);
    }

    private static void filterAndStore(DataOutput out, Game game) throws IOException {
        List<Storable> box = new ArrayList<>();

        for (Object elt : game) {
            if (elt instanceof Storable) {
                box.add((Storable) elt);
            }
        }

        Storable.writeCollection(out, box);
    }

    /**
     * reads and restores a state previously written by {@link #writeStateToFile(DataOutput)}. After this method
     * returns, the elements that represent the state of this object are set to the streamed state.
     * @param in an input stream, synchronized with the begin of {@link #writeStateToFile(DataOutput)}
     */
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

        List<Storable> pocketElts = Storable.readCollection(in, Storable.class);
        cleanBeforeRead(pocketGame);
        pocketElts.forEach(pocketGame::add);
        pocketGame.init();

        List<Storable> worldElts = Storable.readCollection(in, Storable.class);
        cleanBeforeRead(worldGame);
        worldElts.forEach(worldGame::add);
        worldGame.init();

        combinedGame.get(GameTimer.class).set(restoredTime);
    }

    public void cleanBeforeRead(Game game) {
        Iterator<Object> iterator = game.iterator();
        while (iterator.hasNext()) {
            Object elt = iterator.next();
            if (elt instanceof Storable) {
                if (elt instanceof GameAspect) {
                    ((GameAspect) elt).cleanup();
                }

                iterator.remove();
            }
        }
    }
}
