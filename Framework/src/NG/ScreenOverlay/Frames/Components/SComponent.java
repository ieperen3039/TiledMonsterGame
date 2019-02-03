package NG.ScreenOverlay.Frames.Components;

import NG.ScreenOverlay.Frames.SFrameLookAndFeel;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Optional;

/**
 * The S stands for Sub-
 * @author Geert van Ieperen. Created on 20-9-2018.
 */
public abstract class SComponent {
    private boolean layoutIsValid = false;
    private boolean isVisible = true;
    private SContainer parent = null;

    protected final Vector2i position = new Vector2i();
    protected final Vector2i dimensions = new Vector2i();

    /**
     * @return minimum width of this component in pixels. The final width can be assumed to be at least this size unless
     * the layout manger decides otherwise.
     */
    public abstract int minWidth();

    /**
     * @return minimum height of this component in pixels. The final height is at least this size unless the layout
     * manger decides otherwise.
     */
    public abstract int minHeight();

    protected void invalidateLayout() {
        layoutIsValid = false;
        if (parent != null) parent.invalidateLayout();
    }

    public void validateLayout() {
        layoutIsValid = true;
    }

    /**
     * @return true if this component should expand horizontally when possible. when false, the components should always
     * be its minimum width.
     */
    public abstract boolean wantHorizontalGrow();

    /**
     * @return true if this component should expand horizontally when possible. When false, the components should always
     * be its minimum height.
     */
    public abstract boolean wantVerticalGrow();

    /**
     * if this has sub-components, it will find the topmost component {@code c} for which {@code c.contains(x, y)}.
     * @param xRel a relative x coordinate
     * @param yRel a relative y coordinate
     * @return the topmost component {@code c} for which {@code c.contains(x, y)}.
     */
    public SComponent getComponentAt(int xRel, int yRel) {
        return this;
    }

    public boolean contains(Vector2i v) {
        return contains(v.x, v.y);
    }

    /**
     * checks whether the given coordinate is within this component
     * @param x x position relative to parent
     * @param y y position relative to parent
     * @return true iff the given coordinate lies within the bounds defined by position and dimensions
     */
    public boolean contains(int x, int y) {
        int xr = x - getX();
        if (xr < 0 || xr > getWidth()) {
            return false;
        }

        int yr = y - getY();
        return !(yr < 0 || yr > getHeight());
    }

    /**
     * sets the position of this component relative to its parent. If this component is part of a layout, then this
     * method should only be called by the layout manager.
     */
    public void setPosition(int x, int y) {
        position.set(x, y);
    }

    /** @see #setPosition(int, int) */
    public void setPosition(Vector2ic position) {
        this.position.set(position);
    }

    /** Adds the given x and y to the position, like a call of {@code setPosition(getX() + xDelta, getY() + yDelta);} */
    public void addToPosition(int xDelta, int yDelta) {
        position.add(xDelta, yDelta);
    }

    public void setSize(int width, int height) {
        width = Math.max(width, minWidth());
        height = Math.max(height, minHeight());

        assert width >= 0 : "Negative width: " + width + " (height = " + height + ")";
        assert height >= 0 : "Negative height: " + height + " (width = " + width + ")";

        dimensions.set(width, height);
        invalidateLayout();
    }

    public void addToSize(int xDelta, int yDelta) {
        setSize(dimensions.x + xDelta, dimensions.y + yDelta);
    }

    // getters

    /** @see #getPosition() */
    public int getX() {
        return position.x;
    }

    /** @see #getPosition() */
    public int getY() {
        return position.y;
    }

    /** @return the position of this object in regard to its parent */
    public Vector2ic getPosition() {
        return position;
    }

    public int getWidth() {
        return dimensions.x;
    }

    public int getHeight() {
        return dimensions.y;
    }

    /**
     * Draw this component.
     * @param design         The element that provides functions for drawing
     * @param screenPosition the position that should be assumed instead of that returned by {@link #getPosition()}.
     *                       This position is absolute regarding the screen.
     */
    public abstract void draw(SFrameLookAndFeel design, Vector2ic screenPosition);

    /**
     * sets the visibility and invalidates the layout of the parent.
     * @param doVisible if true, the component is set visible and if possible, its parent is updated. If false, the
     *                  component will not be drawn.
     */
    public void setVisible(boolean doVisible) {
        isVisible = doVisible;
    }

    /** @return whether this component is drawn */
    public boolean isVisible() {
        return isVisible;
    }

    public void setParent(SContainer parent) {
        this.parent = parent;
    }

    public Vector2ic getScreenPosition() {
        if (parent == null) return position;

        Vector2ic scRef = parent.getScreenPosition();
        return new Vector2i(scRef).add(position);
    }

    Optional<SContainer> getParent() {
        return Optional.ofNullable(parent);
    }

    public boolean layoutIsValid() {
        return layoutIsValid;
    }
}
