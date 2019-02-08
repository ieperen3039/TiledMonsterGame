package NG.ScreenOverlay.Frames.Components;

import NG.ActionHandling.MouseMoveListener;
import NG.ActionHandling.MouseRelativeClickListener;
import NG.ActionHandling.MouseReleaseListener;

/**
 * @author Geert van Ieperen. Created on 25-9-2018.
 */
public class SExtendedTextArea extends STextArea
        implements MouseRelativeClickListener, MouseMoveListener, MouseReleaseListener {
    private MouseMoveListener dragListener;
    private MouseRelativeClickListener clickListener;
    private MouseReleaseListener releaseListener;

    public SExtendedTextArea(String frameTitle, int minHeight, boolean doGrowInWidth) {
        super(frameTitle, minHeight, doGrowInWidth);
    }

    public SExtendedTextArea(STextArea source) {
        this(source.getText(), 20, source.wantHorizontalGrow());
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
