package NG.GUIMenu.Rendering;

import NG.Mods.InitialisationMod;
import org.joml.Vector2ic;

/**
 * a stateless mapping from abstract descriptions to drawings in NanoVG
 * @author Geert van Ieperen. Created on 20-9-2018.
 */
public interface SFrameLookAndFeel extends InitialisationMod {
    /**
     * Draw the given element on the given position
     * @param type the type of element
     * @param pos  the position of the upper left corner of this element in pixels
     * @param dim  the (width, height) of the button in pixels
     */
    void draw(UIComponent type, Vector2ic pos, Vector2ic dim);

    void drawText(
            Vector2ic pos, Vector2ic dim, String text, NGFonts.TextType type, Alignment align
    );

    /**
     * sets the LF to draw with the specified painter
     * @param painter a new, fresh Painter instance
     */
    void setPainter(NVGOverlay.Painter painter);

    /**
     * @return the used painter instance
     */
    NVGOverlay.Painter getPainter();

    enum Alignment {
        LEFT, CENTER, RIGHT,
        CENTER_TOP,
    }

    enum UIComponent {
        /** a simple button, either held down or not held down */
        BUTTON_PRESSED, BUTTON_ACTIVE, BUTTON_INACTIVE,
        /** draw a button with an image on it. The image should be scaled uniformly to fit the button */
        ICON_BUTTON_ACTIVE, ICON_BUTTON_INACTIVE,
        /** The top panel of a dropdown menu. */
        DROP_DOWN_HEAD_CLOSED, DROP_DOWN_HEAD_OPEN,
        /** The background of some elements. */
        DROP_DOWN_OPTION_FIELD, SCROLL_BAR_BACKGROUND, TOOLBAR_BACKGROUND,
        /** The background of a frame */
        PANEL,
        /** the bar on top of a frame carrying the title */
        FRAME_HEADER,
        /** An area with text that hints the user that the text can be changed. */
        INPUT_FIELD,
        /** A marking to indicate that e.g. a textfield is selected. */
        SELECTION,
        /** the drag bar element of a scrollbar */
        SCROLL_BAR_DRAG_ELEMENT,
    }

    /**
     * @param text     any string
     * @param textType the type displayed
     * @return the width of the text displayed in pixels
     */
    int getTextWidth(String text, NGFonts.TextType textType);
}
