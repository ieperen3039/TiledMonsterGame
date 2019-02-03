package NG.ScreenOverlay.Frames.Components;

import NG.ActionHandling.MouseMoveListener;
import NG.ScreenOverlay.Frames.SFrameLookAndFeel;
import org.joml.Vector2ic;

/**
 * @author Geert van Ieperen. Created on 25-9-2018.
 */
public class SDragEdge extends SComponent implements MouseMoveListener {
    private final SComponent parent;
    private final int width;
    private final int height;

    public SDragEdge(SComponent parent, int width, int height) {
        this.width = width;
        this.height = height;
        setSize(width, height);
        this.parent = parent;
    }

    @Override
    public int minWidth() {
        return width;
    }

    @Override
    public int minHeight() {
        return height;
    }

    @Override
    public boolean wantHorizontalGrow() {
        return false;
    }

    @Override
    public boolean wantVerticalGrow() {
        return false;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        design.drawButton(screenPosition, dimensions, "+", false);
    }

    @Override
    public void mouseMoved(int xDelta, int yDelta) {
        parent.addToSize(xDelta, yDelta);
    }
}
