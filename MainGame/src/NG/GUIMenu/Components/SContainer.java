package NG.GUIMenu.Components;

import NG.GUIMenu.LayoutManagers.GridLayoutManager;
import NG.GUIMenu.LayoutManagers.SLayoutManager;
import NG.GUIMenu.LayoutManagers.SingleElementLayout;
import NG.GUIMenu.Rendering.SFrameLookAndFeel;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Collection;

/**
 * @author Geert van Ieperen. Created on 20-9-2018.
 */
public abstract class SContainer extends SComponent {
    private final SLayoutManager layout;
    protected ComponentBorder layoutBorder;

    /**
     * a container that uses the given manager for its layout
     */
    public SContainer(SLayoutManager layout) {
        this.layout = layout;
    }

    /**
     * constructor for a container that uses a grid layout of the given size and a growth policy of true
     * @param xElts nr of elements in width
     * @param yElts nr of elements in height
     */
    public SContainer(int xElts, int yElts) {
        this(new GridLayoutManager(xElts, yElts));
    }

    /**
     * a wrapper for a target component, to have it behave as a container with one value being itself
     * @param target the component to wrap
     * @return the target as a container object
     */
    public static SContainer singleton(SComponent target) {
        SContainer c = new SGhostContainer(new SingleElementLayout());
        c.add(target, null);
        return c;
    }

    /** creates a new invisible container with the given components on a single column */
    public static SContainer column(SComponent... components) {
        SContainer column = new SGhostContainer(new GridLayoutManager(1, components.length));

        for (int i = 0; i < components.length; i++) {
            column.add(components[i], new Vector2i(0, i));
        }

        column.setSize(0, 0);
        return column;
    }

    /** creates a new invisible container with the given components in a single row */
    public static SContainer row(SComponent... components) {
        SContainer row = new SGhostContainer(new GridLayoutManager(components.length, 1));

        for (int i = 0; i < components.length; i++) {
            row.add(components[i], new Vector2i(i, 0));
        }

        row.setSize(0, 0);
        return row;
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

    protected Collection<SComponent> children() {
        return layout.getComponents();
    }

    /**
     * removes a component from this container
     * @param comp the component that should be added to this component first.
     */
    public void removeCompoment(SComponent comp) {
        layout.remove(comp);
        invalidateLayout();
    }

    public void drawChildren(SFrameLookAndFeel lookFeel, Vector2ic offset) {
        for (SComponent component : children()) {
            if (component.isVisible() && component.getWidth() != 0 && component.getHeight() != 0) {
                Vector2i scPos = new Vector2i(component.getPosition()).add(offset);
                component.draw(lookFeel, scPos);
            }
        }
    }

    @Override
    public SComponent getComponentAt(int xRel, int yRel) {
        validateLayout();
        for (SComponent component : children()) {
            if (component.isVisible() && component.contains(xRel, yRel)) {
                xRel -= component.getX();
                yRel -= component.getY();
                return component.getComponentAt(xRel, yRel);
            }
        }
        return this;
    }

    @Override
    public int minWidth() {
        validateLayoutSize();
        ComponentBorder borderSize = layoutBorder;
        return layout.getMinimumWidth() + borderSize.left + borderSize.right;
    }

    private void validateLayoutSize() {
        if (!layoutIsValid()) {
            layout.recalculateProperties();
            layoutBorder = newLayoutBorder();
        }
    }

    @Override
    public int minHeight() {
        validateLayoutSize();
        ComponentBorder borderSize = layoutBorder;
        return layout.getMinimumHeight() + borderSize.top + borderSize.bottom;
    }

    @Override
    public void doValidateLayout() {
        // first restructure this container
        validateLayoutSize();

        // ensure minimum width and height
        Vector2i layoutPos = new Vector2i();
        Vector2i layoutDim = new Vector2i(getSize());
        layoutBorder.reduce(layoutPos, layoutDim);
        layout.placeComponents(layoutPos, layoutDim);

        // then restructure the children
        for (SComponent child : children()) {
            child.validateLayout();
        }
    }

    @Override
    public boolean wantHorizontalGrow() {
        return super.wantHorizontalGrow() && layout.wantHorizontalGrow();
    }

    @Override
    public boolean wantVerticalGrow() {
        return super.wantVerticalGrow() && layout.wantVerticalGrow();
    }

    @Override
    public SContainer setGrowthPolicy(boolean horizontal, boolean vertical) {
        super.setGrowthPolicy(horizontal, vertical);
        return this;
    }

    /**
     * Gives the desired border sizes for this container
     */
    protected ComponentBorder newLayoutBorder() {
        return new ComponentBorder(4);
    }

    private static class SGhostContainer extends SContainer {
        public SGhostContainer(SLayoutManager layout) {
            super(layout);
        }

        @Override
        public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
            drawChildren(design, screenPosition);
        }

        @Override
        public SComponent getComponentAt(int xRel, int yRel) {
            SComponent found = super.getComponentAt(xRel, yRel);
            if (found == this) return null;
            return found;
        }

        @Override
        protected ComponentBorder newLayoutBorder() {
            return new ComponentBorder();
        }
    }
}
