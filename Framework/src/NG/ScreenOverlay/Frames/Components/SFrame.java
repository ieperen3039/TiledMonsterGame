package NG.ScreenOverlay.Frames.Components;

import NG.ScreenOverlay.Frames.GUIManager;
import NG.ScreenOverlay.Frames.LayoutManagers.SingleElementLayout;
import NG.ScreenOverlay.Frames.SFrameLookAndFeel;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector4i;
import org.joml.Vector4ic;

/**
 * A Frame object similar to {@link javax.swing.JFrame} objects. The {@link #setMainPanel(SContainer)} can be used for
 * full control over the contents of the SFrame.
 * @author Geert van Ieperen. Created on 20-9-2018.
 */
public class SFrame extends SContainer {
    public static final int INNER_BORDER = 4;
    public static final int OUTER_BORDER = 4;

    public static final int FRAME_TITLE_BAR_SIZE = 50;
    private final String title;
    protected GUIManager guiManager;
    private boolean minimized;
    private SContainer upperBar;
    private boolean isDisposed = false;

    /**
     * Creates a SFrame with the given title, width and height
     * @param title the title of the new frame
     * @see SPanel
     */
    public SFrame(String title, int width, int height) {
        this(title, width, height, true);
    }

    /**
     * Creates a SFrame with the given title, width and height. Moving, minimizing and closing by the user is disabled
     * for this frame.
     * @param title the title of the new frame
     * @see SPanel
     */
    public SFrame(String title, int width, int height, boolean manipulable) {
        super(new SingleElementLayout(), false, false);
        this.title = title;

        upperBar = manipulable ? makeUpperBar(title) : SContainer.singleton(new STextArea(title, FRAME_TITLE_BAR_SIZE, true));
        upperBar.setParent(this);

        setMainPanel(new SPanel());
        setSize(width, height);
    }

    /**
     * Creates a SFrame with the given title and minimum size
     * @param title the title of the new frame
     * @see SPanel
     */
    public SFrame(String title) {
        this(title, 0, 0);
    }

    private SPanel makeUpperBar(String frameTitle) {
        SPanel upperBar = new SPanel(0, FRAME_TITLE_BAR_SIZE, 5, 1, true, false);
        SComponent exit = new SCloseButton(this);
        upperBar.add(exit, new Vector2i(4, 0));
        SComponent minimize = new SButton("M", () -> setMinimized(true), FRAME_TITLE_BAR_SIZE, FRAME_TITLE_BAR_SIZE);
        upperBar.add(minimize, new Vector2i(3, 0));
        SExtendedTextArea title = new SExtendedTextArea(frameTitle, 20, true);
        title.setDragListener(this::addToPosition);
        upperBar.add(title, new Vector2i(1, 0));

        return upperBar;
    }

    /**
     * sets the area below the title bar to contain the given component. The size of this component is determined by
     * this frame.
     * @param comp the new middle component
     */
    public void setMainPanel(SContainer comp) {
        super.add(comp, null); // single element layout
        comp.setPosition(0, upperBar.getHeight());
    }

    /**
     * This method should not be used. The main panel should be modified, which can be set with {@link
     * #setMainPanel(SContainer)}
     * @throws UnsupportedOperationException always
     * @deprecated this should not be used
     */
    @Deprecated
    @Override
    public void add(SComponent comp, Object prop) {
        throw new UnsupportedOperationException("Tried adding components to a SFrame, which is illegal");
    }

    /**
     * sets the size of this frame to the minimum as a call to {@code setSize(minWidth(), minHeight())}
     */
    public void pack() {
        setSize(minWidth(), minHeight());
    }

    public void setMinimized(boolean minimized) {
        this.minimized = minimized;
    }

    public boolean isMinimized() {
        return minimized;
    }

    /**
     * requests focus of the guiManager.
     * @throws NullPointerException if no frame-manager has been set
     */
    public void requestFocus() {
        guiManager.focus(this);
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        if (!isVisible()) return;
        if (minimized) {
            // todo minimized panel
//            design.drawMinimized();

        } else {
            // take offset into account for consistency.
            design.drawRectangle(screenPosition, dimensions);
            upperBar.draw(design, screenPosition);
            drawChildren(design, screenPosition);
        }
    }

    @Override
    public int minWidth() {
        return Math.max(super.minWidth(), upperBar.minWidth());
    }

    @Override
    public int minHeight() {
        return super.minHeight() + upperBar.minHeight();
    }

    @Override
    public String toString() {
        return "SFrame (" + title + ")";
    }

    @Override
    public SComponent getComponentAt(int xRel, int yRel) {
        if (upperBar.contains(xRel, yRel)) {
            return upperBar.getComponentAt(xRel, yRel);
        }
        return super.getComponentAt(xRel, yRel);
    }

    @Override
    public Vector2ic getScreenPosition() {
        return getPosition();
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        upperBar.setSize(getWidth(), FRAME_TITLE_BAR_SIZE);
        invalidateLayout();
    }

    @Override
    public void addToSize(int xDelta, int yDelta) {
        upperBar.setSize(getWidth(), FRAME_TITLE_BAR_SIZE);
        super.addToSize(xDelta, yDelta);
    }

    @Override
    public void validateLayout() {
        if (layoutIsValid()) return;
        dimensions.x = Math.max(dimensions.x, minWidth());
        dimensions.y = Math.max(dimensions.y, minHeight());

        upperBar.setSize(getWidth(), FRAME_TITLE_BAR_SIZE);
        upperBar.validateLayout();
        super.validateLayout();
    }

    @Override
    protected Vector4ic getBorderSize() {
        return new Vector4i(INNER_BORDER, INNER_BORDER + upperBar.getHeight(), INNER_BORDER, INNER_BORDER);
    }

    /**
     * removes this component and release any resources used
     */
    public void dispose() {
        this.setVisible(false);
        isDisposed = true;
    }

    public void setManager(GUIManager frameManager) {
        this.guiManager = frameManager;
    }

    /**
     * @return true if {@link #dispose()} has been successfully called
     */
    public boolean isDisposed() {
        return isDisposed;
    }

    @Override
    public boolean wantVerticalGrow() {
        return false;
    }

    @Override
    public boolean wantHorizontalGrow() {
        return false;
    }
}
