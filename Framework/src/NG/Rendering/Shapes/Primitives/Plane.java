package NG.Rendering.Shapes.Primitives;

import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Arrays;

/**
 * a plane with hitbox, allowing checks whether a line-piece hits the plane. all vectors are in a given frame of
 * reference
 * @author Geert van Ieperen
 */
public abstract class Plane {
    private static final float EPSILON = 1e-3f;
    /**
     * a summation of references to define the bounding box of this plane. often, these refer to the same vectors
     */
    protected float leastX = Float.MAX_VALUE;
    protected float mostX = -Float.MAX_VALUE;
    protected float leastY = Float.MAX_VALUE;
    protected float mostY = -Float.MAX_VALUE;
    protected float leastZ = Float.MAX_VALUE;
    protected float mostZ = -Float.MAX_VALUE;

    /** normalized */
    protected final Vector3fc normal;

    protected final Vector3fc[] boundary;

    /** reserved space for collision detection */
    private Vector3f relativePosition = new Vector3f();
    private Vector3f middle = null;

    /**
     * @param vertices the vertices in counterclockwise order
     * @param normal   the normal vector of the plane (not normalized)
     */
    public Plane(Vector3fc[] vertices, Vector3fc normal) {
        this.normal = new Vector3f(normal).normalize();
        this.boundary = vertices;

        // initialize hitbox
        for (Vector3fc vertex : vertices) {
            if (vertex.x() < leastX) leastX = vertex.x();
            if (vertex.x() > mostX) mostX = vertex.x();
            if (vertex.y() < leastY) leastY = vertex.y();
            if (vertex.y() > mostY) mostY = vertex.y();
            if (vertex.z() < leastZ) leastZ = vertex.z();
            if (vertex.z() > mostZ) mostZ = vertex.z();
        }
    }

    /**
     * given a point on position {@code linePosition} moving in the direction of {@code direction}, calculates the
     * movement it is allowed to do before hitting this plane (template design)
     * @param origin    a position vector on the line in local space
     * @param direction the direction vector of the line in local space
     * @param endPoint  the endpoint of this vector, defined as {@code linePosition.add(direction)} if this vector is
     *                  null, an infinite line is assumed
     * @return {@code null} if it does not hit with direction scalar < 1
     */
    public Collision getCollisionWith(Vector3fc origin, Vector3fc direction, Vector3fc endPoint) {
        final boolean isInfinite = (endPoint == null);

        if (directionIsOpposite(direction)) return null;

        if (!isInfinite && asideHitbox(origin, endPoint)) return null;

        float scalar = hitScalar(origin, direction);
        if (!isInfinite && (scalar > (1.0f + EPSILON))) return null;

        Vector3f hitDir = new Vector3f(direction).mul(scalar);
        Vector3fc hitPos = hitDir.add(origin);
        if (!this.encapsulates(hitPos)) return null;

        return new Collision(scalar, normal, hitPos);
    }

    /**
     * @param direction the direction of a given linepiece
     * @return false if the direction opposes the normal-vector, eg if it could hit the plane
     */
    private boolean directionIsOpposite(Vector3fc direction) {
        return direction.dot(normal) >= -EPSILON;
    }

    /**
     * returns whether two points are completely on one side of the plane this standard implementation uses the
     * predefined hitbox, may be overridden for efficiency
     * @param alpha a point in local space
     * @param beta  another point in local space
     * @return true if both points exceed the extremest vector in either x, y, z, -x, -y or -z direction.
     */
    @SuppressWarnings("SimplifiableIfStatement")
    private boolean asideHitbox(Vector3fc alpha, Vector3fc beta) {
        if (((alpha.x() < leastX) && (beta.x() < leastX)) || ((alpha.x() > mostX) && (beta.x() > mostX))) {
            return true;
        }
        if (((alpha.y() < leastY) && (beta.y() < leastY)) || ((alpha.y() > mostY) && (beta.y() > mostY))) {
            return true;
        }
        return ((alpha.z() < leastZ) && (beta.z() < leastZ)) || ((alpha.z() > mostZ) && (beta.z() > mostZ));
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
     * calculates the new {@param direction}, relative to {@param linePosition} where the given line will hit this plane
     * if this plane was infinite
     * @return Vector D such that (linePosition.add(D)) will give the position of the hitPoint. D lies in the extend,
     * but not necessarily on the plane. D is given by ((p0 - l0)*n) \ (l*n)
     */
    protected float hitScalar(Vector3fc linePosition, Vector3fc direction) {
        relativePosition.set(boundary[0]);

        float upper = relativePosition.sub(linePosition, relativePosition).dot(normal);
        float lower = direction.dot(normal);
        return upper / lower;
    }

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
     * given a ray, determines whether it intersects this plane.
     * @param position  the starting point of the line
     * @param direction the direction vector of the line
     * @return true if this plane intersects with the line extended toward infinity
     */
    public boolean intersectWithRay(Vector3fc position, Vector3fc direction) {
        Vector3fc hitDir = direction.mul(hitScalar(position, direction), new Vector3f());

        if (hitDir.dot(direction) < 0) return false;

        Vector3fc hitPoint = new Vector3f(position).add(hitDir);
        return this.encapsulates(hitPoint);
    }

    /**
     * @return the local average of all border positions
     */
    public Vector3fc getMiddle() {
        if (middle == null) {
            middle = Vectors.zeroVector();
            int i = 0;
            while (i < boundary.length) {
                middle.add(boundary[i]);
                i++;
            }
            middle.div(i);
        }
        return middle;
    }

}

