package NG.Entities;

import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 5-8-2019.
 */
public abstract class Dummy implements Entity {
    public void update(float gameTime) {
    }

    public float getIntersection(Vector3fc origin, Vector3fc direction, float gameTime) {
        return 1;
    }

    public abstract Vector3f getPositionAt(float gameTime);
}
