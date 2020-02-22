package NG.GUIMenu.Components;

import NG.GUIMenu.FrameManagers.SFrameLookAndFeel;
import NG.GUIMenu.NGFonts;
import NG.InputHandling.KeyPressListener;
import NG.InputHandling.MouseRelativeClickListener;
import org.joml.Vector2ic;

import static NG.GUIMenu.FrameManagers.SFrameLookAndFeel.UIComponent.SELECTION;

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
        design.drawText(screenPosition, getSize(), text, NGFonts.TextType.REGULAR, SFrameLookAndFeel.Alignment.CENTER);
    }

    @Override
    public void keyPressed(int keyCode) {

    }

    @Override
    public void onClick(int button, int xSc, int ySc) {

    }
}
