package NG.GUIMenu.Components;

import NG.GUIMenu.Rendering.NGFonts;
import NG.GUIMenu.Rendering.SFrameLookAndFeel;
import org.joml.Vector2ic;

/**
 * @author Geert van Ieperen. Created on 22-9-2018.
 */
public class STextArea extends SComponent {
    public static final int LETTER_WIDTH = 18;
    private int textWidth;

    protected final NGFonts.TextType textType;
    protected final SFrameLookAndFeel.Alignment alignment;
    protected String text;

    private int height;
    private int specMinWidth;

    public STextArea(
            String text, int minHeight, int minWidth, boolean doGrowInWidth, NGFonts.TextType textType,
            SFrameLookAndFeel.Alignment alignment
    ) {
        assert text != null;
        this.text = text;
        this.height = minHeight;
        this.specMinWidth = minWidth;
        this.textType = textType;
        this.alignment = alignment;
        textWidth = text.length() * LETTER_WIDTH;

        setGrowthPolicy(doGrowInWidth, false);
    }

    public STextArea(String text, int minHeight) {
        this(text, minHeight, 0, true, NGFonts.TextType.REGULAR, SFrameLookAndFeel.Alignment.LEFT);
    }

    @Override
    public int minWidth() {
        return Math.max(textWidth, specMinWidth);
    }

    @Override
    public int minHeight() {
        return height;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        String text = getText();

        int textWidth = design.getTextWidth(text, textType);
        if (this.textWidth != textWidth) {
            this.textWidth = textWidth;
            invalidateLayout();
        }

        design.drawText(screenPosition, getSize(), text, textType, alignment);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        invalidateLayout();
    }

    @Override
    public String toString() {
        String text = getText();
        String substring = text.length() > 25 ? text.substring(0, 20) + "..." : text;
        return this.getClass().getSimpleName() + " (" + substring + ")";
    }
}
