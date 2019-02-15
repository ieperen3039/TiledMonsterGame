package NG.Rendering;

import NG.Camera.Camera;
import NG.Camera.StaticCamera;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.AbstractGameLoop;
import NG.Engine.Game;
import NG.Engine.GameAspect;
import NG.GameMap.GameMap;
import NG.GameState.GameLights;
import NG.GameState.GameState;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MatrixStack.SceneShaderGL;
import NG.Rendering.Shaders.AdvancedSceneShader;
import NG.Rendering.Shaders.SceneShader;
import NG.Rendering.Shaders.TextureShader;
import NG.Rendering.Shapes.FileShapes;
import NG.Rendering.Textures.Texture;
import NG.ScreenOverlay.ScreenOverlay;
import NG.Settings.Settings;
import NG.Tools.Directory;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Vector3f;

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

    /**
     * creates a new, paused gameloop
     * @param targetTps the target frames per second
     */
    public RenderLoop(int targetTps) {
        super("Renderloop", 20);
        overlay = new ScreenOverlay();
    }

    public void init(Game game) throws IOException {
        this.game = game;
        overlay.init(game.settings().ANTIALIAS_LEVEL);
        overlay.addHudItem((hud) -> {
            if (game.settings().DEBUG_SCREEN) {
                Logger.setOnlineOutput(hud::printRoll);
            }
        });

        sceneShader = new AdvancedSceneShader();
    }

    @Override
    protected void update(float deltaTime) {

        // current time
        game.timer().updateRenderTime();

        // camera
        game.camera().updatePosition(deltaTime); // real-time deltatime

        GameMap world = game.map();
        GameState entities = game.state();
        GameLights lights = game.lights();

        GLFWWindow window = game.window();
        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();
        Settings settings = game.settings();
        boolean doIsometric = settings.ISOMETRIC_VIEW;

        lights.renderShadowMaps();

        // scene shader
        sceneShader.bind();
        {
            sceneShader.initialize(game);

            // GL object
            SGL gl = new SceneShaderGL(sceneShader, windowWidth, windowHeight, game.camera(), doIsometric);
            Toolbox.drawAxisFrame(gl);

            // order is important
            lights.draw(gl);
            world.draw(gl);
            entities.draw(gl);
        }
        sceneShader.unbind();

        overlay.draw(windowWidth, windowHeight, 10, Settings.TOOL_BAR_HEIGHT + 10, 16);

        // update window
        window.update();

        // loop clean
        Toolbox.checkGLError();
        if (window.shouldClose()) stopLoop();
    }

    private void dumpTexture(Texture texture, String fileName) {
        assert (sceneShader instanceof TextureShader);
        GLFWWindow window = game.window();

        sceneShader.bind();
        {
            sceneShader.initialize(game);
            Camera viewpoint = new StaticCamera(new Vector3f(0, 0, 2), Vectors.newZeroVector(), Vectors.newXVector());

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

    @Override
    public void cleanup() {
        sceneShader.cleanup();
        overlay.cleanup();
    }

    public void addHudItem(Consumer<ScreenOverlay.Painter> draw) {
        overlay.addHudItem(draw);
    }
}
