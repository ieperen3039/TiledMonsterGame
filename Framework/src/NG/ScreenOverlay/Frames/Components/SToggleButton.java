package NG.ScreenOverlay.Frames.Components;

import NG.ActionHandling.MouseRelativeClickListener;
import NG.ScreenOverlay.Frames.SFrameLookAndFeel;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;

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
     * @param text      the displayed text
     * @param minWidth  the minimal width of this button, which {@link NG.ScreenOverlay.Frames.LayoutManagers.SLayoutManager}s
     *                  should respect
     * @param minHeight the minimal height of this button.
     * @param initial   the initial state of the button. Iff true, the button will be enabled
     */
    public SToggleButton(String text, int minWidth, int minHeight, boolean initial) {
        this.minHeight = minHeight;
        this.minWidth = minWidth;
        this.text = text;
        this.state = initial;
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
        design.drawButton(screenPosition, dimensions, text, state);
    }

    @Override
    public void onClick(int button, int xSc, int ySc) {
        setState(!state);
    }

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
