package NG.GUIMenu.Components;

import NG.GUIMenu.FrameManagers.SFrameLookAndFeel;
import org.joml.Vector2ic;

import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 24-2-2019.
 */
public class SNamedValue extends STextArea {
    private final Supplier<Object> producer;

    public SNamedValue(String name, Supplier<Object> producer, int minComponentHeight) {
        super(name, minComponentHeight);
        this.producer = producer;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        design.drawText(screenPosition, getSize(), text + ":", textType, SFrameLookAndFeel.Alignment.LEFT);
        design.drawText(screenPosition, getSize(), String.valueOf(producer.get()), textType, SFrameLookAndFeel.Alignment.RIGHT);
    }

    @Override
    public String getText() {
        return text + ": " + producer.get();
    }
}
