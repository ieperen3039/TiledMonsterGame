package NG.Rendering.Shapes.Primitives;

import NG.Entities.Entity;
import NG.Tools.Vectors;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 27-12-2018.
 */
public class Collision implements Comparable<Collision> {
    private final float scalar;
    private final Vector3fc normal;
    private final Vector3fc hitPos;
    private Entity entity;

    public Collision(float scalar, Vector3fc normal, Vector3fc hitPos) {
        this.scalar = scalar;
        this.normal = normal;
        this.hitPos = hitPos;
    }

    public Collision(Vector3fc hitPos) {
        this(1, Vectors.Z, hitPos);
    }

    /**
     * @param c another collision
     * @return a positive integer if this.scalar is more than c.scalar, a negative integer if this.scalar is less than
     * c.scalar, or 0 if they are equal
     */
    @Override
    public int compareTo(Collision c) {
        return (c == null) ? 1 : Float.compare(scalar, c.scalar);
    }

    /**
     * @return the position of contact described by this collision in world-space
     */
    public Vector3fc hitPosition() {
        return hitPos;
    }

    /**
     * @return the normalized normal of contact of the receiving side of the collision
     */
    public Vector3fc normal() {
        return normal;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public float getScalar() {
        return scalar;
    }
}
