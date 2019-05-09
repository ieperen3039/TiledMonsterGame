package NG.GameState;

import NG.Camera.Camera;
import NG.Camera.TycoonFixedCamera;
import NG.Engine.Game;
import NG.Engine.GameService;
import NG.Engine.Version;
import NG.Entities.Entity;
import NG.GUIMenu.Frames.Components.SComponent;
import NG.GUIMenu.Frames.GUIManager;
import NG.GameMap.AbstractMap;
import NG.GameMap.EmptyMap;
import NG.GameMap.GameMap;
import NG.InputHandling.MouseToolCallbacks;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Rendering.GLFWWindow;
import NG.Rendering.Lights.GameState;
import NG.Settings.Settings;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Geert van Ieperen. Created on 2-12-2018.
 */
public class RayTraceTest {
    private GameMap instance;
    private Game game;

    @Before
    public void setUp() throws Exception {
        Settings settings = new Settings();
        GLFWWindow window = new GLFWWindow(Settings.GAME_NAME, new GLFWWindow.Settings(settings), true);
        MouseToolCallbacks inputHandler = new MouseToolCallbacks();

        Camera cam = new TycoonFixedCamera(new Vector3f(), 10, 10);
        GameMap map = new EmptyMap();
        game = new GameService(new Version(0, 0), "", map, cam, window, inputHandler, new Settings());

        cam.init(game);
        instance = game.get(AbstractMap.class);
        instance.init(game);
        cam.set(new Vector3f(0, 0, 0), new Vector3f(-10, -10, 10));
    }

    @Test
    public void screenTestIso() {
        game.get(Settings.class).ISOMETRIC_VIEW = true;
        int width = game.get(GLFWWindow.class).getWidth();
        int height = game.get(GLFWWindow.class).getHeight();

        // middle of screen must be focus
        instance.checkMouseClick(new TestTool() {
            @Override
            public void apply(Vector3fc position) {
                Logger.DEBUG.print(position);
                assertTrue(position.distance(new Vector3f(0, 0, 0)) < 1f);
            }
        }, width / 2, height / 2);
    }

    @Test
    public void screenTestPerspec() {
        game.get(Settings.class).ISOMETRIC_VIEW = false;
        int width = game.get(GLFWWindow.class).getWidth();
        int height = game.get(GLFWWindow.class).getHeight();

        // middle of screen must be focus
        instance.checkMouseClick(new TestTool() {
            @Override
            public void apply(Vector3fc position) {
                Logger.DEBUG.print(position);
                assertTrue(position.distance(new Vector3f(0, 0, 0)) < 1f);
            }
        }, width / 2, height / 2);
    }

    @Test
    public void rayIsoTestZero() {
        testCoord(new Vector3f(0, 0, 0), true);
    }

    @Test
    public void rayIsoTest2() {
        testCoord(new Vector3f(3, 4, 0), true);
    }

    @Test
    public void rayIsoTest3() {
        testCoord(new Vector3f(100, 20, 0), true);
    }

    @Test
    public void rayPerspecTestZero() {
        testCoord(new Vector3f(0, 0, 0), false);
    }

    @Test
    public void rayPerspecTest2() {
        testCoord(new Vector3f(3, 4, 0), false);
    }

    @Test
    public void rayPerspecTest3() {
        testCoord(new Vector3f(100, 20, 0), false);
    }

    /** tests whether the given coordinate on the given isometric setting can be transformed one way and back */
    private void testCoord(final Vector3fc original, boolean isometricView) {
        game.get(Settings.class).ISOMETRIC_VIEW = isometricView;

        int width = game.get(GLFWWindow.class).getWidth();
        int height = game.get(GLFWWindow.class).getHeight();

        Matrix4f proj = game.get(Camera.class).getViewProjection(width / height);

        int[] viewport = {0, 0, width, height};
        Vector3f screen = proj.project(original, viewport, new Vector3f());

        // test whether this screen position results in almost the right coordinate
        instance.checkMouseClick(new TestTool() {
            @Override
            public void apply(Vector3fc position) {
                Logger.DEBUG.print(original, screen, position);
                assertTrue(position.distance(new Vector3f(original.x(), original.y(), 0f)) < 1f);
            }
        }, (int) screen.x, (int) screen.y);
    }

    private class TestTool implements MouseTool {

        @Override
        public void apply(SComponent component, int xSc, int ySc) {
            Logger.DEBUG.print(component, xSc, ySc);
        }

        @Override
        public void apply(Entity entity, int xSc, int ySc) {
            Logger.DEBUG.print(entity);
        }

        @Override
        public void apply(Vector3fc position) {
            Logger.DEBUG.print(Vectors.toString(position));
        }

        @Override
        public void mouseMoved(int xDelta, int yDelta) {
            Logger.DEBUG.print(xDelta, yDelta);
        }

        @Override
        public void onRelease(int button, int xSc, int ySc) {
            Logger.DEBUG.print(button, xSc, ySc);
        }

        @Override
        public void onClick(int button, int x, int y) {
            if (game.get(GUIManager.class).checkMouseClick(this, x, y)) return;

            // invert y for transforming to model space (inconsistency between OpenGL and GLFW)
            y = game.get(GLFWWindow.class).getHeight() - y;

            if (game.get(GameState.class).checkMouseClick(this, x, y)) return;
            game.get(GameMap.class).checkMouseClick(this, x, y);
        }

        @Override
        public void onScroll(float value) {
            game.get(Camera.class).onScroll(value);
            Logger.DEBUG.print(value);
        }
    }
}
