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
     * a rectangle that serves as base for panels
     * @param pos upper left position of the rectangle
     * @param dim dimension of the rectangle
     */
    void drawRectangle(Vector2ic pos, Vector2ic dim);

    /**
     * draws a button with the given text on it. All the text must fit within the button, but no restrictions are given
     * to the size of the text
     * @param pos   upper left position of the button
     * @param dim   dimension of the button
     * @param text  the text displayed on the button
     * @param state true if the button is activated, which can be pressed or toggled on
     */
    void drawButton(Vector2ic pos, Vector2ic dim, String text, boolean state);

    /**
     * draw a button with an image on it. The image should be scaled uniformly to fit the button
     * @param pos   upper left position of the button
     * @param dim   dimension of the button
     * @param icon  a path to the file containing the icon to display
     * @param state true if the button is activated, which can be pressed or toggled on
     * @throws IOException if the file could not be found or accessed
     */
    void drawIconButton(Vector2ic pos, Vector2ic dim, Path icon, boolean state) throws IOException;

    /**
     * Writes the given text within the given bounds. The position and dimension are hard bounds, the size can be
     * adapted
     * @param pos    upper left position of the area where text may occur
     * @param dim    dimensions of the button
     * @param text   the displayed text
     * @param size   the preferred font size of the text
     * @param center if true, the text is placed in the center of the area. If false, it is place in the upper left
     *               corner.
     */
    void drawText(Vector2ic pos, Vector2ic dim, String text, NGFonts.TextType size, boolean center);

    /**
     * draws an area with text that hints the user that the text can be changed. This should include an opaque
     * background and a visual hint when selected.
     * @param pos    upper left position of the area where text may occur
     * @param dim    dimensions of the area
     * @param text   the displayed text
     * @param size   the preferred font size of the text
     * @param cursor the position of the cursor in the text, or -1 if the area is not selected.
     */
    void drawInputField(Vector2ic pos, Vector2ic dim, String text, NGFonts.TextType size, int cursor);

    /**
     * draw a marking to indicate that e.g. a textfield is selected.
     * @param pos upper left position of the selection
     * @param dim dimensions of the selection
     */
    void drawSelection(Vector2ic pos, Vector2ic dim);

    /**
     * Draws the top panel of a dropdown menu. The options of the menu are drawn using drawRectangle and drawText with
     * size {@link NGFonts.TextType#REGULAR}
     * @param pos      top left position of the button
     * @param dim      the dimension of just this single element
     * @param value    the value to be displayed
     * @param isOpened if true, the options are shown below this component
     */
    void drawDropDown(Vector2ic pos, Vector2ic dim, String value, boolean isOpened);

    /**
     * Draw a rectangle on the top of the screen
     * @param height height of the bar in pixels
     */
    void drawToolbar(int height);
}
