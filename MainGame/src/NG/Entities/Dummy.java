package NG.Entities;

import NG.CollisionDetection.BoundingBox;
import NG.GameMap.GameMap;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Collections;
import java.util.List;

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

    public BoundingBox getHitbox() {
        return new BoundingBox(0, 0, 0, 0, 0, 0);
    }

    public float getIntersection(Vector3fc origin, Vector3fc direction, float gameTime) {
        return 1;
    }

    public List<Vector3f> getShapePoints(List<Vector3f> dest, float gameTime) {
        return Collections.emptyList();
    }

    public void collideWith(Entity other, float collisionTime) {

    }

    @Override
    public void collideWith(GameMap map, float collisionTime) {

    }
}
