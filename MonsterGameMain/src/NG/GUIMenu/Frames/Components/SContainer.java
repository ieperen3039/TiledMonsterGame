package NG.GUIMenu.Frames.Components;

import NG.GUIMenu.Frames.LayoutManagers.GridLayoutManager;
import NG.GUIMenu.Frames.LayoutManagers.SLayoutManager;
import NG.GUIMenu.Frames.LayoutManagers.SingleElementLayout;
import NG.GUIMenu.Frames.SFrameLookAndFeel;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector4i;
import org.joml.Vector4ic;

/**
 * @author Geert van Ieperen. Created on 20-9-2018.
 */
public abstract class SContainer extends SComponent {
    public static final int INNER_BORDER = 4;
    public static final int OUTER_BORDER = 4;
    private final SLayoutManager layout;

    private boolean wantHzGrow;
    private boolean wantVtGrow;

    /**
     * a container that uses the given manager for its layout
     */
    public SContainer(SLayoutManager layout, boolean growHorizontal, boolean growVertical) {
        this.layout = layout;
        this.wantHzGrow = growHorizontal;
        this.wantVtGrow = growVertical;
    }

    /**
     * constructor for a container that uses a grid layout of the given size and a growth policy of true
     * @param xElts      nr of elements in width
     * @param yElts      nr of elements in height
     * @param growPolicy
     */
    public SContainer(int xElts, int yElts, boolean growPolicy) {
        this(new GridLayoutManager(xElts, yElts), growPolicy, growPolicy);
    }

    /**
     * a wrapper for a target component, to have it behave as a container with one value being itself
     * @param target the component to wrap
     * @return the target as a container object
     */
    public static SContainer singleton(SComponent target) {
        final SContainer c = new SContainer(new SingleElementLayout(), false, false) {
            @Override
            public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
                drawChildren(design, screenPosition);
            }
        };
        c.add(target, null);

        return c;
    }

    /**
     * @param comp the component to be added
     * @param prop the property instance accepted by the current layout manager
     */
    public void add(SComponent comp, Object prop) {
        layout.add(comp, prop);
        comp.setParent(this);
        invalidateLayout();
    }

    private Iterable<SComponent> children() {
        return layout.getComponents();
    }

    /**
     * removes a component from this container
     * @param comp the component that should be added to this component first.
     */
    public void removeComponent(SComponent comp) {
        layout.remove(comp);
        invalidateLayout();
    }

    public void drawChildren(SFrameLookAndFeel lookFeel, Vector2ic offset) {
        for (SComponent component : children()) {
            if (component.isVisible() && component.getWidth() != 0 && component.getHeight() != 0) {
                Vector2i scPos = new Vector2i(component.position).add(offset);
                component.draw(lookFeel, scPos);
            }
        }
    }

    @Override
    public SComponent getComponentAt(int xRel, int yRel) {
        for (SComponent component : children()) {
            if (component.isVisible() && component.contains(xRel, yRel)) {
                int xr = xRel - component.getX();
                int yr = yRel - component.getY();
                return component.getComponentAt(xr, yr);
            }
        }
        return this;
    }

    @Override
    public int minWidth() {
        if (!layoutIsValid()) layout.recalculateProperties();
        return layout.getMinimumWidth() + (INNER_BORDER * 2);
    }

    @Override
    public int minHeight() {
        if (!layoutIsValid()) layout.recalculateProperties();
        return layout.getMinimumHeight() + (INNER_BORDER * 2);
    }

    @Override
    public void addToSize(int xDelta, int yDelta) {
        super.addToSize(xDelta, yDelta);
        invalidateLayout();
    }

    @Override
    public void validateLayout() {
        if (layoutIsValid()) return;

        // first restructure this container
        layout.recalculateProperties();
        Vector4ic border = getBorderSize();
        Vector2i displacement = new Vector2i(border.x(), border.y());
        Vector2i newDim = new Vector2i(dimensions).sub(border.x() + border.z(), border.y() + border.w());
        layout.setDimensions(displacement, newDim);
        layout.placeComponents();

        // then restructure the children
        for (SComponent child : children()) {
            child.validateLayout();
        }

        super.validateLayout();
    }

    /**
     * Gives the desired border sizes for this container, as (lesser x, lesser y, greater x, greater y)
     * @return a vector with the border sizes as (x1, y1, x2, y2).
     * @see #invalidateLayout()
     */
    protected Vector4ic getBorderSize() {
        return new Vector4i(INNER_BORDER, INNER_BORDER, INNER_BORDER, INNER_BORDER);
    }

    /**
     * sets the want-grow policies.
     * @param horizontal if true, the next invocation of {@link #wantHorizontalGrow()} will return true. Otherwise, it
     *                   will return true iff any of its child components returns true on that method.
     * @param vertical   if true, the next invocation of {@link #wantVerticalGrow()} will return true. Otherwise, it
     *                   will return true iff any of its child components returns true on that method.
     */
    public void setGrowthPolicy(boolean horizontal, boolean vertical) {
        wantHzGrow = horizontal;
        wantVtGrow = vertical;
    }

    @Override
    public boolean wantHorizontalGrow() {
        if (wantHzGrow) return true;
        for (SComponent c : children()) {
            if (c.wantHorizontalGrow()) return true;
        }
        return false;
    }

    @Override
    public boolean wantVerticalGrow() {
        if (wantVtGrow) return true;
        for (SComponent c : children()) {
            if (c.wantVerticalGrow()) return true;
        }
        return false;
    }

}
