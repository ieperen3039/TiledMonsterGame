package NG.Entities;

import NG.GameMap.GameMap;

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
    public void dispose() {
        isDisposed = true;
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

    /**
     * process a collision with the map, happening at collisionTime.
     * @param map           the map
     * @param collisionTime the moment of collision
     */
    @Override
    public void collideWith(GameMap map, float collisionTime) {
    }
}
