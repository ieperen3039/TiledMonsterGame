package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.GUIMenu.LayoutManagers.LimitedVisibilityLayout;
import org.joml.Vector2i;
import org.joml.Vector2ic;

/**
 * @author Geert van Ieperen created on 13-5-2019.
 */
public class SScrollableList extends SContainer {
    private final SScrollBar scroller;

    public SScrollableList(int nrOfShownElts, SComponent... elements) {
        this(nrOfShownElts, new LimitedVisibilityLayout(nrOfShownElts, 0, true), elements);
    }

    private SScrollableList(
            int nrOfShownElts, LimitedVisibilityLayout layout, SComponent... elements
    ) {
        super(layout);

        for (SComponent element : elements) {
            add(element, null);
        }

        scroller = new SScrollBar(elements.length, nrOfShownElts);
        scroller.addListener(value -> {
            layout.setAsFirstVisible(value);
            invalidateLayout();
        });
    }

    @Override
    public SComponent getComponentAt(int xRel, int yRel) {
        if (scroller.contains(xRel, yRel)) {
            Vector2ic position = scroller.getPosition();
            return scroller.getComponentAt(xRel - position.x(), yRel - position.y());

        } else {
            return super.getComponentAt(xRel, yRel);
        }
    }

    @Override
    public void validateLayout() {
        if (layoutIsValid()) return;

        super.validateLayout();

        ComponentBorder border = getLayoutBorder();
        scroller.setSize(0, getHeight() - border.top - border.bottom);
        scroller.setPosition(getWidth() - border.right, border.top);
        scroller.validateLayout();
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        scroller.draw(design, new Vector2i(screenPosition).add(scroller.position));
        drawChildren(design, screenPosition);
    }

    @Override
    protected ComponentBorder getLayoutBorder() {
        return new ComponentBorder(0, scroller.minWidth(), 0, 0);
    }
}
