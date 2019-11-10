package NG.Core;

import NG.Camera.Camera;
import NG.Camera.TycoonFixedCamera;
import NG.CollisionDetection.PhysicsEngine;
import NG.DataStructures.Generic.Color4f;
import NG.Entities.Entity;
import NG.GUIMenu.Frames.FrameManagerImpl;
import NG.GUIMenu.HUDManager;
import NG.GUIMenu.Menu.MainMenu;
import NG.GameEvent.EventLoop;
import NG.GameEvent.GameEventQueueLoop;
import NG.GameMap.EmptyMap;
import NG.GameMap.GameMap;
import NG.GameMap.TileMap;
import NG.InputHandling.ClickShader;
import NG.InputHandling.MouseToolCallbacks;
import NG.Mods.JarModReader;
import NG.Mods.Mod;
import NG.Mods.ModLoader;
import NG.Particles.GameParticles;
import NG.Particles.ParticleShader;
import NG.Rendering.GLFWWindow;
import NG.Rendering.Lights.GameLights;
import NG.Rendering.Lights.GameState;
import NG.Rendering.Lights.SingleShadowMapLights;
import NG.Rendering.RenderLoop;
import NG.Rendering.Shaders.BlinnPhongShader;
import NG.Rendering.Shaders.WorldBPShader;
import NG.Settings.KeyBinding;
import NG.Settings.Settings;
import NG.Storable;
import NG.Tools.Directory;
import NG.Tools.Logger;
import NG.Tools.Splash;
import NG.Tools.Toolbox;
import org.joml.Vector3f;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * This class initializes all gameAspects, allow for starting a game, loading mods and cleaning up afterwards.
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class MonsterGame implements ModLoader {
    public static final Version GAME_VERSION = new Version(0, 4);
    private final HUDManager hud;
    private Splash splashWindow;

    private final RenderLoop renderer;
    private final GLFWWindow window;
    private final MouseToolCallbacks inputHandler;
    private MainMenu mainMenu;

    private Game pocketGame;
    private Game worldGame;
    private Game.Multiplexer combinedGame;

    private List<Mod> allMods;
    private List<Mod> activeMods = Collections.emptyList();

    public MonsterGame() throws IOException {
        Logger.DEBUG.print("Showing splash...");
        splashWindow = new Splash();
        splashWindow.run();

        Logger.INFO.print("Starting up the game engine...");
        String mainThreadName = Thread.currentThread().getName();

        // these are not GameAspects, and thus the init() rule does not apply.
        Settings settings = new Settings();
        window = new GLFWWindow(Settings.GAME_NAME, new GLFWWindow.Settings(settings), true);

        renderer = new RenderLoop(settings.TARGET_FPS);
        inputHandler = new MouseToolCallbacks();
//        hud = new MonsterHud();
        hud = new FrameManagerImpl();

        GameService pocketGame = createWorld("pocket", mainThreadName, settings);
        pocketGame.add(new EmptyMap());
        this.pocketGame = pocketGame;

        GameService worldGame = createWorld("world", mainThreadName, settings);
        worldGame.add(new TileMap(Settings.CHUNK_SIZE));
        this.worldGame = worldGame;

        combinedGame = new Game.Multiplexer(0, worldGame, pocketGame);
        Logger.printOnline(() -> "Current view: " + (combinedGame.current() == 0 ? "World" : "Pocket"));
    }

    private GameService createWorld(String name, String mainThreadName, Settings settings) {
        Camera camera = new TycoonFixedCamera(new Vector3f(), 10, 10);
        EventLoop eventLoop = new GameEventQueueLoop(name + " Loop", settings.TARGET_TPS);
        GameState gameState = new PhysicsEngine();
        GameLights lights = new SingleShadowMapLights();
        GameParticles particles = new GameParticles();
        GameTimer timer = new GameTimer(settings.RENDER_DELAY);

        return new GameService(GAME_VERSION, mainThreadName,
                eventLoop, gameState, lights, camera, particles, timer,
                settings, window, renderer, inputHandler, hud
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
        pocketGame.init();
        worldGame.init();

        Logger.DEBUG.print("Installing basic elements...");

        // world
        renderer.renderSequence(new WorldBPShader())
                .add(gl -> combinedGame.get(GameLights.class).draw(gl))
                .add(gl -> combinedGame.get(GameMap.class).draw(gl));
        // entities
        renderer.renderSequence(new BlinnPhongShader())
                .add(gl -> combinedGame.get(GameLights.class).draw(gl))
                .add(gl -> combinedGame.get(GameState.class).draw(gl));
        // particles
        renderer.renderSequence(new ParticleShader())
                .add(gl -> combinedGame.get(GameParticles.class).draw(gl));

        // GUIs
        renderer.addHudItem(painter -> combinedGame.get(HUDManager.class).draw(painter));
        mainMenu = new MainMenu(worldGame, pocketGame, this, renderer::stopLoop);
        hud.addElement(mainMenu);

        Logger.DEBUG.print("Installing optional elements...");

        // hitboxes
        renderer.renderSequence(null)
                .add(gl -> {
                    if (!combinedGame.get(Settings.class).RENDER_HITBOXES) return;

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
            switch (KeyBinding.get(k)) {
                case EXIT_GAME:
                    renderer.stopLoop();
                    break;

                case TOGGLE_FULLSCREEN:
                    window.toggleFullScreen();
                    break;

                case SWITCH_WORLD:
                    combinedGame.executeOnRenderThread(() ->
                            combinedGame.select((combinedGame.current() + 1) % 2)
                    );
                    break;
            }
        });

        Logger.DEBUG.print("Loading mods...");

        allMods = JarModReader.loadMods(Directory.mods);

        Logger.DEBUG.print("Initial world setup...");

        final Color4f HALOGEN = Color4f.rgb(255, 241, 224);
        pocketGame.get(GameLights.class).

                addDirectionalLight(new Vector3f(1, 1, 2), HALOGEN, 0.8f);
        worldGame.get(GameLights.class).

                addDirectionalLight(new Vector3f(2, 1.5f, 0.5f), Color4f.WHITE, 0.5f);

        Logger.DEBUG.print("Booting game loops...");

        pocketGame.getAll(GameEventQueueLoop.class).

                forEach(Thread::start);
        worldGame.getAll(GameEventQueueLoop.class).

                forEach(Thread::start);

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

        // close splash
        splashWindow.dispose();
        splashWindow = null;
        // show main menu
        mainMenu.setVisible(true);
        mainMenu.testWorld(); // immediately start test world, for debugging purposes
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

        pocketGame.getAll(GameEventQueueLoop.class).forEach(AbstractGameLoop::unPause);
        worldGame.getAll(GameEventQueueLoop.class).forEach(AbstractGameLoop::unPause);
    }

    @Override
    public void stopGame() {
        Logger.INFO.newLine();
        Logger.INFO.print("Stopping game...");

        pocketGame.getAll(GameEventQueueLoop.class).forEach(AbstractGameLoop::pause);
        worldGame.getAll(GameEventQueueLoop.class).forEach(AbstractGameLoop::pause);

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
        pocketGame.cleanup();
        worldGame.cleanup();

        window.cleanup();
        renderer.cleanup();
        inputHandler.cleanup();
    }

    /**
     * writes all relevant parts that represent the state of this Game object to the output stream. This can be reverted
     * using {@link #readStateFromFile(DataInputStream)}.
     * @param out an output stream
     */
    public void writeStateToFile(DataOutputStream out) throws IOException {
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

    private static void filterAndStore(DataOutputStream out, Game game) throws IOException {
        List<Storable> box = new ArrayList<>();

        for (Object elt : game) {
            if (elt instanceof Storable) {
                box.add((Storable) elt);
            }
        }

        out.writeInt(box.size());
        for (Storable s : box) {
            Storable.writeSafe(out, s);
        }
    }

    /**
     * reads and restores a state previously written by {@link #writeStateToFile(DataOutputStream)}. After this method
     * returns, the elements that represent the state of this object are set to the streamed state.
     * @param in an input stream, synchronized with the begin of {@link #writeStateToFile(DataOutputStream)}
     */
    public void readStateFromFile(DataInputStream in) throws Exception {
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

        readAndClean(in, pocketGame);
        readAndClean(in, worldGame);

        combinedGame.get(GameTimer.class).set(restoredTime);
    }

    private void readAndClean(DataInputStream in, Game pocketGame) throws Exception {
        int size = in.readInt();
        ArrayList<Object> pocketElts = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Object entity = Storable.readSafe(in, Object.class);
            if (entity == null) continue;
            pocketElts.add(entity);
        }

        Iterator<Object> iterator = pocketGame.iterator();
        while (iterator.hasNext()) {
            Object elt = iterator.next();
            if (elt instanceof Storable) {
                if (elt instanceof GameAspect) {
                    ((GameAspect) elt).cleanup();
                }

                iterator.remove();
            }
        }

        pocketElts.forEach(pocketGame::add);
        pocketGame.init();
    }


}
