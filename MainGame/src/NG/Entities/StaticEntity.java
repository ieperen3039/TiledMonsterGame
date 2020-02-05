package NG.Entities;

import NG.GameMap.GameMap;
import org.joml.Vector3f;

/**
 * an entity that sits still and doesn't move
 * @author Geert van Ieperen created on 26-7-2019.
 */
public abstract class StaticEntity implements Entity {
    private boolean isDisposed = false;

    @Override
    public void update(float gameTime) {
    }

    @Override
    public void collideWith(Entity other, float collisionTime) {
        // play sound?
    }

    @Override
    public void collideWith(GameMap map, float collisionTime) {

    }

    @Override
    public Vector3f getPositionAt(float currentTime) {
        return new Vector3f();
    }

    @Override
    public void dispose() {
        isDisposed = true;
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }
}
