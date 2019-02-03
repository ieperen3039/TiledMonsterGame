package NG.ScreenOverlay.Frames.Components;

import NG.ActionHandling.KeyPressListener;
import NG.ActionHandling.MouseRelativeClickListener;
import NG.ScreenOverlay.Frames.SFrameLookAndFeel;
import org.joml.Vector2ic;

/**
 * @author Geert van Ieperen. Created on 5-10-2018.
 */
public class STextInput extends STextArea implements KeyPressListener, MouseRelativeClickListener {
    public STextInput(int minHeight, boolean doGrowInWidth) {
        super("", minHeight, doGrowInWidth);
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        design.drawSelection(screenPosition, dimensions);
        design.drawText(screenPosition, dimensions, text, textType, false);
    }

    @Override
    public void keyPressed(int keyCode) {

    }

    @Override
    public void onClick(int button, int xSc, int ySc) {

    }
}
