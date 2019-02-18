package NG.ScreenOverlay.Frames.Components;

import NG.ActionHandling.MouseRelativeClickListener;
import NG.ActionHandling.MouseReleaseListener;
import NG.ScreenOverlay.Frames.SFrameLookAndFeel;
import NG.ScreenOverlay.NGFonts;
import org.joml.Vector2ic;

import static NG.ScreenOverlay.Frames.SFrameLookAndFeel.UIComponent.BUTTON_ACTIVE;
import static NG.ScreenOverlay.Frames.SFrameLookAndFeel.UIComponent.BUTTON_INACTIVE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * @author Geert van Ieperen. Created on 22-9-2018.
 */
public class SCloseButton extends SComponent implements MouseReleaseListener, MouseRelativeClickListener {
    private Runnable closeAction;
    private boolean state = false;

    public SCloseButton(SFrame frame) {
        this.closeAction = frame::dispose;
    }

    public SCloseButton(Runnable closeAction) {
        this.closeAction = closeAction;
    }

    public void setCloseAction(Runnable closeAction) {
        this.closeAction = closeAction;
    }

    @Override
    public int minWidth() {
        return SFrame.FRAME_TITLE_BAR_SIZE;
    }

    @Override
    public int minHeight() {
        return SFrame.FRAME_TITLE_BAR_SIZE;
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
    public void draw(SFrameLookAndFeel design, Vector2ic scPos) {
        design.draw(state ? BUTTON_ACTIVE : BUTTON_INACTIVE, scPos, dimensions);
        design.drawText(scPos, dimensions, "X", NGFonts.TextType.ACCENT, true);

//        try {
//            design.drawIconButton(position, dimensions, null, state);
//        } catch (IOException e) {
//            Logger.WARN.print(e);
//        }
    }

    @Override
    public void onClick(int button, int x, int y) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) state = true;
    }

    @Override
    public void onRelease(int button, int x, int y) {
        if (state && button == GLFW_MOUSE_BUTTON_LEFT) {
            closeAction.run();
        }
    }

    @Override
    public String toString() {
        return "SCloseButton";
    }
}
