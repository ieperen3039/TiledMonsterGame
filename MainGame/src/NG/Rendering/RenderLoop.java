package NG.Rendering;

import NG.Camera.Camera;
import NG.Camera.StaticCamera;
import NG.Core.AbstractGameLoop;
import NG.Core.Game;
import NG.Core.GameAspect;
import NG.Core.GameTimer;
import NG.DataStructures.Generic.Color4f;
import NG.GUIMenu.Rendering.NVGOverlay;
import NG.InputHandling.KeyMouseCallbacks;
import NG.Rendering.Lights.GameLights;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MatrixStack.SceneShaderGL;
import NG.Rendering.Shaders.PhongShader;
import NG.Rendering.Shaders.SceneShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Rendering.Shaders.TextureShader;
import NG.Rendering.Shapes.GenericShapes;
import NG.Rendering.Textures.Texture;
import NG.Settings.Settings;
import NG.Tools.*;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

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
    private final NVGOverlay overlay;
    private Game game;
    private Map<ShaderProgram, RenderBundle> renders;
    private SceneShader uiShader;

    private TimeObserver timeObserver;

    /**
     * creates a new, paused gameloop
     * @param targetFPS the target frames per second
     */
    public RenderLoop(int targetFPS) {
        super("Renderloop", targetFPS);
        overlay = new NVGOverlay();
        renders = new HashMap<>();

        timeObserver = new TimeObserver((targetFPS / 4) + 1, true);
    }

    public void init(Game game) throws IOException {
        if (this.game != null) return;
        this.game = game;

        overlay.init(game.get(Settings.class).ANTIALIAS_LEVEL);
        overlay.addHudItem((hud) -> {
            if (game.get(Settings.class).DEBUG_SCREEN) {
                Logger.putOnlinePrint(hud::printRoll);
            }
        });

        uiShader = new PhongShader();

        game.get(KeyMouseCallbacks.class).addKeyPressListener(k -> {
            if (k == GLFW.GLFW_KEY_PERIOD) {
                Logger.DEBUG.print("\n" + timeObserver.resultsTable());
            }
        });
    }

    /**
     * generates a new render bundle, which allows adding rendering actions which are executed in order on the given
     * shader. There is no guarantee on execution order between shaders
     * @param shader the shader used, or null to use a basic Phong shading
     * @return a bundle that allows adding rendering options.
     */
    public RenderBundle renderSequence(ShaderProgram shader) {
        return renders.computeIfAbsent(shader == null ? uiShader : shader, RenderBundle::new);
    }

    @Override
    protected void update(float deltaTime) {
        Toolbox.checkGLError("Pre-loop");
        timeObserver.startNewLoop();

        // current time
        game.get(GameTimer.class).updateRenderTime();

        // camera
        game.get(Camera.class).updatePosition(deltaTime); // real-time deltatime

        timeObserver.startTiming("ShadowMaps");
        GameLights lights = game.get(GameLights.class);
        lights.renderShadowMaps();
        timeObserver.endTiming("ShadowMaps");

        GLFWWindow window = game.get(GLFWWindow.class);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());
        glEnable(GL_LINE_SMOOTH);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        Toolbox.checkGLError(window.toString());

        for (RenderBundle renderBundle : renders.values()) {
            String identifier = renderBundle.shader.getClass().getSimpleName();
            timeObserver.startTiming(identifier);
            renderBundle.draw();
            timeObserver.endTiming(identifier);
            Toolbox.checkGLError(identifier);
        }

        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();
        timeObserver.startTiming("GUI");
        overlay.draw(windowWidth, windowHeight, 10, 10, 16);
        timeObserver.endTiming("GUI");
        Toolbox.checkGLError(overlay.toString());

        // update window
        window.update();

        // loop clean
        Toolbox.checkGLError("Render loop");
        if (window.shouldClose()) stopLoop();

        timeObserver.startTiming("Loop Overhead");
    }

    public void addHudItem(Consumer<NVGOverlay.Painter> draw) {
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

            SGL tgl = new SceneShaderGL(uiShader, texture.getWidth(), texture.getHeight(), viewpoint);

            uiShader.setPointLight(Vectors.Z, Color4f.WHITE, 0.8f);
            ((TextureShader) uiShader).setTexture(texture);
            tgl.render(GenericShapes.TEXTURED_QUAD, null);
            ((TextureShader) uiShader).unsetTexture();

        }
        uiShader.unbind();
        window.printScreen(Directory.screenshots, fileName, GL_BACK);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public SceneShader getUIShader() {
        return uiShader;
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

                    assert gl.getPosition(new Vector3f(1, 1, 1))
                            .equals(new Vector3f(1, 1, 1));
                }
            }
            shader.unbind();
        }
    }
}
