package NG.GUIMenu;

import NG.DataStructures.Generic.Color4f;
import NG.Rendering.Textures.Texture;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.*;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * @author Jorren & Geert
 */
public final class ScreenOverlay {
    private long vg;
    private NVGColor nvgColorBuffer;
    private NVGPaint paint;

    /** fontbuffer MUST be a field */
    @SuppressWarnings("FieldCanBeLocal")
    private final ByteBuffer[] fontBuffer = new ByteBuffer[NGFonts.values().length];
    private Map<Path, Integer> imageBuffer = new HashMap<>();

    private final Collection<Consumer<Painter>> drawBuffer = new ArrayList<>();
    private final Lock drawBufferLock = new ReentrantLock();

    /**
     * @throws IOException If an error occurs during the setup of the Hud.
     * @param antialiasLevel
     */
    public void init(int antialiasLevel) throws IOException {
        if (antialiasLevel > 0) {
            vg = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
        } else {
            vg = nvgCreate(NVG_STENCIL_STROKES);
        }

        if (vg == NULL) {
            throw new IOException("Could not initialize NanoVG");
        }

        NGFonts[] fonts = NGFonts.values();
        for (int i = 0; i < fonts.length; i++) {
            fontBuffer[i] = fonts[i].asByteBuffer();
            if (nvgCreateFontMem(vg, fonts[i].name, fontBuffer[i], 1) == -1) {
                Logger.ERROR.print("Could not create font " + fonts[i].name);
            }
        }

        nvgColorBuffer = NVGColor.create();
        paint = NVGPaint.create();
    }

    public void cleanup() {
        drawBufferLock.lock();
        try {
            drawBuffer.clear();
        } finally {
            drawBufferLock.unlock();
        }
    }

    public void addHudItem(Consumer<Painter> render) {
        if (render == null) return;

        drawBufferLock.lock();
        try {
            drawBuffer.add(render);
        } finally {
            drawBufferLock.unlock();
        }
    }

    public void removeHudItem(Consumer<Painter> render) {
        if (render == null) return;

        drawBufferLock.lock();
        try {
            drawBuffer.remove(render);
        } finally {
            drawBufferLock.unlock();
        }
    }


    /**
     * @param windowWidth    width of the current window drawn on
     * @param windowHeight   height of the current window
     * @param mapper         maps a world-space vector to relative position ([-1, 1], [-1, 1]) in the view.
     * @param cameraPosition position of camera
     */
    public void draw(int windowWidth, int windowHeight, Function<Vector3f, Vector2f> mapper, Vector3f cameraPosition) {
        Painter vanGogh = new Painter(windowWidth, windowHeight, mapper, cameraPosition, 5, 5, 24);
        draw(windowWidth, windowHeight, vanGogh);
    }

    /**
     * @param windowWidth  width of the current window drawn on
     * @param windowHeight height of the current window
     * @param xRoll        x position of the debug printroll screen
     * @param yRoll        y position of ''
     * @param rollSize     font size of ''
     */
    public void draw(int windowWidth, int windowHeight, int xRoll, int yRoll, int rollSize) {
        Painter bobRoss = new Painter(windowWidth, windowHeight, (v) -> new Vector2f(), Vectors.Z, xRoll, yRoll, rollSize);
        draw(windowWidth, windowHeight, bobRoss);
    }

    /**
     * draw using the given painter
     */
    private synchronized void draw(int windowWidth, int windowHeight, Painter painter) {
        // this should be the case
        glViewport(0, 0, windowWidth, windowHeight);
        // Begin NanoVG frame
        nvgBeginFrame(vg, windowWidth, windowHeight, 1);

        // Draw the right drawhandlers
        drawBufferLock.lock();
        try {
            drawBuffer.forEach(m -> m.accept(painter));
        } finally {
            drawBufferLock.unlock();
        }

        // End NanoVG frame
        nvgEndFrame(vg);

        // restore window state
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
    }

    public class Painter {
        private final int printRollSize;
        private final int yPrintRoll;
        private final int xPrintRoll;
        private int printRollEntry = 0;
        /** maps a position in world-space to a position on the screen */
        private final Function<Vector3f, Vector2f> mapper;

        private Color4f fillColor = Color4f.WHITE;
        private Color4f strokeColor;
        private int strokeWidth = 1;
        private Color4f textColor = Color4f.WHITE;

        public final int windowWidth;
        public final int windowHeight;
        public final Vector3fc cameraPosition;

        /**
         * @param windowWidth    width of this hud display iteration
         * @param windowHeight   height of ''
         * @param mapper         maps a world-space vector to relative position ([-1, 1], [-1, 1]) in the view.
         * @param cameraPosition renderposition of camera in worldspace
         * @param xPrintRoll     x position of where to start the printRoll
         * @param yPrintRoll     y position of where to start the printRoll
         * @param printRollSize  fontsize of printRoll
         */
        public Painter(
                int windowWidth, int windowHeight, Function<Vector3f, Vector2f> mapper, Vector3fc cameraPosition,
                int xPrintRoll, int yPrintRoll, int printRollSize
        ) {
            this.windowWidth = windowWidth;
            this.windowHeight = windowHeight;
            this.mapper = mapper;
            this.cameraPosition = cameraPosition;
            this.printRollSize = printRollSize;
            this.yPrintRoll = printRollSize + yPrintRoll;
            this.xPrintRoll = xPrintRoll;
        }

        /**
         * Get an instance of NVGColor with the correct values. All nvgColorBuffer values are floating point numbers
         * supposed to be between 0f and 1f.
         * @param red   The red component.
         * @param green The green component.
         * @param blue  The blue component.
         * @param alpha The alpha component.
         * @return an instance of NVGColor.
         */
        private NVGColor toBuffer(float red, float green, float blue, float alpha) {
            nvgColorBuffer.r(red);
            nvgColorBuffer.g(green);
            nvgColorBuffer.b(blue);
            nvgColorBuffer.a(alpha);

            return nvgColorBuffer;
        }

        /** @see #toBuffer(float, float, float, float) */
        private NVGColor toBuffer(Color4f color) {
            return toBuffer(color.red, color.green, color.blue, color.alpha);
        }

        /**
         * sets the basic fill color of this painter to the given color
         * @param color a color, where the alpha value gives the opacity of the object
         */
        public void setFillColor(Color4f color) {
            this.fillColor = color;
            nvgFillColor(vg, toBuffer(color));
        }

        /**
         * sets the basic stroke syle of this painter
         * @param color the color of the stroke, if alpha is less than 1, the edge of the fill underneath will be
         *              visible
         * @param width the width of the stroke in pixels
         */
        public void setStroke(Color4f color, int width) {
            this.strokeWidth = width;
            this.strokeColor = color;
            nvgStrokeWidth(vg, width);
            nvgStrokeColor(vg, toBuffer(color));
        }

        /**
         * draws a rectangle using the basic fill and stroke style
         * @see #rectangle(int, int, int, int, Color4f, Color4f, int)
         */
        public void rectangle(int x, int y, int width, int height) {
            assert width >= 0 : "Negative width: " + width + " (height = " + height + ")";
            assert height >= 0 : "Negative height: " + height + " (width = " + width + ")";

            nvgBeginPath(vg);
            nvgRect(vg, x, y, width, height);

            nvgFill(vg);
            nvgStroke(vg);
        }

        /**
         * draws a rectangle on the given position with the given style. After this method call, the colors are reset to
         * the basic colors
         * @param x           the x position in pixels relative to the leftmost position on the GL frame
         * @param y           the y position in pixels relative to the topmost position on the GL frame
         * @param width       the width of this rectangle in pixels
         * @param height      the height of this rectangle in pixels
         * @param fillColor   the color used for the background of this rectangle
         * @param strokeColor the color used for the line around this rectangle
         * @param strokeWidth the width of the line around this rectangle
         */
        public void rectangle(
                int x, int y, int width, int height, Color4f fillColor, Color4f strokeColor, int strokeWidth
        ) {
            nvgFillColor(vg, toBuffer(fillColor));
            nvgStrokeColor(vg, toBuffer(strokeColor));
            nvgStrokeWidth(vg, strokeWidth);
            rectangle(x, y, width, height);
            restoreColors();
        }

        /**
         * resets the colors to the colors given using {@link #setFillColor(Color4f)} and {@link #setStroke(Color4f,
         * int)}
         */
        private void restoreColors() {
            nvgFillColor(vg, toBuffer(fillColor));
            nvgStrokeWidth(vg, strokeWidth);
            nvgStrokeColor(vg, toBuffer(strokeColor));
        }

        /**
         * @param pos    the position as (x, y) in pixels, measured from the top left
         * @param dim    the width and height as (width, height) of the rectangle
         * @param indent the size of the part removed from the rectangle in pixels
         */
        public void roundedRectangle(Vector2ic pos, Vector2ic dim, int indent) {
            roundedRectangle(pos.x(), pos.y(), dim.x(), dim.y(), indent);
        }

        /**
         * @param x      the x position in pixels relative to the leftmost position on the GL frame
         * @param y      the y position in pixels relative to the topmost position on the GL frame
         * @param width  the width of this rectangle in pixels
         * @param height the height of this rectangle in pixels
         * @param indent the size of the part removed from the rectangle in pixels
         */
        public void roundedRectangle(int x, int y, int width, int height, int indent) {
            assert width > 0 && height > 0 : String.format("Non-positive dimensions: height = %d, width = %d", height, width);

            int xMax = x + width;
            int yMax = y + height;

            polygon(
                    new Vector2i(x + indent, y),
                    new Vector2i(xMax - indent, y),
                    new Vector2i(xMax, y + indent),
                    new Vector2i(xMax, yMax - indent),
                    new Vector2i(xMax - indent, yMax),
                    new Vector2i(x + indent, yMax),
                    new Vector2i(x, yMax - indent),
                    new Vector2i(x, y + indent)
            );
        }

        /**
         * @param x           the x position in pixels relative to the leftmost position on the GL frame
         * @param y           the y position in pixels relative to the topmost position on the GL frame
         * @param width       the width of this rectangle in pixels
         * @param height      the height of this rectangle in pixels
         * @param indent      the size of the part removed from the rectangle in pixels
         * @param fillColor   the color used for the background of this rectangle
         * @param strokeColor the color used for the line around this rectangle
         * @param strokeWidth the width of the line around this rectangle
         */
        public void roundedRectangle(
                int x, int y, int width, int height, int indent, Color4f fillColor, Color4f strokeColor, int strokeWidth
        ) {
            nvgFillColor(vg, toBuffer(fillColor));
            nvgStrokeColor(vg, toBuffer(strokeColor));
            nvgStrokeWidth(vg, strokeWidth);
            roundedRectangle(x, y, width, height, indent);
            restoreColors();
        }

        public void circle(int x, int y, int radius) {
            nvgBeginPath(vg);
            nvgCircle(vg, x, y, radius);

            nvgFill(vg);
            nvgStroke(vg);
        }

        public void polygon(Color4f fillColor, Color4f strokeColor, int strokeWidth, Vector2i... points) {
            nvgFillColor(vg, toBuffer(fillColor));
            nvgStrokeColor(vg, toBuffer(strokeColor));
            nvgStrokeWidth(vg, strokeWidth);
            polygon(points);
            restoreColors();
        }

        public void polygon(Vector2i... points) {
            nvgBeginPath(vg);

            nvgMoveTo(vg, points[points.length - 1].x, points[points.length - 1].y);
            for (Vector2i point : points) {
                nvgLineTo(vg, point.x, point.y);
            }

            nvgFill(vg);
            nvgStroke(vg);
        }

        /**
         * draw a line along the given coordinates
         * @param points (x, y) pairs of screen coordinates
         */
        public void line(int strokeWidth, Color4f strokeColor, Vector2i... points) {
            nvgStrokeColor(vg, toBuffer(strokeColor));
            nvgStrokeWidth(vg, strokeWidth);
            nvgBeginPath(vg);
            nvgMoveTo(vg, points[0].x, points[0].y);

            for (int i = 1; i < points.length; i++) {
                nvgLineTo(vg, points[i].x, points[i].y);
            }

            nvgStroke(vg);
            restoreColors();
        }

        // non-shape functions

        public void text(int x, int y, float size, NGFonts font, int align, Color4f color, String text) {
            nvgFontSize(vg, size);
            nvgFontFace(vg, font.name);
            nvgTextAlign(vg, align);
            nvgFillColor(vg, toBuffer(color));
            nvgText(vg, x, y, text);

            nvgFillColor(vg, toBuffer(fillColor));
        }

        /** for debugging purposes. Prints the given text in the upper left corner of the screen */
        public void printRoll(String text) {
            int y = yPrintRoll + ((printRollSize + 5) * printRollEntry);

            text(xPrintRoll, y, printRollSize, NGFonts.LUCIDA_CONSOLE, NVG_ALIGN_LEFT, textColor, text);
            printRollEntry++;
        }

        public void image(Path filename, int x, int y, int width, int height, float alpha) throws IOException {
            image(filename, x, y, width, height, 0f, alpha, NVG_IMAGE_GENERATE_MIPMAPS);
        }

        public void image(Path fileName, int x, int y, int width, int height, float angle, float alpha, int imageFlags)
                throws IOException {
            int img = getImage(fileName, imageFlags);
            NVGPaint p = nvgImagePattern(vg, x, y, width, height, angle, img, alpha, paint);

            rectangle(x, y, width, height);

            nvgFillPaint(vg, p);
            nvgFill(vg);
        }

        private int getImage(Path filePath, int imageFlags) throws IOException {
            if (imageBuffer.containsKey(filePath)) {
                return imageBuffer.get(filePath);
            }
            ByteBuffer image = Toolbox.toByteBuffer(filePath);
            int img = nvgCreateImageMem(vg, imageFlags, image);
            imageBuffer.put(filePath, img);
            return img;
        }

        /**
         * @param worldPosition a position in world-space
         * @return the coordinates of this position as where they appear on the screen, possibly outside the borders.
         */
        public Vector2i positionOnScreen(Vector3f worldPosition) {
            final Vector2f relativePosition = mapper.apply(worldPosition);
            if (relativePosition == null) return null;

            relativePosition.add(1f, -1f).mul(0.5f, -0.5f);

            int x = (int) (relativePosition.x() * windowWidth);
            int y = (int) (relativePosition.y() * windowHeight);
            return new Vector2i(x, y);
        }

        public void drawTexture(
                Texture image, int x, int y, int width, int height, float txMin, float tyMin, float txMax, float tyMax
        ) {
            image.bind(GL_TEXTURE0);

        }
    }
}
