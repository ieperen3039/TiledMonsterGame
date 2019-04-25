package NG.Engine;

import NG.Camera.Camera;
import NG.Camera.TycoonFixedCamera;
import NG.DataStructures.Generic.Color4f;
import NG.Entities.Entity;
import NG.GUIMenu.Frames.GUIManager;
import NG.GUIMenu.Frames.SFrameManager;
import NG.GUIMenu.Menu.MainMenu;
import NG.GameEvent.EventLoop;
import NG.GameEvent.GameEventQueueLoop;
import NG.GameMap.EmptyMap;
import NG.GameMap.GameMap;
import NG.GameMap.TileMap;
import NG.GameState.DynamicState;
import NG.GameState.GameLights;
import NG.GameState.GameState;
import NG.GameState.SingleShadowMapLights;
import NG.InputHandling.ClickShader;
import NG.InputHandling.MouseToolCallbacks;
import NG.Mods.Mod;
import NG.Particles.GameParticles;
import NG.Particles.ParticleShader;
import NG.Rendering.GLFWWindow;
import NG.Rendering.RenderLoop;
import NG.Rendering.Shaders.BlinnPhongShader;
import NG.Rendering.Shaders.WorldBPShader;
import NG.Settings.Settings;
import NG.Storable;
import NG.Tools.Directory;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * This class initializes all gameAspects, allow for starting a game, loading mods and cleaning up afterwards.
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class MonsterGame implements ModLoader {
    public static final Version GAME_VERSION = new Version(0, 4);

    private final RenderLoop renderer;
    private final GLFWWindow window;
    private final MouseToolCallbacks inputHandler;
    private final GUIManager frameManager;
    private MainMenu mainMenu;

    private Game pocketGame;
    private Game worldGame;
    private Game.Multiplexer combinedGame;

    private List<Mod> allMods;
    private List<Mod> activeMods = Collections.emptyList();
    private List<Mod> permanentMods;

    public MonsterGame() throws IOException {
        Logger.INFO.print("Starting up the game engine...");
        String mainThreadName = Thread.currentThread().getName();

        // these are not GameAspects, and thus the init() rule does not apply.
        Settings settings = new Settings();
        window = new GLFWWindow(Settings.GAME_NAME, settings, true);

        renderer = new RenderLoop(settings.TARGET_FPS);
        inputHandler = new MouseToolCallbacks();
        frameManager = new SFrameManager();

        GameMap pocketMap = new EmptyMap();
        GameService pocketGame = createWorld("pocket", mainThreadName, settings, pocketMap);
        this.pocketGame = pocketGame;

        GameMap worldMap = new TileMap(Settings.CHUNK_SIZE);
        GameService worldGame = createWorld("world", mainThreadName, settings, worldMap);
        this.worldGame = worldGame;

        combinedGame = new Game.Multiplexer(0, worldGame, pocketGame);
        Logger.printOnline(() -> "Current view: " + (combinedGame.current() == 0 ? "World" : "Pocket"));
    }

    private GameService createWorld(String name, String mainThreadName, Settings settings, GameMap pocketMap) {
        Camera pocketView = new TycoonFixedCamera(new Vector3f(), 10, 10);
        EventLoop pocketGameLoop = new GameEventQueueLoop(name + " Loop", settings.TARGET_TPS);
        GameState pocketGameState = new DynamicState();
        GameLights pocketLights = new SingleShadowMapLights();
        GameParticles pocketParticles = new GameParticles();
        GameTimer pocketTimer = new GameTimer(settings.RENDER_DELAY);

        return new GameService(GAME_VERSION, mainThreadName,
                pocketGameLoop, pocketGameState, pocketMap, pocketLights, pocketView, pocketParticles, pocketTimer,
                settings, window, renderer, inputHandler, frameManager
        );
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

        Logger.DEBUG.print("Installing basic elements...");

        // world
        renderer.getRenderSequence(new WorldBPShader())
                .add(combinedGame.get(GameLights.class)::draw)
                .add(combinedGame.get(GameMap.class)::draw);
        // entities
        renderer.getRenderSequence(new BlinnPhongShader())
                .add(combinedGame.get(GameLights.class)::draw)
                .add(combinedGame.get(GameState.class)::draw);
        // particles
        renderer.getRenderSequence(new ParticleShader())
                .add(combinedGame.get(GameParticles.class)::draw);

        mainMenu = new MainMenu(worldGame, pocketGame, this, renderer::stopLoop);
        frameManager.addFrame(mainMenu);

        inputHandler.addKeyPressListener(k -> {
            if (k == GLFW.GLFW_KEY_SPACE) {
                combinedGame.executeOnRenderThread(() -> {
                    combinedGame.select((combinedGame.current() + 1) % 2);
                });
            }
        });

        Logger.DEBUG.print("Installing optional elements...");

        // hitboxes
        renderer.getRenderSequence(null)
                .add(gl -> {
                    if (combinedGame.get(Settings.class).RENDER_HITBOXES) return;

                    Collection<Entity> entities = combinedGame.get(GameState.class).entities();
                    Toolbox.drawHitboxes(gl, entities, combinedGame.get(GameTimer.class).getGametime());
                });

        // clickshader
        try {
            ClickShader shader = combinedGame.computeOnRenderThread(ClickShader::new).get();
            combinedGame.add(shader);

        } catch (Exception ex) {
            Logger.WARN.print("Could not start ClickShader: " + ex);
            // we have backups if it doesn't work for whatever reason
        }

        inputHandler.addKeyPressListener(k -> {
            switch (k) {
                case GLFW.GLFW_KEY_ESCAPE:
                    renderer.stopLoop();
                    break;
                case GLFW.GLFW_KEY_F11:
                    window.toggleFullScreen();
                    break;
            }
        });

        Logger.DEBUG.print("Loading mods...");

        allMods = JarModReader.loadMods(Directory.mods);
        permanentMods = JarModReader.filterInitialisationMods(allMods, combinedGame);
        initMods(permanentMods);

        Logger.DEBUG.print("Initial world setup...");

        final Color4f HALOGEN = Color4f.rgb(255, 241, 224);
        pocketGame.get(GameLights.class).addDirectionalLight(new Vector3f(1, 1, 2), HALOGEN, 0.8f);
        worldGame.get(GameLights.class).addDirectionalLight(new Vector3f(2, 1.5f, 0.5f), Color4f.WHITE, 0.5f);

        Logger.DEBUG.print("Booting game loops...");

        pocketGame.getAll(GameEventQueueLoop.class).forEach(Thread::start);
        worldGame.getAll(GameEventQueueLoop.class).forEach(Thread::start);

        Logger.INFO.print("Finished initialisation");
    }

    @Override
    public void initMods(List<Mod> mods) {
        for (Iterator<Mod> it = mods.iterator(); it.hasNext(); ) {
            Mod mod = it.next();
            try {
                mod.init(combinedGame);

            } catch (Exception ex) {
                Logger.ERROR.print("Error while loading " + mod.getModName(), ex);
                it.remove();
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

        pocketGame.getAll(GameEventQueueLoop.class).forEach(AbstractGameLoop::unPause);
        worldGame.getAll(GameEventQueueLoop.class).forEach(AbstractGameLoop::unPause);
    }

    @Override
    public void stopGame() {
        Logger.INFO.newLine();
        Logger.INFO.print("Stopping game...");

        pocketGame.getAll(GameEventQueueLoop.class).forEach(AbstractGameLoop::pause);
        worldGame.getAll(GameEventQueueLoop.class).forEach(AbstractGameLoop::pause);

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
        removeStorablesFrom(pocketGame);
        pocketElts.forEach(pocketGame::add);
        pocketGame.init();

        List<Storable> worldElts = Storable.readCollection(in, Storable.class);
        removeStorablesFrom(worldGame);
        worldElts.forEach(worldGame::add);
        worldGame.init();

        combinedGame.get(GameTimer.class).set(restoredTime);
    }

    public void removeStorablesFrom(Game game) {
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
