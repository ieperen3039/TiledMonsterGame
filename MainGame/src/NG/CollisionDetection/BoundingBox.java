package NG.CollisionDetection;

import org.joml.*;

import java.util.Iterator;

/**
 * @author Geert van Ieperen created on 17-4-2019.
 */
public class BoundingBox extends AABBf {
    public BoundingBox(AABBf source, Vector3fc displacement) {
        float x = displacement.x();
        float y = displacement.y();
        float z = displacement.z();
        this.minX = source.minX + x;
        this.minY = source.minY + y;
        this.minZ = source.minZ + z;
        this.maxX = source.maxX + x;
        this.maxY = source.maxY + y;
        this.maxZ = source.maxZ + z;
    }

    public BoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public BoundingBox() {
        super();
    }

    /**
     * moves this bounding box by adding the given vector to its components
     * @param displacement a vector describing the movement
     * @return this
     */
    public BoundingBox getMoved(Vector3fc displacement) {
        return new BoundingBox(this, displacement);
    }

    /**
     * calculates the fraction t in the ray equation <i>p(t) = origin + t * dir</i>
     * @param origin the origin of the ray
     * @param dir    the direction of the ray
     * @return fraction t of the nearest intersection, such that 0 <= t, and t = {@link Float#POSITIVE_INFINITY} if the
     * ray does not hit.
     */
    public float intersectRay(Vector3fc origin, Vector3fc dir) {
        Vector2f result = new Vector2f();

        boolean doIntersect = Intersectionf.intersectRayAab(
                origin.x(), origin.y(), origin.z(),
                dir.x(), dir.y(), dir.z(),
                minX, minY, minZ, maxX, maxY, maxZ,
                result
        );

        if (result.x < 0) return 0;
        return doIntersect ? result.x : Float.POSITIVE_INFINITY;
    }

    /**
     * calculates the fraction t such that this can move (thisMove * t) before hitting other.
     * @param thisPos   the offset of this bounding box
     * @param thisMove  the movement of this bounding box
     * @param other     the other bounding box
     * @param otherPos  the offset of the other bounding box
     * @param otherMove the movement of the other bounding box
     * @return fraction t of the first collision of this box, such that 0 <= t <= 1, and t = 1 if the ray does not hit.
     */
    public float relativeCollisionFraction(
            Vector3fc thisPos, Vector3fc thisMove, BoundingBox other, Vector3fc otherPos, Vector3fc otherMove
    ) {
        if (thisMove.equals(otherMove)) return 1; // when moving the same direction (or both zero) they will not hit

        float min = 1; // at most 1
        Vector3fc relativePos = new Vector3f(thisPos).sub(otherPos);
        Vector3fc relativeMove = new Vector3f(thisMove).sub(otherMove);

        for (Vector3f origin : corners()) {
            origin.add(relativePos);

            float f = other.intersectRay(origin, relativeMove);
            if (f < min) min = f;
        }

        return min;
    }

    /**
     * Iterates over the corners of this bounding box using an iterator-local buffer vector
     * @return an iterable source of the corners of this box. Changes of the bounding box are reflected in the values
     * returned by this iterator.
     */
    public Iterable<Vector3f> corners() {
        return () -> new Iterator<>() {
            int i = 0;
            Vector3f buffer = new Vector3f();

            @Override
            public boolean hasNext() {
                return i < 8;
            }

            @Override
            public Vector3f next() {
                switch (i++) {
                    case 0:
                        return buffer.set(minX, minY, minZ);
                    case 1:
                        return buffer.set(maxX, minY, minZ);
                    case 2:
                        return buffer.set(minX, maxY, minZ);
                    case 3:
                        return buffer.set(minX, minY, maxZ);
                    case 4:
                        return buffer.set(maxX, maxY, minZ);
                    case 5:
                        return buffer.set(maxX, minY, maxZ);
                    case 6:
                        return buffer.set(minX, maxY, maxZ);
                    case 7:
                        return buffer.set(maxX, maxY, maxZ);
                    default:
                        throw new IllegalStateException("i = " + --i);
                }
            }
        };
    }
}
