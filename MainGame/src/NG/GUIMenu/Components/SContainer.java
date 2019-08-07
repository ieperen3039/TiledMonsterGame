package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.GUIMenu.LayoutManagers.GridLayoutManager;
import NG.GUIMenu.LayoutManagers.SLayoutManager;
import NG.GUIMenu.LayoutManagers.SingleElementLayout;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Collection;

/**
 * @author Geert van Ieperen. Created on 20-9-2018.
 */
public abstract class SContainer extends SComponent {
    private final SLayoutManager layout;

    /**
     * a container that uses the given manager for its layout
     */
    public SContainer(SLayoutManager layout) {
        this.layout = layout;
    }

    /**
     * constructor for a container that uses a grid layout of the given size and a growth policy of true
     * @param xElts      nr of elements in width
     * @param yElts      nr of elements in height
     * @param growPolicy
     */
    public SContainer(int xElts, int yElts, boolean growPolicy) {
        this(new GridLayoutManager(xElts, yElts));
        setGrowthPolicy(growPolicy, growPolicy);
    }

    /**
     * a wrapper for a target component, to have it behave as a container with one value being itself
     * @param target the component to wrap
     * @return the target as a container object
     */
    public static SContainer singleton(SComponent target) {
        final SContainer c = new SContainer(new SingleElementLayout()) {
            @Override
            public void draw(SFrameLookAndFeel design, Vector2ic offset) {
                drawChildren(design, offset);
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
                Vector2i scPos = new Vector2i(component.position).add(offset);
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
        if (!layoutIsValid()) layout.recalculateProperties();
        ComponentBorder borderSize = getLayoutBorder();
        return layout.getMinimumWidth() + borderSize.left + borderSize.right;
    }

    @Override
    public int minHeight() {
        if (!layoutIsValid()) layout.recalculateProperties();
        ComponentBorder borderSize = getLayoutBorder();
        return layout.getMinimumHeight() + borderSize.top + borderSize.bottom;
    }

    @Override
    public void doValidateLayout() {
        // first restructure this container
        layout.recalculateProperties();

        Vector2i layoutPos = new Vector2i();
        Vector2i layoutDim = new Vector2i(dimensions);
        ComponentBorder border = getLayoutBorder();
        border.reduce(layoutPos, layoutDim);

        layout.placeComponents(layoutPos, layoutDim);

        // then restructure the children
        for (SComponent child : children()) {
            child.validateLayout();
        }

        super.doValidateLayout();
    }

    /**
     * Gives the desired border sizes for this container
     */
    protected ComponentBorder getLayoutBorder() {
        return new ComponentBorder(4);
    }
}
