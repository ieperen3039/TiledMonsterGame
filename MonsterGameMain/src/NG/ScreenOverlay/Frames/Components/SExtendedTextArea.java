package NG.ScreenOverlay.Frames.Components;

import NG.InputHandling.MouseMoveListener;
import NG.InputHandling.MouseRelativeClickListener;
import NG.InputHandling.MouseReleaseListener;
import NG.ScreenOverlay.NGFonts;

/**
 * @author Geert van Ieperen. Created on 25-9-2018.
 */
public class SExtendedTextArea extends STextArea
        implements MouseRelativeClickListener, MouseMoveListener, MouseReleaseListener {
    private MouseMoveListener dragListener;
    private MouseRelativeClickListener clickListener;
    private MouseReleaseListener releaseListener;

    public SExtendedTextArea(
            String frameTitle, int minHeight, int minWidth, boolean doGrowInWidth, NGFonts.TextType textType
    ) {
        super(frameTitle, minHeight, minWidth, doGrowInWidth, textType);
    }

    public SExtendedTextArea(STextArea source) {
        this(source.getText(), source.minWidth(), source.minWidth(), source.wantHorizontalGrow(), source.textType);
    }

    public SExtendedTextArea(String text, int minHeight, boolean doGrowInWidth) {
        super(text, minHeight);
    }

    @Override
    public void onClick(int button, int xRel, int yRel) {
        if (clickListener == null) return;
        clickListener.onClick(button, xRel, yRel);
    }

    @Override
    public void mouseMoved(int xDelta, int yDelta) {
        if (dragListener == null) return;
        dragListener.mouseMoved(xDelta, yDelta);
    }

    @Override
    public void onRelease(int button, int xSc, int ySc) {
        if (releaseListener == null) return;
        releaseListener.onRelease(button, xSc, ySc);
    }

    public void setDragListener(MouseMoveListener dragListener) {
        this.dragListener = dragListener;
    }

    public void setClickListener(MouseRelativeClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setReleaseListener(MouseReleaseListener releaseListener) {
        this.releaseListener = releaseListener;
    }
}
