package NG.ScreenOverlay.Frames.Components;

import NG.ScreenOverlay.Frames.SFrameLookAndFeel;
import NG.ScreenOverlay.NGFonts;
import org.joml.Vector2ic;

/**
 * @author Geert van Ieperen. Created on 22-9-2018.
 */
public class STextArea extends SComponent {
    public static final int LETTER_WIDTH = 15;
    protected final NGFonts.TextType textType;
    protected String text;

    private final boolean doGrowInWidth;
    private int height;

    public STextArea(String text, int minHeight, boolean doGrowInWidth) {
        this.text = text;
        this.height = minHeight;
        this.doGrowInWidth = doGrowInWidth;
        textType = NGFonts.TextType.REGULAR;
    }

    @Override
    public int minWidth() {
        return text.length() * LETTER_WIDTH;
    }

    @Override
    public int minHeight() {
        return height;
    }

    @Override
    public boolean wantHorizontalGrow() {
        return doGrowInWidth;
    }

    @Override
    public boolean wantVerticalGrow() {
        return false;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        design.drawText(screenPosition, dimensions, text, textType, true);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        int end = Math.min(text.length(), 30);
        return this.getClass().getSimpleName() + " (" + text.substring(0, end) + ")";
    }
}
