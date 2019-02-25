package NG.ScreenOverlay.Frames.Components;

import NG.ScreenOverlay.Frames.SFrameLookAndFeel;
import org.joml.Vector2ic;

import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 24-2-2019.
 */
public class SNamedValue extends STextArea {
    private final Supplier<Number> producer;

    public SNamedValue(String name, Supplier<Number> producer, int minHeight) {
        super(name, minHeight);
        this.producer = producer;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        design.drawText(screenPosition, dimensions, text + ":", textType, SFrameLookAndFeel.Alignment.LEFT);
        design.drawText(screenPosition, dimensions, String.valueOf(producer.get()), textType, SFrameLookAndFeel.Alignment.RIGHT);
    }

    @Override
    public String getText() {
        return text + ": " + producer.get();
    }
}
