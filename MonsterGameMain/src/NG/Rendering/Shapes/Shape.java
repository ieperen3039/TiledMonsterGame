package NG.Rendering.Shapes;

import NG.Rendering.Shapes.Primitives.Collision;
import NG.Rendering.Shapes.Primitives.Plane;
import org.joml.Vector3fc;

import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Geert van Ieperen created on 30-10-2017.
 */
public interface Shape {

    /** returns all planes of this object in no specific order */
    Iterable<? extends Plane> getPlanes();

    /** @return the points of this plane in no specific order */
    Iterable<Vector3fc> getPoints();

    /** @see #getPlanes() */
    default Stream<? extends Plane> getPlaneStream() {
        return StreamSupport.stream(getPlanes().spliterator(), false);
    }

    /** @see #getPoints() */
    default Stream<? extends Vector3fc> getPointStream() {
        return StreamSupport.stream(getPoints().spliterator(), false);
    }

    /**
     * given a point on position {@code linePosition} moving in the direction of {@code direction}, calculates the
     * movement it is allowed to do before hitting this shape
     * @param linePosition a position vector on the line in local space
     * @param direction    the direction vector of the line in local space
     * @param endPoint     the endpoint of this vector, defined as {@code linePosition.add(direction)}
     * @return {@code null} if it does not hit with direction scalar < 1 otherwise, it provides a collision object about
     * the first collision with this shape
     */
    default Collision getCollision(Vector3fc linePosition, Vector3fc direction, Vector3fc endPoint) {
        return getPlaneStream()
                .parallel()
                // find the vector that hits the planes
                .map((plane) -> plane.getCollisionWith(linePosition, direction, endPoint))
                // exclude the vectors that did not hit
                .filter(Objects::nonNull)
                // return the shortest vector
                .min(Collision::compareTo)
                .orElse(null);
    }
}
