package NG.ScreenOverlay.Frames.Components;

import NG.ActionHandling.MouseRelativeClickListener;
import NG.ActionHandling.MouseReleaseListener;
import NG.ScreenOverlay.Frames.SFrameLookAndFeel;
import org.joml.Vector2ic;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * @author Geert van Ieperen. Created on 22-9-2018.
 */
public class SCloseButton extends SComponent implements MouseReleaseListener, MouseRelativeClickListener {
    private final SFrame frame;
    private boolean state = false;

    public SCloseButton(SFrame frame) {
        this.frame = frame;
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
        design.drawButton(scPos, dimensions, "X", state);

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
        if (button == GLFW_MOUSE_BUTTON_LEFT) frame.dispose();
    }

    @Override
    public String toString() {
        return "SCloseButton";
    }
}
