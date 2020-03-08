package NG.Entities;

import NG.GameMap.GameMap;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 5-8-2019.
 */
public abstract class Dummy implements Entity {
    private boolean isDisposed = false;

    public void update(float gameTime) {

    }

    public void dispose() {
        isDisposed = true;
    }

    public boolean isDisposed() {
        return isDisposed;
    }

    public float getIntersection(Vector3fc origin, Vector3fc direction, float gameTime) {
        return 1;
    }

    public abstract Vector3f getPositionAt(float gameTime);

    /**
     * process a collision with the map, happening at collisionTime.
     * @param map           the map
     * @param collisionTime the moment of collision
     */
    @Override
    public void collideWith(GameMap map, float collisionTime) {
    }
}
