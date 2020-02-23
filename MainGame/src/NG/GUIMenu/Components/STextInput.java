package NG.GUIMenu.Components;

import NG.GUIMenu.Rendering.SFrameLookAndFeel;
import NG.InputHandling.KeyPressListener;
import NG.InputHandling.MouseRelativeClickListener;
import org.joml.Vector2ic;

import static NG.GUIMenu.Rendering.SFrameLookAndFeel.UIComponent.SELECTION;

/**
 * @author Geert van Ieperen. Created on 5-10-2018.
 */
public class STextInput extends STextArea implements KeyPressListener, MouseRelativeClickListener {
    public STextInput(int minHeight, boolean doGrowInWidth) {
        super("", minHeight);
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        design.draw(SELECTION, screenPosition, getSize());
        super.draw(design, screenPosition);
    }

    @Override
    public void keyPressed(int keyCode) {

    }

    @Override
    public void onClick(int button, int xSc, int ySc) {

    }
}
