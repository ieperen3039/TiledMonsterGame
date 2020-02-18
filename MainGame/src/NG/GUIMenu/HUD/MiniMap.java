package NG.GUIMenu.HUD;

import NG.Core.Game;
import NG.DataStructures.Generic.Color4f;
import NG.GUIMenu.Components.SComponent;
import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.GUIMenu.GUIPainter;
import NG.GameMap.GameMap;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.nio.ByteBuffer;

/**
 * @author Geert van Ieperen created on 16-2-2020.
 */
public class MiniMap extends SComponent {
    private final Game game;
    private final int width;
    private final int height;
    private int nvgImage = -1;
    private int xSize = 20;
    @SuppressWarnings("SuspiciousNameCombination")
    private int ySize = xSize;

    private final Vector2i focus;
    private boolean isDirty = false;

    public MiniMap(Game game, int width, int height) {
        this.game = game;
        this.width = width;
        this.height = height;
        setGrowthPolicy(false, false);
        focus = new Vector2i(0, 0);
    }

    ByteBuffer getImageBuffer(int xFocus, int yFocus) {
        GameMap map = game.get(GameMap.class);
        Vector2ic size = map.getSize();
        int zFocus = map.getHeightAt(xFocus, yFocus);

        int xStart = xFocus - xSize / 2;
        int yStart = yFocus - xSize / 2;
        int xEnd = xStart + xSize; // this prevents rounding errors
        int yEnd = yStart + ySize;

        // Load texture contents into a byte buffer
        int byteSize = 4;
        ByteBuffer buffer = ByteBuffer.allocateDirect(byteSize * xSize * ySize);

        for (int x = xStart; x < xEnd; x++) {
            for (int y = yStart; y < yEnd; y++) {
                // if this coordinate is within the map
                if (x >= 0 && x < size.x() && y >= 0 && y < size.y()) {
                    int height = map.getHeightAt(x, y);
                    int offset = (height - zFocus) * 30 + 128;
                    Color4f color = Color4f.rgb(offset, 255, offset);
                    color.put(buffer);

                } else {
                    Color4f.BLACK.put(buffer);
                }
            }
        }

        buffer.rewind();

        return buffer;
    }

    @Override
    public int minWidth() {
        return width;
    }

    @Override
    public int minHeight() {
        return height;
    }

    public void setFocus(Vector2ic newFocus) {
        if (!newFocus.equals(focus)) {
            isDirty = true;
            focus.set(newFocus);
        }
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        GUIPainter painter = design.getPainter();

        if (nvgImage == -1) {
            nvgImage = painter.createImageFromBuffer(getImageBuffer(focus.x, focus.y), xSize, ySize);
            isDirty = false;

        } else if (isDirty) {
            painter.updateImageFromBuffer(nvgImage, getImageBuffer(focus.x, focus.y));
            isDirty = false;
        }

        design.getPainter().drawImage(nvgImage, screenPosition.x(), screenPosition.y(), getWidth(), getHeight());
    }
}
