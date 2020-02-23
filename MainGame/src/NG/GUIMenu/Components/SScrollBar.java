package NG.GUIMenu.Components;

import NG.GUIMenu.Rendering.SFrameLookAndFeel;
import NG.InputHandling.MouseMoveListener;
import NG.InputHandling.MouseReleaseListener;
import NG.Tools.Toolbox;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;

import static NG.GUIMenu.Rendering.SFrameLookAndFeel.UIComponent.SCROLL_BAR_BACKGROUND;
import static NG.GUIMenu.Rendering.SFrameLookAndFeel.UIComponent.SCROLL_BAR_DRAG_ELEMENT;

/**
 * @author Geert van Ieperen created on 13-5-2019.
 */
class SScrollBar extends SComponent {
    private static final int SCROLL_BAR_WIDTH = 50;
    private static final int SCROLL_BUTTON_SIZE = 50;
    private static final int DRAG_BAR_MIN_SIZE = 15;

    private final List<SScrollBarListener> listeners = new ArrayList<>();
    private final SComponent[] elements;

    private final SButton scrollUp;
    private final SButton scrollDown;
    private final SDragBar dragBar;

    private int minimumInd;
    private int maximumInd;
    private int currentInd;
    private float dragBarOffsetFraction;
    private float barSizeFraction = 0.25f;

    /**
     * generates a vertical scrollbar with two one-step arrows on the sides.
     * @param minimum the minimum value this scrollbar can output
     * @param maximum the maximum value this scrollbar can output
     * @param current the output value to start with
     */
    public SScrollBar(int minimum, int maximum, int current) {
        assert maximum > minimum || maximum == 0;
        assert current >= minimum;
        assert current <= maximum;

        this.currentInd = current;
        this.maximumInd = maximum;
        this.minimumInd = minimum;
        this.scrollUp = new SButton("/\\", this::up, SCROLL_BUTTON_SIZE, SCROLL_BUTTON_SIZE);
        this.scrollDown = new SButton("\\/", this::down, SCROLL_BUTTON_SIZE, SCROLL_BUTTON_SIZE);
        this.dragBar = new SDragBar();

        elements = new SComponent[3];
        elements[0] = scrollUp;
        elements[1] = dragBar;
        elements[2] = scrollDown;
        setGrowthPolicy(false, true);
    }

    public SScrollBar(int totalElts, int shownElts) {
        this(0, Math.max(totalElts - shownElts, 0), 0);
        this.barSizeFraction = (totalElts == 0) ? 1 : (float) shownElts / totalElts;
    }

    public void addListener(SScrollBarListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SScrollBarListener leaver) {
        listeners.remove(leaver);
    }

    private void down() {
        currentInd = Math.min(currentInd + 1, maximumInd); // down in direction is up in index
        alignDragBar();
        notifyListeners();
    }

    private void up() {
        currentInd = Math.max(currentInd - 1, minimumInd); // down in direction is up in index
        alignDragBar();
        notifyListeners();
    }

    private void alignDragBar() {
        dragBarOffsetFraction = (maximumInd == 0) ? 0 : (float) (currentInd - minimumInd) / (maximumInd - minimumInd);
        positionDragbar(dragBarOffsetFraction);
    }

    @Override
    public void doValidateLayout() {
        scrollUp.validateLayout();

        positionDragbar(dragBarOffsetFraction);
        dragBar.validateLayout();

        scrollDown.setPosition(0, getHeight() - SCROLL_BUTTON_SIZE);
        scrollDown.validateLayout();
    }

    private void positionDragbar(float fraction) {
        int dragBarSpace = getDragBarSpace();
        if (dragBarSpace > 0) {
            int dragBarHeight = (int) (dragBarSpace * Math.min(1, barSizeFraction));
            dragBar.setSize(SCROLL_BAR_WIDTH, dragBarHeight);

            int dragMaxYPos = getHeight() - SCROLL_BUTTON_SIZE - dragBarHeight;
            int drabBarY = (int) Toolbox.interpolate(SCROLL_BUTTON_SIZE, dragMaxYPos, fraction);
            dragBar.setPosition(0, drabBarY);

        } else {
            dragBar.setVisible(false);
        }
    }

    @Override
    public int minWidth() {
        return SCROLL_BAR_WIDTH;
    }

    @Override
    public int minHeight() {
        return 2 * SCROLL_BUTTON_SIZE + DRAG_BAR_MIN_SIZE;
    }

    @Override
    public SComponent getComponentAt(int xRel, int yRel) {
        validateLayout();
        for (SComponent elt : elements) {
            if (elt.isVisible() && elt.contains(xRel, yRel)) {
                xRel -= elt.getX();
                yRel -= elt.getY();
                return elt.getComponentAt(xRel, yRel);
            }
        }

        return this;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {

        Vector2i pos = new Vector2i(screenPosition).add(0, SCROLL_BUTTON_SIZE);
        Vector2i size = new Vector2i(SCROLL_BAR_WIDTH, getDragBarSpace());
        design.draw(SCROLL_BAR_BACKGROUND, pos, size);

        scrollUp.draw(design, new Vector2i(screenPosition).add(scrollUp.getPosition()));
        if (dragBar.isVisible()) {
            dragBar.draw(design, new Vector2i(screenPosition).add(dragBar.getPosition()));
        }
        scrollDown.draw(design, new Vector2i(screenPosition).add(scrollDown.getPosition()));
    }

    private int getDragBarSpace() {
        return Math.max(getHeight() - 2 * SCROLL_BUTTON_SIZE, 0);
    }

    public int getIndex() {
        return currentInd;
    }

    public void resize(int newTotalElts, int newShownElts) {
        maximumInd = Math.max(newTotalElts - newShownElts, 0);
        this.barSizeFraction = (newTotalElts == 0) ? 1 : (float) newShownElts / newTotalElts;
        positionDragbar(dragBarOffsetFraction);
        invalidateLayout();
    }

    /**
     * @author Geert van Ieperen created on 13-5-2019.
     */
    public class SDragBar extends SComponent implements MouseMoveListener, MouseReleaseListener {

        SDragBar() {
            setGrowthPolicy(true, false);
            setSize(0, 0);
        }

        @Override
        public int minWidth() {
            return SCROLL_BAR_WIDTH;
        }

        @Override
        public int minHeight() {
            return DRAG_BAR_MIN_SIZE;
        }

        @Override
        public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
            design.draw(SCROLL_BAR_DRAG_ELEMENT, screenPosition, getSize());
        }

        @Override
        public void mouseMoved(int xDelta, int yDelta) {
            dragBarOffsetFraction += ((float) yDelta / getDragBarSpace());
            dragBarOffsetFraction = Math.max(0, Math.min(1, dragBarOffsetFraction));
            positionDragbar(dragBarOffsetFraction);

            int newCurrent;
            if (dragBarOffsetFraction == 1) {
                newCurrent = maximumInd;
            } else {
                newCurrent = (int) (dragBarOffsetFraction * (maximumInd - minimumInd + 1)) + minimumInd;
            }

            if (newCurrent != currentInd) {
                currentInd = newCurrent;
                notifyListeners();
            }
        }

        @Override
        public void onRelease(int button, int xSc, int ySc) {
            alignDragBar();
        }
    }

    private void notifyListeners() {
        listeners.forEach(l -> l.onChange(currentInd));
    }

    public interface SScrollBarListener {
        /**
         * @param newState the new number this scrollbar points at
         */
        void onChange(int newState);
    }
}
