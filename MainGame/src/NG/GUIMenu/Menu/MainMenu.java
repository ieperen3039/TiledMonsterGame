package NG.GUIMenu.Menu;

import NG.Animations.BodyAnimation;
import NG.Animations.BodyModel;
import NG.Animations.PartialAnimation;
import NG.Animations.RobotMeshes;
import NG.Camera.Camera;
import NG.CollisionDetection.GameState;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Generic.Color4f;
import NG.Entities.CubeMonster;
import NG.GUIMenu.Components.*;
import NG.GUIMenu.HUD.HUDManager;
import NG.GameMap.GameMap;
import NG.GameMap.MapGeneratorMod;
import NG.GameMap.SimpleMapGenerator;
import NG.GameMap.TileThemeSet;
import NG.InputHandling.MouseTools.DefaultMouseTool;
import NG.Living.MonsterSoul;
import NG.Living.Player;
import NG.Mods.ModLoader;
import NG.Particles.GameParticles;
import NG.Particles.ParticleCloud;
import NG.Particles.Particles;
import NG.Rendering.Lights.GameLights;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.RenderLoop;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Rendering.Shapes.GenericShapes;
import NG.Rendering.TilePointer;
import NG.Tools.Directory;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Collection;

import static NG.Rendering.Shapes.GenericShapes.CUBE;

/**
 * @author Geert van Ieperen. Created on 28-9-2018.
 */
public class MainMenu extends SFrame {

    private final Game overworld;
    private final Game pocketworld;
    private final ModLoader modLoader;
    private Mesh demoS3DModel = CUBE;

    public MainMenu(Game overworld, Game pocketworld, ModLoader modManager, Runnable exitGameAction) {
        super("Main Menu", 400, 500, false);
        this.overworld = overworld;
        this.pocketworld = pocketworld;
        this.modLoader = modManager;

        setMainPanel(SPanel.row(
                new SFiller(),
                SPanel.column(
                        new SButton("Start new game", this::showNewGamePanel),
                        new SButton("Start Testworld", this::testWorld),
                        new SButton("Animation Tester", this::animationDisplay),
                        new SButton("Particle Tester", this::particles),
                        new SButton("GUI Tester", this::gui),
                        new SFiller(),
                        new SButton("Exit game", exitGameAction)
                ),
                new SFiller()
        ));
    }

    private void gui() {
        SFrame newFrame = new SFrame("Gui tester", SPanel.column(
                new S3DModelDisplay(
                        overworld, 500, 500, this::drawCube, CUBE.getBoundingBox(),
                        () -> new Vector3f(2, 0, 1).rotateZ(overworld.get(GameTimer.class).getRendertime())
                ),
                new STileBrowser(true, 600, 150,
                        new SButton("Cube", () -> demoS3DModel = CUBE, 150, 150),
                        new SButton("Arrow", () -> demoS3DModel = GenericShapes.ARROW, 150, 150),
                        new SButton("Icosahedron", () -> demoS3DModel = GenericShapes.ICOSAHEDRON, 150, 150),
                        new SButton("Inv cube", () -> demoS3DModel = GenericShapes.INV_CUBE, 150, 150),
                        new SButton("Robot Torso", () -> demoS3DModel = RobotMeshes.ROBOT_TORSO, 150, 150)
                )
        ));
        overworld.get(HUDManager.class).addElement(newFrame);
    }

    private void drawCube(SGL gl) {
        ShaderProgram s = gl.getShader();
        if (s instanceof MaterialShader) {
            ((MaterialShader) s).setMaterial(Material.ROUGH, Color4f.GREY);
        }

        gl.render(demoS3DModel, null);
    }

    private void particles() {
        HUDManager targetGUI = overworld.get(HUDManager.class);
        Vector3fc center = overworld.get(Camera.class).getEye();

        overworld.get(TilePointer.class).setVisible(false);

        targetGUI.addElement(new SFrame("EXPLOSIONS").setMainPanel(SPanel.column(
                new STextArea("MR. TORGUE APPROVES", SButton.BUTTON_MIN_HEIGHT),
                new SButton("BOOM", () -> {
                    ParticleCloud cloud = Particles.explosion(
                            center, Vectors.O, Color4f.RED, Color4f.ORANGE,
                            50_000, 5f, 10f
                    );
                    overworld.get(GameParticles.class).add(cloud);
                })
        )));
    }

    public void testWorld() {
        Logger.DEBUG.printFrom(2, "Start test-world");
        try {
            int xSize = 32;
            int ySize = 32;

            GameState state = overworld.get(GameState.class);
            state.cleanup();
            state.init(overworld);

            TileThemeSet.BASE.load();

            // random map
            int seed = Math.abs(Toolbox.random.nextInt());
            MapGeneratorMod mapGenerator = new SimpleMapGenerator(seed);
            mapGenerator.setSize(xSize + 1, ySize + 1);
            GameMap gameMap = overworld.get(GameMap.class);
            gameMap.generateNew(mapGenerator);

            centerCamera(overworld.get(Camera.class), gameMap);

            modLoader.initMods(modLoader.allMods());

            // set camera to middle of map
            Vector3f cameraFocus = centerCamera(overworld.get(Camera.class), gameMap);
            overworld.get(TilePointer.class).setVisible(true);

            /* --- DEBUG SECTION --- */
            Player player = overworld.get(Player.class);

            // add a default entity
            Vector2ic position = gameMap.getCoordinate(cameraFocus);
            MonsterSoul monsterSoul1 = new CubeMonster(Directory.souls.getFile("soul1.txt"));
            state.addEntity(monsterSoul1.getAsEntity(overworld, new Vector2i(position).add(1, 2), Vectors.X));
            MonsterSoul monsterSoul2 = new CubeMonster(Directory.souls.getFile("soul1.txt"));
            state.addEntity(monsterSoul2.getAsEntity(overworld, new Vector2i(position).add(-1, 2), Vectors.X));
            MonsterSoul monsterSoul3 = new CubeMonster(Directory.souls.getFile("soul1.txt"));
            state.addEntity(monsterSoul3.getAsEntity(overworld, new Vector2i(position).add(1, -2), Vectors.X));
            MonsterSoul monsterSoul4 = new CubeMonster(Directory.souls.getFile("soul1.txt"));
            state.addEntity(monsterSoul4.getAsEntity(overworld, new Vector2i(position).add(-1, -2), Vectors.X));

            player.addToTeam(monsterSoul1);
            player.addToTeam(monsterSoul2);

            /* --- END SECTION --- */

            // add lights
            overworld.get(GameLights.class).addDirectionalLight(new Vector3f(1, 1.5f, 1f), Color4f.WHITE, 0.2f);

            // start
            modLoader.startGame();

        } catch (Exception e) {
            Logger.ERROR.print(e);
        }
    }

    public static Vector3f centerCamera(Camera cam, GameMap map) {
        Vector2ic edge = map.getSize();
        Vector3f cameraFocus = map.getPosition(edge.x() / 2, edge.y() / 2);
        float initialZoom = Math.min(Math.max((float) edge.length() / 4, 8), 100);
        Vector3f cameraEye = new Vector3f(cameraFocus).add(-initialZoom, -initialZoom, initialZoom);
        cam.set(cameraFocus, cameraEye);
        return cameraFocus;
    }

    private void animationDisplay() {
        BodyModel[] models = BodyModel.values();
        BodyAnimation[] animations = BodyAnimation.values();

        BodyAnimation baseAni = BodyAnimation.BASE_POSE;
        BodyModel baseMode = BodyModel.ANTHRO;

        HUDManager targetGUI = overworld.get(HUDManager.class);
        SDropDown animationSelection = new SDropDown(targetGUI, baseAni.ordinal(), Toolbox.toStringArray(animations));
        SDropDown modelSelection = new SDropDown(targetGUI, baseMode.ordinal(), Toolbox.toStringArray(models));
        PartialAnimation.Demonstrator demonstrator = new PartialAnimation.Demonstrator(baseAni, baseMode, overworld.get(GameTimer.class));

        targetGUI.addElement(new SFrame("Animations", SButton.BUTTON_MIN_WIDTH, 0)
                .setMainPanel(SPanel.column(
                        animationSelection,
                        modelSelection
                ))
        );

        animationSelection.addStateChangeListener((i) -> demonstrator.setAnimation(animations[i]));
        modelSelection.addStateChangeListener(i -> demonstrator.setModel(models[i]));


        overworld.get(TilePointer.class).setVisible(false);
        overworld.get(RenderLoop.class).renderSequence(null)
                .add(gl -> {
                    gl.translate(-1, -1, 0);
                    Toolbox.drawAxisFrame(gl);
                    gl.translate(1, 1, 0);
//                    gl.render(GenericShapes.QUAD, null);
                });

        overworld.get(GameState.class).addEntity(demonstrator);
        overworld.get(Camera.class).set(Vectors.O, new Vector3f(4, 4, 4));

        modLoader.startGame();
    }

    private void showNewGamePanel() {
        SFrame newGameFrame = new NewGameFrame(overworld, modLoader);
        newGameFrame.setVisible(true);
        overworld.get(HUDManager.class).addElement(newGameFrame);
    }

    private static class AStartTestMouseTool extends DefaultMouseTool {
        Vector2i first;

        public AStartTestMouseTool(Game game) {
            super(game);
        }

        @Override
        public void apply(Vector3fc position, int xSc, int ySc) {
            Vector2i second = game.get(GameMap.class).getCoordinate(position);

            if (first == null) {
                this.first = second;
                game.get(GameMap.class).setHighlights(first);

            } else {
                Collection<Vector2i> path = game.get(GameMap.class).findPath(first, second, 1, 0.1f);
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
