package NG.ActionHandling;

import NG.ActionHandling.MouseTools.DefaultMouseTool;
import NG.ActionHandling.MouseTools.MouseTool;
import NG.DataStructures.Tracked.TrackedFloat;
import NG.Engine.Game;
import NG.Engine.GameAspect;
import NG.Rendering.GLFWWindow;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.Vector2i;
import org.lwjgl.glfw.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

/**
 * A callback handler specialized on a tycoon-game
 * @author Geert van Ieperen. Created on 18-11-2018.
 */
public class MouseToolCallbacks implements GameAspect, KeyMouseCallbacks {
    private final DefaultMouseTool DEFAULT_MOUSE_TOOL = new DefaultMouseTool();
    private final Collection<KeyPressListener> keyPressListeners = new ArrayList<>();
    private final Collection<KeyReleaseListener> keyReleaseListeners = new ArrayList<>();
    private final Collection<MousePositionListener> mousePositionListeners = new ArrayList<>();

    private final ExecutorService taskScheduler = Executors.newSingleThreadExecutor();

    private Game game;
    private KeyTypeListener keyTypeListener = null;
    private MouseTool currentTool = DEFAULT_MOUSE_TOOL;

    @Override
    public void init(Game game) {
        this.game = game;
        GLFWWindow target = game.window();
        Vector2i mousePosition = target.getMousePosition();
        target.setCallbacks(new KeyPressCallback(), new MouseButtonPressCallback(), new MouseMoveCallback(mousePosition), new MouseScrollCallback());
        target.setTextCallback(new CharTypeCallback());
    }

    @Override
    public void cleanup() {
        keyPressListeners.clear();
        keyReleaseListeners.clear();
        mousePositionListeners.clear();
        keyTypeListener = null;
        taskScheduler.shutdown();
    }

    @Override
    public void setMouseTool(MouseTool tool) {
        currentTool = Objects.requireNonNullElse(tool, DEFAULT_MOUSE_TOOL);
        Logger.DEBUG.print("Set mousetool to " + currentTool);
    }

    @Override
    public MouseTool getMouseTool() {
        return currentTool;
    }

    @Override
    public boolean mouseIsOnMap() {
        GLFWWindow window = game.window();
        Vector2i pos = window.getMousePosition();
        // out of bounds
        if (pos.x < 0 || pos.y < 0 || pos.x > window.getWidth() || pos.y > window.getHeight()) {
            return false;
        }

        return !game.gui().covers(pos.x, pos.y);
    }

    @Override
    public MouseTool getDefaultMouseTool() {
        return DEFAULT_MOUSE_TOOL;
    }

    @Override
    public void addMousePositionListener(MousePositionListener listener) {
        mousePositionListeners.add(listener);
    }

    @Override
    public void addKeyPressListener(KeyPressListener listener) {
        keyPressListeners.add(listener);
    }

    @Override
    public void addKeyReleaseListener(KeyReleaseListener listener) {
        keyReleaseListeners.add(listener);
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean removeListener(Object listener) {
        boolean mp = mousePositionListeners.remove(listener);
        boolean kp = keyPressListeners.remove(listener);
        boolean kr = keyReleaseListeners.remove(listener);

        return mp || kp || kr;
    }

    /**
     * Sets a listener such that all generated key events are forwarded to this listener.
     * @param listener The new listener, or null to uninstall this listener and allow regular key presses.
     */
    public void setKeyTypeListener(KeyTypeListener listener) {
        keyTypeListener = listener;
    }

    private class KeyPressCallback extends GLFWKeyCallback {
        @Override
        public void invoke(long window, int keyCode, int scanCode, int action, int mods) {
            if (keyCode < 0) return;
            if (action == GLFW_PRESS) {
                keyPressListeners.forEach(l -> execute(() -> l.keyPressed(keyCode)));

            } else if (action == GLFW_RELEASE) {
                keyReleaseListeners.forEach(l -> execute(() -> l.keyReleased(keyCode)));
            }
        }
    }

    private void execute(Runnable action) {
        taskScheduler.submit(() -> {
            try {
                action.run();
            } catch (Exception ex) {
                Logger.ERROR.print(ex);
            }
        });
    }

    private class MouseButtonPressCallback extends GLFWMouseButtonCallback {
        @Override
        public void invoke(long windowHandle, int button, int action, int mods) {
            Vector2i pos = game.window().getMousePosition();

            if (action == GLFW_PRESS) {
                execute(() -> {
                    int x = pos.x;
                    int y = pos.y;
                    currentTool.setButton(button);

                    if (game.gui().checkMouseClick(currentTool, x, y)) return;

                    // invert y for transforming to model space (inconsistency between OpenGL and GLFW)
                    y = game.window().getHeight() - y;

                    if (game.state().checkMouseClick(currentTool, x, y)) return;
                    game.map().checkMouseClick(currentTool, x, y);
                });

            } else if (action == GLFW_RELEASE) {
                execute(() -> currentTool.onRelease(button, pos.x, pos.y));
            }
        }
    }

    private class MouseMoveCallback extends GLFWCursorPosCallback {
        private TrackedFloat mouseXPos;
        private TrackedFloat mouseYPos;

        MouseMoveCallback(Vector2i mousePos) {
            mouseXPos = new TrackedFloat((float) mousePos.x);
            mouseYPos = new TrackedFloat((float) mousePos.y);
        }

        @Override
        public void invoke(long window, double xpos, double ypos) {
            mouseXPos.update((float) xpos);
            mouseYPos.update((float) ypos);

            for (MousePositionListener listener : mousePositionListeners) {
                execute(() -> listener.mouseMoved((int) xpos, (int) ypos));
            }

            int xDiff = Toolbox.randomToInt(mouseXPos.difference());
            int yDiff = Toolbox.randomToInt(mouseYPos.difference());
            execute(() -> currentTool.mouseMoved(xDiff, yDiff));
        }
    }

    private class MouseScrollCallback extends GLFWScrollCallback {
        @Override
        public void invoke(long window, double xoffset, double yoffset) {
            // TODO send signal to mousetool
            game.camera().onScroll((float) yoffset);
        }
    }

    private class CharTypeCallback extends GLFWCharCallback {
        @Override
        public void invoke(long window, int codepoint) {
            if (keyTypeListener != null && Character.isAlphabetic(codepoint)) {
                char s = Character.toChars(codepoint)[0];

                execute(() -> keyTypeListener.keyTyped(s));
            }
        }
    }
}
