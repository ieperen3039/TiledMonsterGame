package NG.GameState;

import NG.ActionHandling.MouseTools.MouseTool;
import NG.Engine.Game;
import NG.Entities.Entity;
import NG.GameMap.GameMap;
import NG.Rendering.RenderLoop;
import NG.ScreenOverlay.Frames.Components.SComponent;
import NG.Settings.Settings;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.junit.Before;
import org.junit.Test;
import org.lwjgl.glfw.GLFW;

import static org.junit.Assert.assertTrue;

/**
 * @author Geert van Ieperen. Created on 2-12-2018.
 */
public class RayTraceTest {
    private GameMap instance;
    private Game game;

    @Before
    public void setUp() throws Exception {
        game = new DecoyGame("Testgame", new RenderLoop(60), new Settings());
        game.window().init(game);
        game.camera().init(game);
        instance = game.map();
        instance.init(game);
        game.camera().set(new Vector3f(0, 0, 0), new Vector3f(-10, -10, 10));
    }

    @Test
    public void screenTestIso() {
        game.settings().ISOMETRIC_VIEW = true;
        int width = game.window().getWidth();
        int height = game.window().getHeight();

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
        game.settings().ISOMETRIC_VIEW = false;
        int width = game.window().getWidth();
        int height = game.window().getHeight();

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
        game.settings().ISOMETRIC_VIEW = isometricView;

        int width = game.window().getWidth();
        int height = game.window().getHeight();

        Matrix4f proj = game.camera().getViewProjection(width, height, isometricView);

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

        /**
         * sets the button field. Should only be called by the input handling
         * @param button a button enum, often {@link GLFW#GLFW_MOUSE_BUTTON_LEFT} or {@link
         *               GLFW#GLFW_MOUSE_BUTTON_RIGHT}
         */
        @Override
        public void setButton(int button) {
        }
    }
}
