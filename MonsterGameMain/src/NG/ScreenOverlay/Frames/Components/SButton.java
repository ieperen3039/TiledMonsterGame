package NG.ScreenOverlay.Frames.Components;

import NG.ActionHandling.MouseRelativeClickListener;
import NG.ActionHandling.MouseReleaseListener;
import NG.ScreenOverlay.Frames.SFrameLookAndFeel;
import NG.ScreenOverlay.NGFonts;
import NG.Tools.Logger;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.Collection;

import static NG.ScreenOverlay.Frames.SFrameLookAndFeel.UIComponent.BUTTON_ACTIVE;
import static NG.ScreenOverlay.Frames.SFrameLookAndFeel.UIComponent.BUTTON_INACTIVE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

/**
 * A Button that may execute actions for both left and right clicks upon release.
 * @author Geert van Ieperen. Created on 22-9-2018.
 */
public class SButton extends SComponent implements MouseReleaseListener, MouseRelativeClickListener {
    private Collection<Runnable> leftClickListeners = new ArrayList<>();
    private Collection<Runnable> rightClickListeners = new ArrayList<>();
    private final int minHeight;
    private final int minWidth;

    private String text;
    private boolean isPressed = false;
    private boolean vtGrow = false;
    private boolean hzGrow = false;

    /**
     * a button with no associated action (a dead button)
     * @param text      the text of the button
     * @param minWidth  the minimal width of this buttion, which {@link NG.ScreenOverlay.Frames.LayoutManagers.SLayoutManager}s
     *                  should respect
     * @param minHeight the minimal height of this button.
     * @see #addLeftClickListener(Runnable)
     */
    public SButton(String text, int minWidth, int minHeight) {
        this.minHeight = minHeight;
        this.minWidth = minWidth;
        setText(text);
    }

    /**
     * a button with a basic associated action
     * @param text      the text of the button
     * @param action    the action that is executed upon (releasing a) left click
     * @param minWidth  the minimal width of this buttion, which {@link NG.ScreenOverlay.Frames.LayoutManagers.SLayoutManager}s
     *                  should respect
     * @param minHeight the minimal height of this button.
     */
    public SButton(String text, Runnable action, int minWidth, int minHeight) {
        this(text, minWidth, minHeight);
        leftClickListeners.add(action);
    }

    /**
     * a button with both a left and a right click action
     * @param text         the text of the button
     * @param onLeftClick  the action that is executed upon (releasing a) left click
     * @param onRightClick the action that is executed upon (releasing a) right click
     * @param minWidth     the minimal width of this buttion, which {@link NG.ScreenOverlay.Frames.LayoutManagers.SLayoutManager}s
     *                     should respect
     * @param minHeight    the minimal height of this button.
     */
    public SButton(String text, Runnable onLeftClick, Runnable onRightClick, int minWidth, int minHeight) {
        this(text, onLeftClick, minWidth, minHeight);
        rightClickListeners.add(onRightClick);
    }

    public void setGrowthPolicy(boolean horizontal, boolean vertical) {
        hzGrow = horizontal;
        vtGrow = vertical;
    }

    @Override
    public int minWidth() {
        return minWidth;
    }

    @Override
    public int minHeight() {
        return minHeight;
    }

    @Override
    public boolean wantHorizontalGrow() {
        return hzGrow;
    }

    @Override
    public boolean wantVerticalGrow() {
        return vtGrow;
    }

    public void addLeftClickListener(Runnable action) {
        leftClickListeners.add(action);
    }

    public void addRightClickListeners(Runnable action) {
        rightClickListeners.add(action);
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        design.draw(isPressed ? BUTTON_ACTIVE : BUTTON_INACTIVE, screenPosition, dimensions);
        design.drawText(screenPosition, dimensions, text, NGFonts.TextType.REGULAR, true);
    }

    @Override
    public void onClick(int button, int xSc, int ySc) {
        isPressed = true;
    }

    @Override
    public void onRelease(int button, int xSc, int ySc) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            leftClickListeners.forEach(Runnable::run);

        } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
            rightClickListeners.forEach(Runnable::run);
        } else {
            Logger.DEBUG.print("button clicked with " + button + " which has no action");
        }
        isPressed = false;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " (" + getText() + ")";
    }
}
