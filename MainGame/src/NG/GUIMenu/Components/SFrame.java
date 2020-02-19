package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.GUIMenu.LayoutManagers.SingleElementLayout;
import NG.GUIMenu.NGFonts;
import org.joml.Vector2i;
import org.joml.Vector2ic;

/**
 * A Frame object similar to {@link javax.swing.JFrame} objects. The {@link #setMainPanel(SComponent)} can be used to
 * control over the contents of the SFrame.
 * @author Geert van Ieperen. Created on 20-9-2018.
 */
public class SFrame extends SComponent {
    public static final int FRAME_TITLE_BAR_SIZE = 50;

    private final String title;
    private final SPanel contents;
    private final SContainer body;

    private boolean isDisposed = false;
    private STextArea titleComponent;

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
        this.title = title;

        SContainer upperBar;
        if (manipulable) {
            upperBar = makeUpperBar(title);

        } else {
            titleComponent = new STextArea(title, FRAME_TITLE_BAR_SIZE, 0, true, NGFonts.TextType.TITLE, SFrameLookAndFeel.Alignment.CENTER_TOP);
            upperBar = new SPanel(new SingleElementLayout(), true);
            upperBar.add(titleComponent, null);
        }
        body = SContainer.singleton(new SFiller());

        contents = new SPanel(0, 0, 1, 2, true, true);
        contents.setParent(this);
        contents.setBorderVisible(false);
        contents.add(upperBar, new Vector2i(0, 0));
        contents.add(body, new Vector2i(0, 1));

        setSize(width, height);
        setGrowthPolicy(false, false);
    }

    /**
     * Creates a SFrame with the given title and minimum size
     * @param title the title of the new frame
     * @see SPanel
     */
    public SFrame(String title) {
        this(title, 0, 0);
    }

    public SFrame(String title, SComponent mainPanel) {
        this(title);
        setMainPanel(mainPanel);
        pack();
    }

    public void setTitle(String title) {
        titleComponent.setText(title);
    }

    private SPanel makeUpperBar(String frameTitle) {
        SExtendedTextArea title = new SExtendedTextArea(
                frameTitle, FRAME_TITLE_BAR_SIZE, 0, true,
                NGFonts.TextType.TITLE, SFrameLookAndFeel.Alignment.CENTER
        );
        titleComponent = title;
        title.setDragListener(this::addToPosition);

        SPanel bar = SPanel.row(
                title, new SCloseButton(this)
        );
        bar.setBorderVisible(true);

        return bar;
    }

    /**
     * sets the area below the title bar to contain the given component. The size of this component is determined by
     * this frame.
     * @param comp the new middle component
     * @return this
     */
    public SFrame setMainPanel(SComponent comp) {
        body.add(comp, null);
        return this;
    }

    /**
     * sets the size of this frame to the minimum as a call to {@code setSize(minWidth(), minHeight())}
     */
    public void pack() {
        setSize(minWidth(), minHeight());
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        if (!isVisible()) return;
        design.draw(SFrameLookAndFeel.UIComponent.PANEL, screenPosition, getSize());
        contents.draw(design, screenPosition);
    }

    @Override
    public int minWidth() {
        return contents.minWidth();
    }

    @Override
    public int minHeight() {
        return contents.minHeight();
    }

    @Override
    public String toString() {
        return "SFrame (" + title + ")";
    }

    @Override
    public SComponent getComponentAt(int xRel, int yRel) {
        return contents.getComponentAt(xRel, yRel);
    }

    @Override
    public Vector2i getScreenPosition() {
        return new Vector2i(getPosition());
    }

    @Override
    public void doValidateLayout() {
        contents.doValidateLayout();
        super.doValidateLayout();
    }

    /**
     * removes this component and release any resources used
     */
    public void dispose() {
        this.setVisible(false);
        isDisposed = true;
    }

    /**
     * @return true if {@link #dispose()} has been successfully called
     */
    public boolean isDisposed() {
        return isDisposed;
    }
}
