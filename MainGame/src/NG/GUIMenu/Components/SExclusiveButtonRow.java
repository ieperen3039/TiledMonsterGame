package NG.GUIMenu.Components;

import org.joml.Vector2i;
import org.joml.Vector2ic;

/**
 * @author Geert van Ieperen created on 21-2-2020.
 */
public class SExclusiveButtonRow extends SDecorator {
    private SToggleButton selected;

    public SExclusiveButtonRow(boolean horizontal, String... elements) {
        super(horizontal ? new SPanel(elements.length, 1) : new SPanel(1, elements.length));
        Vector2ic delta;
        Vector2i pos = new Vector2i(0, 0);

        delta = horizontal ? new Vector2i(1, 0) : new Vector2i(0, 1);

        for (String element : elements) {
            SToggleButton button = new SToggleButton(element);
            button.addStateChangeListener((s) -> {
                if (s) select(button);
            });

            add(button, pos);
            pos.add(delta);
        }
    }

    private void select(SToggleButton elt) {
        selected.setState(false);
        selected = elt;
    }
}
