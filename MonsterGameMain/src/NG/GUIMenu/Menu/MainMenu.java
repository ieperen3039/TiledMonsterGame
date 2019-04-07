package NG.GUIMenu.Menu;

import NG.Animations.BodyAnimation;
import NG.Animations.BodyModel;
import NG.Animations.PartialAnimation;
import NG.Camera.Camera;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.Game;
import NG.Engine.GameTimer;
import NG.Engine.ModLoader;
import NG.Entities.Cube;
import NG.Entities.CubeMonster;
import NG.Entities.Entity;
import NG.Entities.MonsterEntity;
import NG.GUIMenu.Frames.Components.*;
import NG.GUIMenu.Frames.GUIManager;
import NG.GUIMenu.SToolBar;
import NG.GameMap.*;
import NG.GameState.GameLights;
import NG.GameState.GameState;
import NG.InputHandling.KeyMouseCallbacks;
import NG.InputHandling.MouseTools.DefaultMouseTool;
import NG.Living.Commands.Command;
import NG.Living.Commands.CommandWalk;
import NG.Living.MonsterSoul;
import NG.Living.Player;
import NG.Particles.GameParticles;
import NG.Particles.ParticleCloud;
import NG.Particles.Particles;
import NG.Settings.Settings;
import NG.Tools.Directory;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.*;

import java.lang.Math;
import java.util.List;

/**
 * @author Geert van Ieperen. Created on 28-9-2018.
 */
public class MainMenu extends SFrame {
    public static final int BUTTON_MIN_WIDTH = 300;
    public static final int BUTTON_MIN_HEIGHT = 50;
    private static final int NOF_ENTITIES = 4 * 4 * 4;

    private final Game overworld;
    private final Game pocketworld;
    private final ModLoader modLoader;
    private final SFrame newGameFrame;

    public MainMenu(Game overworld, Game pocketworld, ModLoader modManager, Runnable exitGameAction) {
        super("Main Menu", 400, 500, false);
        this.overworld = overworld;
        this.pocketworld = pocketworld;
        this.modLoader = modManager;

        newGameFrame = new NewGameFrame(overworld, modLoader);

        setMainPanel(SPanel.row(
                new SFiller(),
                SPanel.column(
                        new SButton("Start new game", this::showNewGamePanel, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT),
                        new SButton("Start Testworld", this::testWorld, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT),
                        new SButton("Animation Tester", this::animationDisplay, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT),
                        new SButton("Particle Tester", this::particles, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT),
                        new SFiller(),
                        new SButton("Exit game", exitGameAction, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT)
                ),
                new SFiller()
        ));
    }

    private void particles() {
        GUIManager targetGUI = overworld.get(GUIManager.class);

        targetGUI.addFrame(new SFrame("EXPLOSIONS").setMainPanel(SPanel.column(
                new STextArea("MR. TORGUE APPROVES", BUTTON_MIN_HEIGHT),
                new SButton("BOOM", () -> {
                    ParticleCloud cloud = Particles.explosion(
                            Vectors.O, Vectors.O, Color4f.RED, Color4f.ORANGE,
                            (int) (overworld.get(Settings.class).PARTICLE_MODIFIER * 1000),
                            5f, 10f
                    );
                    overworld.get(GameParticles.class).add(cloud);
                }, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT)
        )));
        dispose();
    }

    public void testWorld() {
        Logger.DEBUG.printFrom(2, "Start test-world");
        try {
            int xSize = Settings.CHUNK_SIZE;
            int ySize = Settings.CHUNK_SIZE;
            overworld.get(ClaimRegistry.class).cleanup();

            TileThemeSet.PLAIN.load();

            // random map
            int seed = Math.abs(Toolbox.random.nextInt());
            MapGeneratorMod mapGenerator = new SimpleMapGenerator(seed);
            mapGenerator.setSize(xSize + 1, ySize + 1);
            overworld.get(GameMap.class).generateNew(mapGenerator);

            pocketworld.loadMap(Directory.savedMaps.getFile("pocketDefault.mgm"));
            centerCamera(pocketworld);

            modLoader.initMods(modLoader.allMods());

            // set camera to middle of map
            Vector3f cameraFocus = centerCamera(overworld);

            /* --- DEBUG SECTION --- */

            // add a default entity
            Vector3i position = overworld.get(GameMap.class).getCoordinate(cameraFocus);
            MonsterSoul monsterSoul = new CubeMonster(Directory.souls.getFile("soul1.txt"));
            MonsterEntity cow = monsterSoul.getAsEntity(overworld, new Vector2i(position.x, position.y), Vectors.X);
            overworld.get(GameState.class).addEntity(cow);

            // give it some command
            Command command = new CommandWalk(new Player(), monsterSoul, new Vector2i(position.x + 2, position.y + 2));
            monsterSoul.accept(command);

            /* --- END SECTION --- */

            // add lights
            overworld.get(GameLights.class).addDirectionalLight(new Vector3f(1, 1.5f, 1f), Color4f.WHITE, 0.2f);

            // start
            start();

        } catch (Exception e) {
            Logger.ERROR.print(e);
        }
    }

    private void start() {
        modLoader.startGame();
        newGameFrame.setVisible(false);
    }

    private Vector3f centerCamera(Game game) {
        Vector2ic edge = game.get(GameMap.class).getSize();
        Vector3f cameraFocus = game.get(GameMap.class).getPosition(edge.x() / 2, edge.y() / 2);
        Camera cam = game.get(Camera.class);
        int initialZoom = (int) edge.length();
        Vector3f cameraEye = new Vector3f(cameraFocus).add(-initialZoom, -initialZoom, initialZoom);
        cam.set(cameraFocus, cameraEye);
        return cameraFocus;
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
                    overworld.get(GameState.class).addEntity(cube);
                    if (--i == 0) break cubing;
                }
            }
        }

        Camera cam = overworld.get(Camera.class);
        Vector3f cameraEye = new Vector3f(cbrtc, cbrtc, cbrtc).mul(spacing).add(10, 10, 10);
        cam.set(Vectors.O, cameraEye);

        overworld.get(GameLights.class).addDirectionalLight(new Vector3f(1, 1.5f, 0.5f), Color4f.WHITE, 0.5f);

        start();
    }

    private void animationDisplay() {
        BodyModel[] models = BodyModel.values();
        BodyAnimation[] animations = BodyAnimation.values();

        BodyAnimation baseAni = BodyAnimation.WALK_CYCLE;
        BodyModel baseMode = BodyModel.ANTHRO;

        GUIManager targetGUI = overworld.get(GUIManager.class);
        SDropDown animationSelection = new SDropDown(targetGUI, baseAni.ordinal(), Toolbox.toStringArray(animations));
        SDropDown modelSelection = new SDropDown(targetGUI, baseMode.ordinal(), Toolbox.toStringArray(models));
        PartialAnimation.Demonstrator demonstrator = new PartialAnimation.Demonstrator(baseAni, baseMode, overworld.get(GameTimer.class));

        targetGUI.addFrame(new SFrame("Animations", BUTTON_MIN_WIDTH, 0)
                .setMainPanel(SPanel.column(
                        animationSelection,
                        modelSelection
                ))
        );

        animationSelection.addStateChangeListener((i) -> demonstrator.setAnimation(animations[i]));
        modelSelection.addStateChangeListener(i -> demonstrator.setModel(models[i]));

        overworld.get(GameState.class).addEntity(demonstrator);

        start();
    }

    private void showNewGamePanel() {
        newGameFrame.setVisible(true);
        overworld.get(GUIManager.class).addFrame(newGameFrame);
    }

    public SToolBar getToolBar(Game game) {
        SToolBar toolBar = new SToolBar(game, true);

        toolBar.addButton("Stop", () -> {
            game.get(GUIManager.class).setToolBar(null);
            setVisible(true);
        }, 100);

        toolBar.addButton("A* tester", () -> game.get(KeyMouseCallbacks.class)
                .setMouseTool(new AStartTestMouseTool(game)), 200);

        return toolBar;
    }

    private class AStartTestMouseTool extends DefaultMouseTool {
        Vector2i first;
        private Game game;

        public AStartTestMouseTool(Game game) {
            this.game = game;
        }

        @Override
        public void apply(Vector3fc position) {
            Vector3i temp = game.get(GameMap.class).getCoordinate(position);
            Vector2i second = new Vector2i(temp.x, temp.y);

            if (first == null) {
                this.first = second;
                game.get(GameMap.class).setHighlights(first);

            } else {
                List<Vector2i> path = game.get(GameMap.class).findPath(first, second, 1, 0.1f);
                game.get(GameMap.class).setHighlights(path.toArray(new Vector2ic[0]));
                first = null;
            }
        }

        @Override
        public String toString() {
            return "A* tool";
        }
    }
}
