package NG.ScreenOverlay.Frames;

import NG.Mods.InitialisationMod;
import NG.ScreenOverlay.NGFonts;
import NG.ScreenOverlay.ScreenOverlay;
import org.joml.Vector2ic;

import java.io.IOException;
import java.nio.file.Path;

/**
 * a stateless mapping from abstract descriptions to drawings in NanoVG
 * @author Geert van Ieperen. Created on 20-9-2018.
 */
public interface SFrameLookAndFeel extends InitialisationMod {

    /**
     * sets the LF to draw with the specified painter
     * @param painter a new, fresh Painter instance
     */
    void setPainter(ScreenOverlay.Painter painter);

    /**
     * Draw the given element on the given position
     * @param type the type of element
     * @param pos  the position of the upper left corner of this element in pixels
     * @param dim  the (width, height) of the button in pixels
     */
    void draw(UIComponent type, Vector2ic pos, Vector2ic dim);

    default void drawText(Vector2ic pos, Vector2ic dim, String text) {
        drawText(pos, dim, text, NGFonts.TextType.REGULAR, Alignment.CENTER);
    }

    void drawText(
            Vector2ic pos, Vector2ic dim, String text, NGFonts.TextType type, Alignment align
    );

    /**
     * draw a button with an image on it. The image should be scaled uniformly to fit the button
     * @param pos  upper left position of the button
     * @param dim  dimension of the button
     * @param icon a path to the file containing the icon to display
     * @throws IOException if the file could not be found or accessed
     */
    void drawIcon(Vector2ic pos, Vector2ic dim, Path icon) throws IOException;

    /**
     * Draw a rectangle on the top of the screen with the given height
     * @param height height of the bar in pixels
     */
    void drawToolbar(int height);

    enum Alignment {
        LEFT, CENTER, RIGHT,
        CENTER_TOP,
    }

    enum UIComponent {
        /** a simple button, either held down or not held down */
        BUTTON_ACTIVE, BUTTON_INACTIVE,
        /** draw a button with an image on it. The image should be scaled uniformly to fit the button */
        ICON_BUTTON_ACTIVE, ICON_BUTTON_INACTIVE,
        /** The top panel of a dropdown menu. */
        DROP_DOWN_HEAD_CLOSED, DROP_DOWN_HEAD_OPEN,
        /** The background of the options as visible when a dropdown menu is opened. */
        DROP_DOWN_OPTION_FIELD,
        /** The background of a frame */
        FRAME_BODY,
        /** the bar on top of a frame carrying the title */
        FRAME_HEADER,
        /** An area with text that hints the user that the text can be changed. */
        INPUT_FIELD,
        /** A marking to indicate that e.g. a textfield is selected. */
        SELECTION,
    }
}
