package NG.Rendering;

import NG.DataStructures.Generic.Color4f;
import NG.Tools.Directory;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * @author Jorren Hendriks & Geert van Ieperen
 * <p>
 * A window which initializes GLFW and manages it.
 */
public class GLFWWindow {
    private final String title;
    private final boolean resizable;
    // buffers for mouse input
    private final DoubleBuffer mousePosX;
    private final DoubleBuffer mousePosY;
    private final Settings settings;

    private long primaryMonitor;
    private long window;
    private int width;
    private int height;
    private boolean fullScreen = false;
    private boolean mouseIsCaptured;
    private List<ResizeListener> sizeChangeListeners = new ArrayList<>();
    private Thread glContext;

    public GLFWWindow(String title, Settings settings, boolean resizable) {
        this.title = title;
        this.resizable = resizable;
        this.settings = settings;

        this.mousePosX = BufferUtils.createDoubleBuffer(1);
        this.mousePosY = BufferUtils.createDoubleBuffer(1);

        // Setup error callback, print to Logger.ERROR
        GLFWErrorCallback.createPrint(Logger.ERROR.getPrintStream()).set();

        // Initialize GLFW
        if (!glfwInit()) {
            throw new RuntimeException("Unable to initialize GLFW");
        }

        // Configure window
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, resizable ? GL_TRUE : GL_FALSE);
        // Set OpenGL version
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        if (settings.antialiasLevel > 0) {
            glfwWindowHint(GLFW_STENCIL_BITS, settings.antialiasLevel);
            glfwWindowHint(GLFW_SAMPLES, settings.antialiasLevel);
        }

        window = getWindow(settings.windowWidth, settings.windowHeight);
        primaryMonitor = glfwGetPrimaryMonitor();

        if (settings.fullscreen) {
            setFullScreen(settings);
        } else {
            setWindowed(settings);
        }

        if (settings.vSync) {
            // Turn on vSync
            glfwSwapInterval(1);
        }

        GL.createCapabilities();
        glContext = Thread.currentThread();

        // debug message callbacks
        if (settings.debugMode && settings.glDebugMessages) {
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
            GLUtil.setupDebugMessageCallback();
        }

        // Set clear color to black
        glClearColor(0f, 0f, 0f, 0f); // black
//        glClearColor(1f, 1f, 1f, 0f); // white

        // Enable Depth Test
        glEnable(GL_DEPTH_TEST);
        // Enable Stencil Test
        glEnable(GL_STENCIL_TEST);
        // Support transparencies
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        if (settings.cullFace) {
            // face culling
            glEnable(GL_CULL_FACE);
            glCullFace(GL_FRONT);
        }

        // set polygons to fill
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        Toolbox.checkGLError();
    }

    /**
     * creates a window of the given width and height
     * @param width  in pixels
     * @param height in pixels
     */
    private long getWindow(int width, int height) {
        // Create window
        long newWindow = glfwCreateWindow(width, height, title, NULL, NULL);
        if (newWindow == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        this.width = width;
        this.height = height;

//        glfwSetWindowIcon(newWindow, null); // TODO icon

        if (this.resizable) {
            // Setup resize callback
            glfwSetFramebufferSizeCallback(newWindow, (window, newWidth, newHeight) -> {
                this.width = newWidth;
                this.height = newHeight;
                sizeChangeListeners.forEach(l -> l.onChange(newWidth, newHeight));
            });
        }

        // Make GL context current
        glfwMakeContextCurrent(newWindow);
        return newWindow;
    }

    /**
     * update the {@link GLFWWindow}. This will deal with basic OpenGL formalities. Besides it will also poll for events
     * which occurred on the window. Finally returns whether the window should close.
     */
    public void update() {
        // Swap buffers
        glfwSwapBuffers(window);

        // Clear framebuffer
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        // Poll for events
        glfwPollEvents();
    }

    /**
     * saves a copy of the front buffer (the display) to disc
     * @param dir          directory to store the image to
     * @param filename     the file to save to
     * @param bufferToRead the GL buffer to read, usually one of {@link GL11#GL_FRONT} or {@link GL11#GL_BACK}
     */
    public void printScreen(Directory dir, String filename, int bufferToRead) {
        glReadBuffer(bufferToRead);
        int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
        glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        new Thread(() -> {
            String format = "png";
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int i = (x + (width * y)) * bpp;
                    int r = buffer.get(i) & 0xFF;
                    int g = buffer.get(i + 1) & 0xFF;
                    int b = buffer.get(i + 2) & 0xFF;
                    image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
                }
            }

            try {
                File file = dir.getFile(filename + "." + format); // The file to save to.
                if (file.exists()) {
                    Files.delete(file.toPath());
                } else {
                    boolean success = file.mkdirs();
                    if (!success) {
                        Logger.ERROR.print("Could not create directories", file);
                        return;
                    }
                }
                ImageIO.write(image, format, file);

            } catch (IOException e) {
                Logger.ERROR.print(e);
            }
        }, "Writing frame to disc").start();

    }

    /**
     * hints the window to close
     */
    public void close() {
        glfwSetWindowShouldClose(window, true);
    }

    public void open() {
        // Show window
        glfwShowWindow(window);
        glfwFocusWindow(window);
    }

    /**
     * Terminate GLFW and release GLFW error callback
     */
    public void cleanup() {
        sizeChangeListeners.clear();
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }


    /**
     * Set the color which is used for clearing the window.
     * @param red   The red value (0.0 - 1.0)
     * @param green The green value (0.0 - 1.0)
     * @param blue  The blue value (0.0 - 1.0)
     * @param alpha The alpha value (0.0 - 1.0)
     */
    public void setClearColor(float red, float green, float blue, float alpha) {
        glClearColor(red, green, blue, alpha);
    }

    /**
     * Check whether a certain key is pressed.
     * @param keyCode The keycode of the key.
     * @return Whether the key with requested keyCode is pressed.
     */
    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(window, keyCode) == GLFW_PRESS;
    }

    /**
     * Check whether a certain mouse button is pressed.
     * @param button The button of the mouse.
     * @return Whether the requested button is pressed.
     */
    public boolean isMouseButtonPressed(int button) {
        return glfwGetMouseButton(window, button) == GLFW_PRESS;
    }

    /**
     * Get the current position of the mouse.
     * @return the position of the cursor, in screen coordinates, relative to the upper-left corner of the client area
     * of the specified window
     */
    public Vector2i getMousePosition() {
        glfwGetCursorPos(window, mousePosX, mousePosY);
        return new Vector2i((int) mousePosX.get(0), (int) mousePosY.get(0));
    }

    /**
     * Get whether the window should close.
     * @return Whether the window should close.
     */
    public boolean shouldClose() {
        return glfwWindowShouldClose(window);
    }

    /**
     * Get the width of the window.
     * @return The width of the window.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the height of the window.
     * @return The height of the window.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get whether resizing the window is allowed.
     * @return Whether resizing the window is allowed.
     */
    public boolean resizeEnabled() {
        return resizable;
    }

    public void setFullScreen(Settings settings) {
        GLFWVidMode vidmode = glfwGetVideoMode(primaryMonitor);
        glfwSetWindowMonitor(window, primaryMonitor, 0, 0, vidmode.width(), vidmode.height(), settings.targetFPS);

        if (settings.vSync) {
            // Turn on vSync
            glfwSwapInterval(1);
        }

        fullScreen = true;
    }

    public void setWindowed(Settings settings) {
        // Get primary display resolution
        GLFWVidMode vidmode = glfwGetVideoMode(primaryMonitor);
        // Center window on display
        glfwSetWindowPos(
                window,
                (vidmode.width() - settings.windowWidth) / 2,
                (vidmode.height() - settings.windowHeight) / 2
        );
        fullScreen = false;
    }

    public void toggleFullScreen() {
        if (fullScreen) {
            setWindowed(settings);
        } else {
            setFullScreen(settings);
        }
    }

    /** sets the mouse pointer to the given mode */
    public void setCursorMode(CursorMode mode) {
        switch (mode) {
            case VISIBLE:
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                break;
            case HIDDEN_FREE:
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
                break;
            case HIDDEN_CAPTURED:
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                break;
        }
    }

    public void setClearColor(Color4f color4f) {
        setClearColor(color4f.red, color4f.green, color4f.blue, color4f.alpha);
    }

    /**
     * Sets the callbacks to the given listeners. The values that are null are skipped.
     */
    public void setCallbacks(
            GLFWKeyCallbackI key, GLFWMouseButtonCallbackI mousePress, GLFWCursorPosCallbackI mouseMove,
            GLFWScrollCallbackI mouseScroll
    ) {
        if (key != null) glfwSetKeyCallback(window, key);
        if (mousePress != null) glfwSetMouseButtonCallback(window, mousePress);
        if (mouseMove != null) glfwSetCursorPosCallback(window, mouseMove);
        if (mouseScroll != null) glfwSetScrollCallback(window, mouseScroll);
    }

    public void setTextCallback(GLFWCharCallbackI input) {
        glfwSetCharCallback(window, input);
    }

    public void addResizeListener(ResizeListener listener) {
        sizeChangeListeners.add(listener);
    }

    public void setMinimized(boolean doMinimize) {
        if (doMinimize) {
            glfwHideWindow(window);
        } else {
            glfwShowWindow(window);
        }
    }

    public Thread getOpenGLThread() {
        return glContext;
    }

    public static Settings defaultSettings() {
        return new Settings(false, false, 1, false, 800, 600, false, 60, false);
    }

    public interface ResizeListener {
        void onChange(int width, int height);
    }

    public static class Settings {
        final boolean debugMode;
        final boolean glDebugMessages;
        final int antialiasLevel;
        final int windowWidth;
        final int windowHeight;
        final boolean vSync;
        final int targetFPS;
        final boolean fullscreen;
        final boolean cullFace;

        public Settings(NG.Settings.Settings s) {
            this(s.DEBUG, false, s.ANTIALIAS_LEVEL, !s.DEBUG, s.DEFAULT_WINDOW_WIDTH, s.DEFAULT_WINDOW_HEIGHT, s.V_SYNC, s.TARGET_FPS, true);
        }

        public Settings(
                boolean debugMode, boolean glDebugMessages, int antialiasLevel, boolean fullscreen, int windowWidth,
                int windowHeight, boolean vSync, int targetFPS, boolean cullFace
        ) {
            this.debugMode = debugMode;
            this.glDebugMessages = glDebugMessages;
            this.antialiasLevel = antialiasLevel;
            this.fullscreen = fullscreen;
            this.windowWidth = windowWidth;
            this.windowHeight = windowHeight;
            this.vSync = vSync;
            this.targetFPS = targetFPS;
            this.cullFace = cullFace;
        }
    }
}

enum CursorMode {VISIBLE, HIDDEN_FREE, HIDDEN_CAPTURED}
