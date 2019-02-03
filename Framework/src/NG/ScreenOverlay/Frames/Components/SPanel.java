package NG.ScreenOverlay.Frames.Components;

import NG.ScreenOverlay.Frames.LayoutManagers.GridLayoutManager;
import NG.ScreenOverlay.Frames.LayoutManagers.SLayoutManager;
import NG.ScreenOverlay.Frames.SFrameLookAndFeel;
import org.joml.Vector2i;
import org.joml.Vector2ic;

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
    private final int minimumWidth;
    private final int minimumHeight;

    /**
     * creates a panel with the given layout manager
     * @param minimumWidth   minimum width of the panel in pixels
     * @param minimumHeight  minimum height of the panel in pixels
     * @param layoutManager  a new layout manager for this component
     * @param growHorizontal if true, the panel tries to grow in its width
     * @param growVertical   if true, the panel tries to grow in its height
     */
    public SPanel(
            int minimumWidth, int minimumHeight, SLayoutManager layoutManager, boolean growHorizontal,
            boolean growVertical
    ) {
        super(layoutManager, growHorizontal, growVertical);
        this.minimumWidth = minimumWidth;
        this.minimumHeight = minimumHeight;
    }

    /**
     * creates a panel that uses a grid layout with the given number of rows and columns
     * @param minimumWidth   minimum width of the panel in pixels
     * @param minimumHeight  minimum height of the panel in pixels
     * @param rows           number of grid rows in this panel
     * @param cols           number of columns in this panel
     * @param growHorizontal if true, the panel tries to grow in its width
     * @param growVertical   if true, the panel tries to grow in its height
     */
    public SPanel(
            int minimumWidth, int minimumHeight, int cols, int rows, boolean growHorizontal, boolean growVertical
    ) {
        this(minimumWidth, minimumHeight, new GridLayoutManager(cols, rows), growHorizontal, growVertical);
    }

    /**
     * creates a panel with the given layout manager, minimum size of (0, 0)
     * @param layoutManager a new layout manager for this component
     * @param growPolicy    if true, try to grow
     */
    public SPanel(SLayoutManager layoutManager, boolean growPolicy) {
        this(0, 0, layoutManager, growPolicy, growPolicy);
    }

    /**
     * creates a panel that uses a default GridLayout of 3x3 and with minimum size of (0, 0) and a no-growth policy.
     * Objects should be located using one of {@link #NORTH}, {@link #EAST}, {@link #SOUTH}, {@link #WEST}, {@link
     * #NORTHEAST}, {@link #SOUTHEAST}, {@link #MORTHWEST}, {@link #SOUTHWEST}, {@link #MIDDLE}
     */
    public SPanel() {
        super(new GridLayoutManager(3, 3), false, false);
        this.minimumWidth = 0;
        this.minimumHeight = 0;
    }

    /**
     * creates a panel that uses no layout and with given minimum size
     * @param minimumWidth  minimum width of the panel in pixels
     * @param minimumHeight minimum height of the panel in pixels
     * @param growPolicy    if true, this panel tries to grow in each direction
     */
    public SPanel(int minimumWidth, int minimumHeight, boolean growPolicy) {
        super(new GridLayoutManager(1, 1), growPolicy, growPolicy);
        this.minimumWidth = minimumWidth;
        this.minimumHeight = minimumHeight;
    }

    /**
     * the basic panel for holding components. Growth policy is false, and minimum size is (0, 0).
     * @param cols number of components
     * @param rows number of rows
     */
    public SPanel(int cols, int rows) {
        this(0, 0, cols, rows, false, false);
    }

    @Override
    public int minWidth() {
        return Math.max(minimumWidth, super.minWidth());
    }

    @Override
    public int minHeight() {
        return Math.max(minimumHeight, super.minHeight());
    }

    @Override
    public void draw(SFrameLookAndFeel lookFeel, Vector2ic screenPosition) {
        assert getWidth() > 0 && getHeight() > 0 :
                String.format("Non-positive dimensions of %s: width = %d, height = %d", this, getWidth(), getHeight());

        lookFeel.drawRectangle(screenPosition, dimensions);
        drawChildren(lookFeel, screenPosition);
    }
}
