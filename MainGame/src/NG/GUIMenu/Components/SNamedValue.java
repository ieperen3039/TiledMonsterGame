package NG.GUIMenu.Components;

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
    public String getText() {
        return text + ": " + producer.get();
    }
}
