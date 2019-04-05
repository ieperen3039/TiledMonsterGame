package NG.Rendering;

import NG.Camera.Camera;
import NG.Camera.StaticCamera;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.AbstractGameLoop;
import NG.Engine.Game;
import NG.Engine.GameAspect;
import NG.Engine.GameTimer;
import NG.GUIMenu.ScreenOverlay;
import NG.GameMap.ClaimRegistry;
import NG.GameMap.GameMap;
import NG.GameState.GameLights;
import NG.GameState.GameState;
import NG.InputHandling.KeyMouseCallbacks;
import NG.Particles.GameParticles;
import NG.Particles.ParticleShader;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MatrixStack.SceneShaderGL;
import NG.Rendering.Shaders.*;
import NG.Rendering.Shapes.GenericShapes;
import NG.Rendering.Textures.Texture;
import NG.Settings.Settings;
import NG.Tools.Directory;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Repeatedly renders a frame of the main camera of the game given by {@link #init(Game)}
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class RenderLoop extends AbstractGameLoop implements GameAspect {
    private final ScreenOverlay overlay;
    private Game game;
    private SceneShader sceneShader;
    private SceneShader worldShader;
    private ParticleShader particleShader;
    private Pointer pointer;
    private boolean cursorIsVisible = false;

    /**
     * creates a new, paused gameloop
     * @param targetFPS the target frames per second
     */
    public RenderLoop(int targetFPS) {
        super("Renderloop", targetFPS);
        overlay = new ScreenOverlay();

        pointer = new Pointer(new Vector3f());
    }

    public void init(Game game) throws IOException {
        if (this.game != null) return;
        this.game = game;
        Settings settings = game.get(Settings.class);

        overlay.init(settings.ANTIALIAS_LEVEL);
        overlay.addHudItem((hud) -> {
            if (settings.DEBUG_SCREEN) {
                Logger.putOnlinePrint(hud::printRoll);
            }
        });

        sceneShader = new BlinnPhongShader();
        worldShader = new WorldBPShader();
        particleShader = new ParticleShader();
        game.get(KeyMouseCallbacks.class).addMousePositionListener(this::updateArrow);
    }

    private void updateArrow(int xPos, int yPos) {
        if (game.get(KeyMouseCallbacks.class).mouseIsOnMap()) {
            Vector3f origin = new Vector3f();
            Vector3f direction = new Vector3f();

            int correctedY = game.get(GLFWWindow.class).getHeight() - yPos;
            Vectors.windowCoordToRay(game, xPos, correctedY, origin, direction);

            GameMap map = game.get(GameMap.class);
            Vector3f position = map.intersectWithRay(origin, direction);
            Vector3i coordinate = map.getCoordinate(position);
            position.set(map.getPosition(coordinate.x, coordinate.y));

            pointer.setPosition(position);

            if (cursorIsVisible) {
                game.get(GLFWWindow.class).hideCursor(false);
                cursorIsVisible = false;
            }

        } else {
            if (!cursorIsVisible) {
                game.get(GLFWWindow.class).showCursor();
                cursorIsVisible = true;
            }
        }
    }

    @Override
    protected void update(float deltaTime) {
        // current time
        game.get(GameTimer.class).updateRenderTime();

        // camera
        game.get(Camera.class).updatePosition(deltaTime); // real-time deltatime

        GameMap world = game.get(GameMap.class);
        GameLights lights = game.get(GameLights.class);
        GameParticles particles = game.get(GameParticles.class);
        GLFWWindow window = game.get(GLFWWindow.class);

        lights.renderShadowMaps();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderWith(worldShader, world::draw, true);
        renderWith(sceneShader, this::drawEntities, true);

        if (game.get(Settings.class).RENDER_CLAIMED_TILES) {
            renderWith(sceneShader, gl -> drawClaimedTiles(gl, world), false);
        }

        renderWith(particleShader, particles::draw, false);

        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();
        overlay.draw(windowWidth, windowHeight, 10, Settings.TOOL_BAR_HEIGHT + 10, 16);

        // update window
        window.update();

        // loop clean
        Toolbox.checkGLError();
        if (window.shouldClose()) stopLoop();
    }

    private void drawClaimedTiles(SGL gl, GameMap world) {
        Collection<Vector2ic> claims = game.get(ClaimRegistry.class).getClaimedTiles();

        for (Vector2ic c : claims) {
            Vector3f tr = world.getPosition(c);
            tr.add(0, 0, 0.01f);

            gl.translate(tr);
            gl.render(GenericShapes.QUAD, null);
            gl.translate(tr.negate());
        }
    }

    private void drawEntities(SGL gl) {
        game.get(GameState.class).draw(gl);
        pointer.draw(gl);
    }

    private void renderWith(ShaderProgram shader, Consumer<SGL> draw, boolean addLights) {
        shader.bind();
        {
            shader.initialize(game);

            // GL object
            SGL gl = shader.getGL(game);

            if (addLights) {
                game.get(GameLights.class).draw(gl);
            }

            draw.accept(gl);
        }
        shader.unbind();
    }

    public void addHudItem(Consumer<ScreenOverlay.Painter> draw) {
        overlay.addHudItem(draw);
    }

    @Override
    public void cleanup() {
        sceneShader.cleanup();
        overlay.cleanup();
    }

    private void dumpTexture(Texture texture, String fileName) {
        assert (sceneShader instanceof TextureShader);
        GLFWWindow window = game.get(GLFWWindow.class);

        sceneShader.bind();
        {
            sceneShader.initialize(game);
            Camera viewpoint = new StaticCamera(new Vector3f(0, 0, 3), Vectors.newZeroVector(), Vectors.newXVector());

            SGL tgl = new SceneShaderGL(sceneShader, texture.getWidth(), texture.getHeight(), viewpoint, true);

            sceneShader.setPointLight(Vectors.Z, Color4f.WHITE, 0.8f);
            ((TextureShader) sceneShader).setTexture(texture);
            tgl.render(GenericShapes.TEXTURED_QUAD, null);
            ((TextureShader) sceneShader).unsetTexture();

        }
        sceneShader.unbind();
        window.printScreen(Directory.screenshots, fileName, GL_BACK);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    private class Pointer {
        private static final float SIZE = 2;
        private Vector3f position;

        private Pointer(Vector3f position) {
            this.position = position;
        }

        public void draw(SGL gl) {
            gl.pushMatrix();
            {
                gl.translate(position);
                Toolbox.draw3DPointer(gl);

                gl.translate(0, 0, 4);
                gl.scale(SIZE, SIZE, -SIZE);

                if (gl.getShader() instanceof MaterialShader) {
                    MaterialShader mShader = (MaterialShader) gl.getShader();
                    mShader.setMaterial(Material.ROUGH, Color4f.WHITE);
                }

                gl.render(GenericShapes.ARROW, null);
            }
            gl.popMatrix();
        }

        public void setPosition(Vector3fc targetPosition) {
            position.set(targetPosition);
        }
    }
}
