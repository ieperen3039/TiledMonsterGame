package NG.Rendering;

import NG.Camera.Camera;
import NG.Camera.StaticCamera;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.AbstractGameLoop;
import NG.Engine.Game;
import NG.Engine.GameAspect;
import NG.Engine.GameTimer;
import NG.GUIMenu.ScreenOverlay;
import NG.GameMap.GameMap;
import NG.GameState.GameLights;
import NG.InputHandling.KeyMouseCallbacks;
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
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Repeatedly renders a frame of the main camera of the game given by {@link #init(Game)}
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class RenderLoop extends AbstractGameLoop implements GameAspect {
    private final ScreenOverlay overlay;
    private Game game;
    private Map<ShaderProgram, RenderBundle> renders;
    private Pointer pointer;
    private boolean cursorIsVisible = false;
    private SceneShader uiShader;

    /**
     * creates a new, paused gameloop
     * @param targetFPS the target frames per second
     */
    public RenderLoop(int targetFPS) {
        super("Renderloop", targetFPS);
        overlay = new ScreenOverlay();
        renders = new HashMap<>();

        pointer = new Pointer();
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

        uiShader = new PhongShader();
        getRenderSequence(uiShader)
                .add(game.get(GameLights.class)::draw)
                .add(pointer::draw);

        game.get(KeyMouseCallbacks.class).addMousePositionListener(this::updateArrow);
    }

    /**
     * generates a new render bundle, which allows adding rendering actions which are executed in order on the given
     * shader. There is no guarantee on execution order between shaders
     * @param shader the shader used, or null to use a basic Phong shading
     * @return a bundle that allows adding rendering options.
     */
    public RenderBundle getRenderSequence(ShaderProgram shader) {
        return renders.computeIfAbsent(shader == null ? uiShader : shader, RenderBundle::new);
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
            Vector3f midSquare = map.getPosition(coordinate.x, coordinate.y);

            pointer.setPosition(position, midSquare);

            if (cursorIsVisible && game.get(Settings.class).HIDE_CURSOR_ON_MAP) {
                game.get(GLFWWindow.class).setCursorMode(CursorMode.HIDDEN_FREE);
                cursorIsVisible = false;
            }

        } else {
            if (!cursorIsVisible) {
                game.get(GLFWWindow.class).setCursorMode(CursorMode.VISIBLE);
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

        GameLights lights = game.get(GameLights.class);
        lights.renderShadowMaps();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renders.values().forEach(RenderBundle::draw);

        GLFWWindow window = game.get(GLFWWindow.class);
        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();
        overlay.draw(windowWidth, windowHeight, 10, Settings.TOOL_BAR_HEIGHT + 10, 16);

        // update window
        window.update();

        // loop clean
        Toolbox.checkGLError();
        if (window.shouldClose()) stopLoop();
    }

    public void addHudItem(Consumer<ScreenOverlay.Painter> draw) {
        overlay.addHudItem(draw);
    }

    @Override
    public void cleanup() {
        uiShader.cleanup();
        overlay.cleanup();
    }

    private void dumpTexture(Texture texture, String fileName) {
        assert (uiShader instanceof TextureShader);
        GLFWWindow window = game.get(GLFWWindow.class);

        uiShader.bind();
        {
            uiShader.initialize(game);
            Camera viewpoint = new StaticCamera(new Vector3f(0, 0, 3), Vectors.newZeroVector(), Vectors.newXVector());

            SGL tgl = new SceneShaderGL(uiShader, texture.getWidth(), texture.getHeight(), viewpoint, true);

            uiShader.setPointLight(Vectors.Z, Color4f.WHITE, 0.8f);
            ((TextureShader) uiShader).setTexture(texture);
            tgl.render(GenericShapes.TEXTURED_QUAD, null);
            ((TextureShader) uiShader).unsetTexture();

        }
        uiShader.unbind();
        window.printScreen(Directory.screenshots, fileName, GL_BACK);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    private class Pointer {
        private static final float SIZE = 2;
        private Vector3f midSquare;
        private Vector3f exact;
        private Vector3f midSquareNegate;

        private Pointer() {
            this.midSquare = new Vector3f();
            this.exact = new Vector3f();
            this.midSquareNegate = new Vector3f();
        }

        public void draw(SGL gl) {
            gl.pushMatrix();
            {
                gl.translate(midSquare);
                Toolbox.draw3DPointer(gl);
                gl.translate(midSquareNegate);

                gl.translate(exact);
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

        public void setPosition(Vector3fc position, Vector3fc midSquare) {
            this.midSquare.set(midSquare);
            this.midSquare.negate(midSquareNegate);
            exact.set(position.x(), position.y(), midSquare.z());
        }
    }

    public class RenderBundle {
        private ShaderProgram shader;
        private List<Consumer<SGL>> targets;

        public RenderBundle(ShaderProgram shader) {
            this.shader = shader;
            this.targets = new ArrayList<>();
        }

        /**
         * appends the given consumer to the end of the render sequence
         * @return this
         */
        public RenderBundle add(Consumer<SGL> drawable) {
            targets.add(drawable);
            return this;
        }

        /**
         * executes the given drawables in order
         */
        public void draw() {
            shader.bind();
            {
                shader.initialize(game);

                // GL object
                SGL gl = shader.getGL(game);

                for (Consumer<SGL> tgt : targets) {
                    tgt.accept(gl);
                }
            }
            shader.unbind();
        }
    }
}
