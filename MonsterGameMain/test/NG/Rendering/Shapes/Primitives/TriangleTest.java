package NG.Rendering.Shapes.Primitives;

import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Geert van Ieperen created on 27-12-2017.
 */
public class TriangleTest extends PlaneTest {
    private Vector3f from;
    private Vector3f to;

    @Before
    public void setUp() {
        Vector3f a = new Vector3f(-10, 10, 0);
        Vector3f b = new Vector3f(10, -10, 0);
        Vector3f c = new Vector3f(10, 10, 0);
        instance = new Triangle(a, b, c, Vectors.Z);
    }

    @Test
    public void orthogonalPositive() {
        from = new Vector3f(1, 1, 4);
        to = new Vector3f(1, 1, 1);
        testIntersect(from, to, null);
    }

    @Test
    public void horizontalPositive() {
        from = new Vector3f(2, 2, 3);
        to = new Vector3f(4, 4, 3);
        testIntersect(from, to, null);
    }

    @Test
    public void horizontalNegative() {
        from = new Vector3f(2, 2, -3);
        to = new Vector3f(4, 4, -3);
        testIntersect(from, to, null);
    }

    @Test
    public void angledPositive() {
        from = new Vector3f(2, 2, 3);
        to = new Vector3f(1, 1, -1);
        testIntersect(from, to, new Vector3f(1.25f, 1.25f, 0));
    }

    @Test
    public void cutUnderAngle() {
        from = new Vector3f(0, 0, 3);
        to = new Vector3f(4, 4, -1);
        testIntersect(from, to, new Vector3f(3, 3, 0));
    }

    @Test
    public void cutOnZeroZeroZero() {
        from = new Vector3f(2, 2, 2);
        to = new Vector3f(-2, -2, -2);
        testIntersect(from, to, new Vector3f(0, 0, 0));
    }

    @Test
    public void cutUnderAngleInverse() {
        from = new Vector3f(0, 0, -3);
        to = new Vector3f(4, 4, 1);
        testIntersect(from, to, null);
    }

    @Test
    public void touchEnd() {
        from = new Vector3f(1, 1, 1);
        to = new Vector3f(0, 1, 0);
        testIntersect(from, to, new Vector3f(0, 1, 0));
    }

    @Test
    public void touchBegin() {
        from = new Vector3f(1, 1, 0);
        to = new Vector3f(-2, 2, 1);
        testIntersect(from, to, null);
    }

    @Test
    public void cutFromTouch() {
        from = new Vector3f(1, 1, 0);
        to = new Vector3f(-2, 2, -3);
        testIntersect(from, to, new Vector3f(1, 1, 0));
    }

    @Test
    public void upToTouch() {
        from = new Vector3f(1, -1, -1);
        to = new Vector3f(1, 2, 0);
        testIntersect(from, to, null);
    }

    @Test
    public void nullVector() {
        from = new Vector3f(1, 2, 3);
        to = new Vector3f(1, 2, 3);
        testIntersect(from, to, null);
    }

    @Test
    public void asidePlane() {
        from = new Vector3f(11, 1, 1);
        to = new Vector3f(11, -1, -1);
        testIntersect(from, to, null);
    }

    @Test
    public void diagonalHoverAsidePlane() {
        from = new Vector3f(0, 0, 5);
        to = new Vector3f(15, 15, -1);
        testIntersect(from, to, null);
    }

    @Test
    public void rakeEdge() {
        from = new Vector3f(11, 11, 1);
        to = new Vector3f(9, 9, -1);
        testIntersect(from, to, new Vector3f(10, 10, 0));
    }
}
