package NG.ScreenOverlay.Frames.Components;

import NG.ActionHandling.MouseRelativeClickListener;
import NG.ScreenOverlay.Frames.SFrameLookAndFeel;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static NG.ScreenOverlay.Frames.SFrameLookAndFeel.UIComponent.BUTTON_ACTIVE;
import static NG.ScreenOverlay.Frames.SFrameLookAndFeel.UIComponent.BUTTON_INACTIVE;

/**
 * A button with a state that only changes upon clicking the button
 * @author Geert van Ieperen. Created on 22-9-2018.
 */
public class SToggleButton extends SComponent implements MouseRelativeClickListener {
    private final int minHeight;
    private final int minWidth;
    private boolean vtGrow = false;
    private boolean hzGrow = false;
    private String text;

    private boolean state;
    private List<Runnable> stateChangeListeners = new ArrayList<>();

    /**
     * Create a button with the given properties
     * @param text         the displayed text
     * @param minWidth     the minimal width of this button, which {@link NG.ScreenOverlay.Frames.LayoutManagers.SLayoutManager}s
     *                     should respect
     * @param minHeight    the minimal height of this button.
     * @param initialState the initial state of the button. If true, the button will be enabled
     */
    public SToggleButton(String text, int minWidth, int minHeight, boolean initialState) {
        this.minHeight = minHeight;
        this.minWidth = minWidth;
        this.text = text;
        this.state = initialState;
    }

    /**
     * Create a button with the given properties, starting disabled
     * @param text      the displayed text
     * @param minWidth  the minimal width of this buttion, which {@link NG.ScreenOverlay.Frames.LayoutManagers.SLayoutManager}s
     *                  should respect
     * @param minHeight the minimal height of this button.
     */
    public SToggleButton(String text, int minWidth, int minHeight) {
        this(text, minWidth, minHeight, false);
    }

    /**
     * Create a button with the given properties, starting disabled and with the given listener
     * @param text                the displayed text
     * @param minWidth            the minimal width of this buttion, which {@link NG.ScreenOverlay.Frames.LayoutManagers.SLayoutManager}s
     *                            should respect
     * @param minHeight           the minimal height of this button.
     * @param stateChangeListener upon change, this action is activated with the current state as argument
     */
    public SToggleButton(String text, int minWidth, int minHeight, Consumer<Boolean> stateChangeListener) {
        this(text, minWidth, minHeight);
        addStateChangeListener(() -> stateChangeListener.accept(state));
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

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        if (dimensions.x == 0 || dimensions.y == 0) return;
        design.draw(state ? BUTTON_ACTIVE : BUTTON_INACTIVE, screenPosition, dimensions);
        design.drawText(screenPosition, dimensions, text);
    }

    @Override
    public void onClick(int button, int xSc, int ySc) {
        setState(!state);
    }

    /**
     * @param action Upon change, this action is activated
     */
    public void addStateChangeListener(Runnable action) {
        stateChangeListeners.add(action);
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
        stateChangeListeners.forEach(Runnable::run);
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
