package NG.Rendering;

import NG.Camera.Camera;
import NG.Camera.StaticCamera;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.AbstractGameLoop;
import NG.Engine.Game;
import NG.Engine.GameAspect;
import NG.GameMap.GameMap;
import NG.GameState.GameLights;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MatrixStack.SceneShaderGL;
import NG.Rendering.Shaders.*;
import NG.Rendering.Shapes.FileShapes;
import NG.Rendering.Textures.Texture;
import NG.ScreenOverlay.ScreenOverlay;
import NG.Settings.Settings;
import NG.Tools.Directory;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

import java.io.IOException;
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
    private BigArrow bigArrow;
    private boolean cursorIsVisible = false;

    /**
     * creates a new, paused gameloop
     * @param targetTps the target frames per second
     */
    public RenderLoop(int targetTps) {
        super("Renderloop", targetTps);
        overlay = new ScreenOverlay();
    }

    public void init(Game game) throws IOException {
        this.game = game;
        Settings settings = game.settings();

        overlay.init(settings.ANTIALIAS_LEVEL);
        overlay.addHudItem((hud) -> {
            if (settings.DEBUG_SCREEN) {
                Logger.setOnlineOutput(hud::printRoll);
            }
        });

        sceneShader = new BlinnPhongShader();
        worldShader = new WorldBPShader();

        bigArrow = new BigArrow(new Vector3f());
        game.inputHandling().addMousePositionListener(this::updateArrow);
    }

    private void updateArrow(int xPos, int yPos) {
        if (game.inputHandling().mouseIsOnMap()) {
            Vector3f origin = new Vector3f();
            Vector3f direction = new Vector3f();

            int correctedY = game.window().getHeight() - yPos;
            Vectors.windowCoordToRay(game, xPos, correctedY, origin, direction);

            GameMap map = game.map();
            Vector3f position = map.intersectWithRay(origin, direction);
            Vector3i coordinate = map.getCoordinate(position);
            position.set(map.getPosition(coordinate.x, coordinate.y));

            bigArrow.setPosition(position);

            if (cursorIsVisible) {
                game.window().hideCursor(false);
                cursorIsVisible = false;
            }

        } else {
            if (!cursorIsVisible) {
                game.window().showCursor();
                cursorIsVisible = true;
            }
        }
    }

    @Override
    protected void update(float deltaTime) {
        // current time
        game.timer().updateRenderTime();

        // camera
        game.camera().updatePosition(deltaTime); // real-time deltatime

        GameMap world = game.map();
        GameLights lights = game.lights();

        GLFWWindow window = game.window();
        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();

        lights.renderShadowMaps();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderWith(sceneShader, this::drawEntities, lights, windowWidth, windowHeight);
        renderWith(worldShader, world::draw, lights, windowWidth, windowHeight);

        overlay.draw(windowWidth, windowHeight, 10, Settings.TOOL_BAR_HEIGHT + 10, 16);

        // update window
        window.update();

        // loop clean
        Toolbox.checkGLError();
        if (window.shouldClose()) stopLoop();
    }

    private void drawEntities(SGL gl) {
        game.entities().draw(gl);
        bigArrow.draw(gl);
    }

    private void renderWith(
            SceneShader sceneShader, Consumer<SGL> draw, GameLights lights, int windowWidth, int windowHeight
    ) {
        boolean doIsometric = game.settings().ISOMETRIC_VIEW;

        sceneShader.bind();
        {
            sceneShader.initialize(game);

            // GL object
            SGL gl = new SceneShaderGL(sceneShader, windowWidth, windowHeight, game.camera(), doIsometric);

            // order is important
            lights.draw(gl);
            draw.accept(gl);
        }
        sceneShader.unbind();
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
        GLFWWindow window = game.window();

        sceneShader.bind();
        {
            sceneShader.initialize(game);
            Camera viewpoint = new StaticCamera(new Vector3f(0, 0, 3), Vectors.newZeroVector(), Vectors.newXVector());

            SGL tgl = new SceneShaderGL(sceneShader, texture.getWidth(), texture.getHeight(), viewpoint, true);

            sceneShader.setPointLight(Vectors.Z, Color4f.WHITE, 0.8f);
            ((TextureShader) sceneShader).setTexture(texture);
            tgl.render(FileShapes.TEXTURED_QUAD, null);
            ((TextureShader) sceneShader).unsetTexture();

        }
        sceneShader.unbind();
        window.printScreen(Directory.screenshots, fileName, GL_BACK);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    private class BigArrow {
        private static final float SIZE = 2;
        private Vector3f position;

        private BigArrow(Vector3f position) {
            this.position = position;
        }

        public void draw(SGL gl) {
            gl.pushMatrix();
            {
                gl.translate(position);
                Toolbox.draw3DPointer(gl);

                gl.translate(0, 0, 2);
                gl.scale(SIZE, SIZE, -SIZE);

                if (gl.getShader() instanceof MaterialShader) {
                    MaterialShader mShader = (MaterialShader) gl.getShader();
                    mShader.setMaterial(Material.ROUGH, Color4f.WHITE);
                }

                gl.render(FileShapes.ARROW, null);
            }
            gl.popMatrix();
        }

        public void setPosition(Vector3fc targetPosition) {
            position.set(targetPosition);
        }
    }
}
