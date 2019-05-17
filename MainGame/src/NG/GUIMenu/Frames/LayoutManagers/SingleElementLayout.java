package NG.GUIMenu.Frames.LayoutManagers;

import NG.GUIMenu.Frames.Components.SComponent;
import org.joml.Vector2ic;

import java.util.Collection;
import java.util.Collections;

/**
 * A layout with only one element. The property class is Void (always null)
 * @author Geert van Ieperen. Created on 29-10-2018.
 */
public class SingleElementLayout implements SLayoutManager {
    private SComponent target;

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
    }

    @Override
    public void placeComponents(Vector2ic position, Vector2ic dimensions) {
        target.setPosition(position);
        target.setSize(dimensions.x(), dimensions.y());
    }

    @Override
    public Collection<SComponent> getComponents() {
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
    public Class<?> getPropertyClass() {
        return null;
    }
}
