package NG.ScreenOverlay.Frames.LayoutManagers;

import NG.ScreenOverlay.Frames.Components.SComponent;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Collections;

/**
 * A layout with only one element
 * @author Geert van Ieperen. Created on 29-10-2018.
 */
public class SingleElementLayout implements SLayoutManager {
    private SComponent target;
    private final Vector2i position = new Vector2i();
    private final Vector2i dimensions = new Vector2i();

    @Override
    public void add(SComponent comp, Object prop) {
        target = comp;
    }

    @Override
    public void remove(SComponent comp) {
        target = null;
    }

    @Override
    public void recalculateProperties() {
        placeComponents();
    }

    @Override
    public void placeComponents() {
        target.setPosition(position);
        target.setSize(dimensions.x(), dimensions.y());
    }

    @Override
    public Iterable<SComponent> getComponents() {
        return Collections.singleton(target);
    }

    @Override
    public int getMinimumWidth() {
        if (target == null) return 0;
        return target.minWidth();
    }

    @Override
    public int getMinimumHeight() {
        if (target == null) return 0;
        return target.minHeight();
    }

    @Override
    public void setDimensions(Vector2ic position, Vector2ic dimensions) {
        this.position.set(position);
        this.dimensions.set(dimensions);
    }

    @Override
    public Class<?> getPropertyClass() {
        return null;
    }
}
