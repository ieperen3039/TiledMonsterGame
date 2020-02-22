package NG.GUIMenu.Components;

import NG.GUIMenu.FrameManagers.SFrameLookAndFeel;
import NG.GUIMenu.LayoutManagers.GridLayoutManager;
import NG.GUIMenu.LayoutManagers.SLayoutManager;
import NG.GUIMenu.LayoutManagers.SingleElementLayout;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import static NG.GUIMenu.FrameManagers.SFrameLookAndFeel.UIComponent.PANEL;

/**
 * @author Geert van Ieperen. Created on 20-9-2018.
 */
public class SPanel extends SContainer {

    /** when using the default constructor, you can use these values to denote the positions */
    public static final Object NORTH = new Vector2i(1, 0);
    public static final Object EAST = new Vector2i(2, 1);
    public static final Object SOUTH = new Vector2i(1, 2);
    public static final Object WEST = new Vector2i(0, 1);
    public static final Object NORTHEAST = new Vector2i(2, 0);
    public static final Object SOUTHEAST = new Vector2i(2, 2);
    public static final Object MORTHWEST = new Vector2i(0, 0);
    public static final Object SOUTHWEST = new Vector2i(0, 2);
    public static final Object MIDDLE = new Vector2i(1, 1);

    private boolean border = true;

    /**
     * creates a panel with the given layout manager
     * @param layoutManager  a new layout manager for this component
     * @param growHorizontal if true, the panel tries to grow in its width
     * @param growVertical   if true, the panel tries to grow in its height
     * @param drawBorder     if true, this panel itself is drawn
     */
    public SPanel(
            SLayoutManager layoutManager, boolean growHorizontal,
            boolean growVertical,
            boolean drawBorder
    ) {
        super(layoutManager);
        this.border = drawBorder;

        setGrowthPolicy(growHorizontal, growVertical);
    }

    /**
     * creates a panel that uses a grid layout with the given number of rows and columns
     * @param cols           number of columns in this panel
     * @param rows           number of grid rows in this panel
     * @param growHorizontal if true, the panel tries to grow in its width
     * @param growVertical   if true, the panel tries to grow in its height
     */
    public SPanel(
            int cols, int rows, boolean growHorizontal, boolean growVertical
    ) {
        this(
                (cols * rows > 0 ? new GridLayoutManager(cols, rows) : new SingleElementLayout()),
                growHorizontal, growVertical, true);
    }

    /**
     * creates a panel with the given layout manager, minimum size of (0, 0)
     * @param layoutManager a new layout manager for this component
     * @param growPolicy    if true, try to grow
     */
    public SPanel(SLayoutManager layoutManager, boolean growPolicy) {
        this(layoutManager, growPolicy, growPolicy, true);
    }

    /**
     * creates a panel that uses a default GridLayout of 3x3 and with minimum size of (0, 0) and a no-growth policy.
     * Objects should be located using one of
     * {@link #NORTH},
     * {@link #EAST},
     * {@link #SOUTH},
     * {@link #WEST},
     * {@link #NORTHEAST},
     * {@link #SOUTHEAST},
     * {@link #MORTHWEST},
     * {@link #SOUTHWEST},
     * {@link #MIDDLE}
     */
    public SPanel() {
        super(new GridLayoutManager(3, 3));
    }

    /**
     * creates a panel that uses no layout and with given minimum size
     * @param growPolicy if true, this panel tries to grow in each direction
     */
    public SPanel(boolean growPolicy) {
        super(new GridLayoutManager(1, 1));
        setGrowthPolicy(growPolicy, growPolicy);
    }

    /**
     * the basic panel for holding components. Growth policy is false, and minimum size is (0, 0).
     * Use {@link Vector2i} for positioning elements. Note that indices are 0-indexed.
     * @param cols number of components
     * @param rows number of rows
     */
    public SPanel(int cols, int rows) {
        this(cols, rows, false, false);
    }

    /** creates a new panel with the given components on a single column */
    public static SPanel column(SComponent... components) {
        SPanel newPanel = new SPanel(1, components.length);
        newPanel.border = false;

        for (int i = 0; i < components.length; i++) {
            newPanel.add(components[i], new Vector2i(0, i));
        }

        newPanel.setSize(0, 0);
        return newPanel;
    }

    /** creates a new panel with the given components in a row */
    public static SPanel row(SComponent... components) {
        SPanel newPanel = new SPanel(components.length, 1);
        newPanel.border = false;

        for (int i = 0; i < components.length; i++) {
            newPanel.add(components[i], new Vector2i(i, 0));
        }

        newPanel.setSize(0, 0);
        return newPanel;
    }

    @Override
    public SComponent getComponentAt(int xRel, int yRel) {
        SComponent base = super.getComponentAt(xRel, yRel);
        if (!border && base == this) return null;
        return base;
    }

    public SPanel setBorderVisible(boolean doVisible) {
        this.border = doVisible;
        return this;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        assert getWidth() > 0 && getHeight() > 0 :
                String.format("Non-positive dimensions of %s: width = %d, height = %d", this, getWidth(), getHeight());

        if (border) design.draw(PANEL, screenPosition, getSize());
        drawChildren(design, screenPosition);
    }

    @Override
    protected ComponentBorder newLayoutBorder() {
        return border ? super.newLayoutBorder() : new ComponentBorder();
    }
}
