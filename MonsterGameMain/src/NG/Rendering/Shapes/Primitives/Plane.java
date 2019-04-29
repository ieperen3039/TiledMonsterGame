package NG.Rendering.Shapes.Primitives;

import NG.Tools.Vectors;
import org.joml.Intersectionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Arrays;

/**
 * a plane with hitbox, allowing checks whether a line-piece hits the plane. all vectors are in a given frame of
 * reference
 * @author Geert van Ieperen
 */
public abstract class Plane {
    private static final float EPSILON = 1e-4f;

    /** normalized */
    protected final Vector3fc normal;
    protected final Vector3fc[] boundary;

    /**
     * @param vertices the vertices in counterclockwise order
     * @param normal   the normal vector of the plane (not normalized)
     */
    public Plane(Vector3fc[] vertices, Vector3fc normal) {
        this.normal = new Vector3f(normal).normalize();
        this.boundary = vertices;
    }

    /**
     * given a point on position {@code origin} and a direction of {@code direction}, calculates the fraction t such
     * that (origin + direction * t) lies on the plane, or Float.POSITIVE_INFINITY if it does not hit.
     * @param origin    the begin of a line segment
     * @param direction the direction of the line segment
     * @return the scalar t
     */
    public float getIntersectionScalar(Vector3fc origin, Vector3fc direction) {
        Vector3fc point = boundary[0];
        float scalar = Intersectionf.intersectRayPlane(
                origin.x(), origin.y(), origin.z(),
                direction.x(), direction.y(), direction.z(),
                point.x(), point.y(), point.z(),
                normal.x(), normal.y(), normal.z(),
                EPSILON
        );

        if (scalar < 0) return Float.POSITIVE_INFINITY;

        Vector3fc hitPos = new Vector3f(direction).mul(scalar).add(origin);
        return this.encapsulates(hitPos) ? scalar : Float.POSITIVE_INFINITY;

    }

    /**
     * determines whether the given point lies on or within the boundary, given that it lies on the infinite extension
     * of this plane
     * @param hitPos a point on this plane
     * @return true if the point is not outside the boundary of this plane
     * @precondition hitPos lies on the plane of the points of {@code boundary}
     */
    protected abstract boolean encapsulates(Vector3fc hitPos);

    /**
     * @return a stream of the vertices of this object in counterclockwise order
     */
    public Iterable<Vector3fc> getBorder() {
        return Arrays.asList(boundary);
    }

    public Vector3fc getNormal() {
        return normal;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(this.getClass().getSimpleName());
        s.append("{");
        for (Vector3fc posVector : boundary) {
            s.append(posVector);
        }
        s.append("}");
        return s.toString();
    }

    /**
     * @return the local average of all border positions
     */
    public Vector3fc getMiddle() {
        Vector3f middle = Vectors.newZeroVector();
        int i = 0;
        while (i < boundary.length) {
            middle.add(boundary[i]);
            i++;
        }
        middle.div(i);
        return middle;
    }

}

