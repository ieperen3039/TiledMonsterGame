package NG.ScreenOverlay.Menu;

import NG.Camera.Camera;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.Game;
import NG.Engine.ModLoader;
import NG.Entities.Cube;
import NG.Entities.Entity;
import NG.GameState.MapGeneratorMod;
import NG.ScreenOverlay.Frames.Components.*;
import NG.Tools.Vectors;
import org.joml.Vector2i;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen. Created on 28-9-2018.
 */
public class MainMenu extends SFrame {
    // these are upper bounds
    private static final int NUM_TOP_BUTTONS = 10;
    private static final int NUM_BOT_BUTTONS = 10;
    public static final int BUTTON_MIN_WIDTH = 300;
    public static final int BUTTON_MIN_HEIGHT = 50;
    private static final int NOF_ENTITIES = 4 * 4 * 4;

    private final Vector2i topButtonPos;
    private final Vector2i bottomButtonPos;
    public static final int NUM_BUTTONS = NUM_TOP_BUTTONS + NUM_BOT_BUTTONS + 1;
    private final Game game;
    private final ModLoader modLoader;
    private final SFrame newGameFrame;

    public MainMenu(Game game, ModLoader modManager, Runnable exitGameAction) {
        super("Main Menu", 400, 500, false);
        this.game = game;
        this.modLoader = modManager;
        topButtonPos = new Vector2i(1, -1);
        bottomButtonPos = new Vector2i(1, NUM_BUTTONS);
        SContainer buttons = new SPanel(3, NUM_BUTTONS);

        newGameFrame = new NewGameFrame(game, modLoader);

        SButton newGame = new SButton("Start new game", this::showNewGame, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
        buttons.add(newGame, onTop());
        SButton justStart = new SButton("Start Testworld", this::testWorld, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
        buttons.add(justStart, onTop());
        SButton entityCloud = new SButton("Start EntityCloud", this::entityCloud, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
        buttons.add(entityCloud, onTop());
        SButton exitGame = new SButton("Exit game", exitGameAction, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
        buttons.add(exitGame, onBot());

        Vector2i mid = onTop();
        buttons.add(new SFiller(), new Vector2i(0, mid.y));
        buttons.add(new SFiller(), new Vector2i(1, mid.y));
        buttons.add(new SFiller(), new Vector2i(2, mid.y));

        setMainPanel(buttons);
    }

    private void testWorld() {
        int xSize = 200;
        int ySize = 200;

        // random map
        MapGeneratorMod mapGenerator = modLoader.allMods().stream()
                .filter(m -> m instanceof MapGeneratorMod)
                .findAny() // any generator
                .map(m -> (MapGeneratorMod) m)
                .orElseThrow();
        mapGenerator.setXSize(xSize);
        mapGenerator.setYSize(ySize);
        game.map().generateNew(mapGenerator);

        modLoader.initMods(modLoader.allMods());

        // set camera to middle of map
        Vector3f cameraFocus = new Vector3f(xSize / 2f, ySize / 2f, 0);
        Camera cam = game.camera();
        Vector3f cameraEye = new Vector3f(cameraFocus).add(-50, -50, 50);
        cam.set(cameraFocus, cameraEye);

        Vector3f pos = new Vector3f(cameraFocus).add(0, 0, 20);
        Entity cube = new Cube(pos);
        game.state().addEntity(cube);

        game.state().setDirectionalLight(new Vector3f(1, 1.5f, 0.5f), Color4f.WHITE, 0.5f);

        // start
        modLoader.startGame();
        newGameFrame.setVisible(false);
    }

    private void entityCloud() {
        final int spacing = 100 / NOF_ENTITIES + 5;
        int cbrtc = (int) Math.ceil(Math.cbrt(NOF_ENTITIES));

        int i = NOF_ENTITIES;
        cubing:
        for (int x = 0; x < cbrtc; x++) {
            for (int y = 0; y < cbrtc; y++) {
                for (int z = 0; z < cbrtc; z++) {
                    Vector3f pos = new Vector3f(x, y, z).mul(spacing);
                    Entity cube = new Cube(pos);
                    game.state().addEntity(cube);
                    if (--i == 0) break cubing;
                }
            }
        }

        Camera cam = game.camera();
        Vector3f cameraEye = new Vector3f(cbrtc, cbrtc, cbrtc).mul(spacing).add(10, 10, 10);
        cam.set(Vectors.zeroVector(), cameraEye);

        game.state().setDirectionalLight(new Vector3f(1, 1.5f, 0.5f), Color4f.WHITE, 0.5f);

        modLoader.startGame();
        newGameFrame.setVisible(false);
    }

    private void showNewGame() {
        newGameFrame.setVisible(true);
        game.gui().addFrame(newGameFrame);
    }

    private Vector2i onTop() {
        return topButtonPos.add(0, 1);
    }

    private Vector2i onBot() {
        return bottomButtonPos.sub(0, 1);
    }

}
