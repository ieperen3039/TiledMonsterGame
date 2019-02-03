package NG.ActionHandling.MouseTools;

import NG.ActionHandling.MouseMoveListener;
import NG.ActionHandling.MouseRelativeClickListener;
import NG.ActionHandling.MouseReleaseListener;
import NG.DataStructures.Tracked.TrackedInteger;
import NG.Entities.Entity;
import NG.ScreenOverlay.Frames.Components.SComponent;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.lwjgl.glfw.GLFW;

/**
 * @author Geert van Ieperen. Created on 26-11-2018.
 */
public class DefaultMouseTool implements MouseTool {
    private int dragButton = 0;
    private MouseMoveListener dragListener = null;
    private MouseReleaseListener releaseListener = null;
    private TrackedInteger cameraXPos = new TrackedInteger(0);
    private TrackedInteger cameraYPos = new TrackedInteger(0);
    private int button;

    @Override
    public void apply(SComponent component, int xSc, int ySc) {
        Logger.DEBUG.print("Clicked on " + component);

        if (component instanceof MouseRelativeClickListener) {
            MouseRelativeClickListener cl = (MouseRelativeClickListener) component;
            // by def. of MouseRelativeClickListener, give relative coordinates
            Vector2ic pos = component.getScreenPosition();
            cl.onClick(button, xSc - pos.x(), ySc - pos.y());
        }

        if (component instanceof MouseMoveListener) {
            dragListener = (MouseMoveListener) component;
            dragButton = button;
            cameraXPos.update(xSc);
            cameraYPos.update(ySc);

        } else {
            dragListener = null;
        }

        if (component instanceof MouseReleaseListener) {
            releaseListener = (MouseReleaseListener) component;

        } else {
            releaseListener = null;
        }
    }

    @Override
    public void apply(Entity entity, int xSc, int ySc) {
        Logger.DEBUG.print("Clicked on " + entity);
        entity.onClick(button);
    }

    @Override
    public void apply(Vector2fc position) {
        Logger.DEBUG.print("Clicked on " + Vectors.toString(position));
    }

    @Override
    public void onRelease(int button, int xSc, int ySc) {
        if (button != dragButton) return;

        dragListener = null;
        if (releaseListener != null) {
            releaseListener.onRelease(button, xSc, ySc);
            releaseListener = null;
        }
    }

    @Override
    public void mouseMoved(int xPos, int yPos) {
        if (dragListener == null) return;

        cameraXPos.update(xPos);
        cameraYPos.update(yPos);
        int xDelta = cameraXPos.difference();
        int yDelta = cameraYPos.difference();
        dragListener.mouseMoved(xDelta, yDelta);
    }

    @Override
    public String toString() {
        return "Default MouseTool";
    }

    /**
     * sets the button field. Should only be called by the input handling
     * @param button a button enum, often {@link GLFW#GLFW_MOUSE_BUTTON_LEFT} or {@link GLFW#GLFW_MOUSE_BUTTON_RIGHT}
     */
    @Override
    public void setButton(int button) {
        this.button = button;
    }
}
