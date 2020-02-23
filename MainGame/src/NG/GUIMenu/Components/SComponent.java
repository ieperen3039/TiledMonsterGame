package NG.GUIMenu.Components;

import NG.GUIMenu.Rendering.SFrameLookAndFeel;
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
    private SComponent parent = null;

    private final Vector2i position = new Vector2i();
    private final Vector2i dimensions = new Vector2i();

    private boolean wantHzGrow = true;
    private boolean wantVtGrow = true;

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

    /**
     * sets the layout validity flag of this component and all of its parents to false.
     */
    protected final void invalidateLayout() {
        if (layoutIsValid) {
            layoutIsValid = false;
            if (parent != null) parent.invalidateLayout();
        }
    }

    /**
     * restores the validity of the layout of this component.
     * @see #doValidateLayout()
     */
    public final void validateLayout() {
        if (!layoutIsValid) {
            doValidateLayout();
            layoutIsValid = true;
        }
    }

    /**
     * set the validity of this component and all of its children
     */
    protected void doValidateLayout() {
    }

    /**
     * sets the want-grow policies.
     * @param horizontal if true, the next invocation of {@link #wantHorizontalGrow()} will return true. Otherwise, it
     *                   will return true iff any of its child components returns true on that method.
     * @param vertical   if true, the next invocation of {@link #wantVerticalGrow()} will return true. Otherwise, it
     *                   will return true iff any of its child components returns true on that method.
     * @return this
     */
    public SComponent setGrowthPolicy(boolean horizontal, boolean vertical) {
        wantHzGrow = horizontal;
        wantVtGrow = vertical;
        invalidateLayout();
        return this;
    }

    /**
     * @return true if this component should expand horizontally when possible. when false, the components should always
     * be its minimum width.
     */
    public boolean wantHorizontalGrow() {
        return wantHzGrow;
    }

    /**
     * @return true if this component should expand horizontally when possible. When false, the components should always
     * be its minimum height.
     */
    public boolean wantVerticalGrow() {
        return wantVtGrow;
    }

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
        if (xr <= 0 || xr >= getWidth()) {
            return false;
        }

        int yr = y - getY();
        return !(yr <= 0 || yr >= getHeight());
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

    public final void setSize(int width, int height) {
        width = Math.max(width, minWidth());
        height = Math.max(height, minHeight());

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

    public Vector2i getScreenPosition() {
        if (parent == null) {
            return new Vector2i(position);
        } else {
            return parent.getScreenPosition().add(position);
        }
    }

    public int getWidth() {
        return dimensions.x;
    }

    public int getHeight() {
        return dimensions.y;
    }

    protected Vector2ic getSize() {
        return dimensions;
    }

    /**
     * Draw this component.
     * @param design         The element that provides functions for drawing
     * @param screenPosition the position where this component is drawn instead of the local (relative) position.
     */
    public abstract void draw(SFrameLookAndFeel design, Vector2ic screenPosition);

    /**
     * sets the visibility and invalidates the layout of the parent.
     * @param doVisible if true, the component is set visible and if possible, its parent is updated. If false, the
     *                  component will not be drawn.
     */
    public void setVisible(boolean doVisible) {
        isVisible = doVisible;
        if (doVisible) validateLayout();
    }

    /** @return whether this component is drawn */
    public boolean isVisible() {
        return isVisible;
    }

    public void setParent(SComponent parent) {
        this.parent = parent;
    }

    public Optional<SComponent> getParent() {
        return Optional.ofNullable(parent);
    }

    public boolean layoutIsValid() {
        return layoutIsValid;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
