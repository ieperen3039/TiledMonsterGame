package NG.Tools;

import org.joml.*;
import org.junit.Before;
import org.junit.Test;

import java.lang.Math;
import java.text.NumberFormat;
import java.util.Locale;

import static java.lang.Math.abs;
import static org.junit.Assert.fail;

/**
 * @author Geert van Ieperen created on 3-7-2019.
 */
public class Vector3fxTest {

    private static final NumberFormat NI = NumberFormat.getInstance(Locale.US);
    private static final float MAX_ERROR = 0.01f;
    private static final double DOT_REL_ERROR = 0.001;
    private static final double ROT_REL_ERROR = 0.001f;
    private Vector3dc[] testVecs;

    @Before
    public void setUp() throws Exception {
        testVecs = new Vector3dc[]{
                new Vector3d(0, 0, 0),
                new Vector3d(1, 1, 1),
                new Vector3d(-1, 0, 0),
                new Vector3d(-1, -1, -1),
                new Vector3d(0.5, 0, 0),
                new Vector3d(0.5, 0.5, -0.8),
                new Vector3d(1000, 1, 1),
                new Vector3d(1000 + Vector3fx.MIN_FIXPOINT, 1, 1),
                new Vector3d(Vector3fx.MAX_FIXPOINT, 0, 0),
                new Vector3d(Vector3fx.MAX_FIXPOINT, Vector3fx.MAX_FIXPOINT, -Vector3fx.MAX_FIXPOINT),
        };
    }

    @Test // The requirement for validity of all other tests
    public void toVector3d() {
        float x = 0.74f;
        float y = Float.intBitsToFloat(Float.floatToIntBits(1000f) + 1);
        int z = -42;

        Vector3d origin = new Vector3d(x, y, z);
        Vector3fx vec = new Vector3fx(x, y, z);

        if (abs(x - vec.x()) > MAX_ERROR) fail(vec.toString());
        if (abs(y - vec.y()) > MAX_ERROR) fail(vec.toString());
        if (abs(z - vec.z()) > MAX_ERROR) fail(vec.toString());

        if (!vec.equals(new Vector3fx(origin))) fail();

        Vector3d result = vec.toVector3d();
        if (origin.distance(result) > MAX_ERROR) fail(vec.toString());
    }

    @Test
    public void add0() {
        Vector3fc base = new Vector3f(0.074f, Float.intBitsToFloat(Float.floatToIntBits(1000f) + 1), -42);

        for (Vector3dc v : testVecs) {
            Vector3d expected = new Vector3d(v).add(base);
            Vector3d result = new Vector3fx(v).add(base).toVector3d();

            if (expected.get(expected.maxComponent()) > Vector3fx.MAX_FIXPOINT) {
                continue; // overflow on the fixed-point
            }

            double error = expected.distance(result);
            if (error > MAX_ERROR) {
                fail(String.format("v = %s: expected %s but got %s", v, expected.toString(NI), result.toString(NI)));
            }
        }
    }

    @Test
    public void add1() {
        float x = 0.74f;
        float y = Float.intBitsToFloat(Float.floatToIntBits(1000f) + 1);
        int z = -42;

        for (Vector3dc v : testVecs) {
            Vector3d expected = new Vector3d(v).add(x, y, z);
            Vector3d result = new Vector3fx(v).add(x, y, z).toVector3d();

            if (expected.get(expected.maxComponent()) > Vector3fx.MAX_FIXPOINT) {
                continue; // overflow on the fixed-point
            }

            double error = expected.distance(result);
            if (error > MAX_ERROR) {
                fail(String.format("v = %s: expected %s but got %s", v, expected.toString(NI), result.toString(NI)));
            }
        }
    }

    @Test
    public void add2() {
        Vector3fx base = new Vector3fx(0.74f, 1000 + Vector3fx.MIN_FIXPOINT, -42);

        for (Vector3dc v : testVecs) {
            Vector3d expected = new Vector3d(v).add(base.toVector3d());
            Vector3d result = new Vector3fx(v).add(base).toVector3d();

            if (expected.get(expected.maxComponent()) > Vector3fx.MAX_FIXPOINT) {
                continue; // overflow on the fixed-point
            }

            double error = expected.distance(result);
            if (error > MAX_ERROR) {
                fail(String.format("v = %s: expected %s but got %s", v, expected.toString(NI), result.toString(NI)));
            }
        }
    }

    @Test
    public void sub0() {
        Vector3fc base = new Vector3f(0.74f, Float.intBitsToFloat(Float.floatToIntBits(1000f) + 1), -42);

        for (Vector3dc v : testVecs) {
            Vector3d expected = new Vector3d(v).sub(base);
            Vector3d result = new Vector3fx(v).sub(base).toVector3d();

            if (expected.get(expected.maxComponent()) > Vector3fx.MAX_FIXPOINT) {
                continue; // overflow on the fixed-point
            }

            double error = expected.distance(result);
            if (error > MAX_ERROR) {
                fail(String.format("v = %s: expected %s but got %s", v, expected.toString(NI), result.toString(NI)));
            }
        }
    }

    @Test
    public void sub1() {
        float x = 0.74f;
        float y = Float.intBitsToFloat(Float.floatToIntBits(1000f) + 1);
        int z = -42;

        for (Vector3dc v : testVecs) {
            Vector3d expected = new Vector3d(v).sub(x, y, z);
            Vector3d result = new Vector3fx(v).sub(x, y, z).toVector3d();

            if (expected.get(expected.maxComponent()) > Vector3fx.MAX_FIXPOINT) {
                continue; // overflow on the fixed-point
            }

            double error = expected.distance(result);
            if (error > MAX_ERROR) {
                fail(String.format("v = %s: expected %s but got %s", v, expected.toString(NI), result.toString(NI)));
            }
        }
    }

    @Test
    public void sub2() {
        Vector3fx base = new Vector3fx(0.74f, 1000 + Vector3fx.MIN_FIXPOINT, -42);

        for (Vector3dc v : testVecs) {
            Vector3d expected = new Vector3d(v).sub(base.toVector3d());
            Vector3d result = new Vector3fx(v).sub(base).toVector3d();

            if (expected.get(expected.maxComponent()) > Vector3fx.MAX_FIXPOINT) {
                continue; // overflow on the fixed-point
            }

            double error = expected.distance(result);
            if (error > MAX_ERROR) {
                fail(String.format("v = %s: expected %s but got %s", v, expected.toString(NI), result.toString(NI)));
            }
        }
    }


    @Test
    public void mul0() {
        Vector3fc base = new Vector3f(0.74f, Float.intBitsToFloat(Float.floatToIntBits(1000f) + 1), -42);

        for (Vector3dc v : testVecs) {
            Vector3d expected = new Vector3d(v).mul(base);
            Vector3d result = new Vector3fx(v).mul(base).toVector3d();

            if (expected.get(expected.maxComponent()) > Vector3fx.MAX_FIXPOINT) {
                continue; // overflow on the fixed-point
            }

            double error = expected.distance(result);
            if (error > MAX_ERROR) {
                fail(String.format("v = %s: expected %s but got %s", v, expected.toString(NI), result.toString(NI)));
            }
        }
    }

    @Test
    public void mul1() {
        float x = 0.74f;
        float y = Float.intBitsToFloat(Float.floatToIntBits(1000f) + 1);
        int z = -42;

        for (Vector3dc v : testVecs) {
            Vector3d expected = new Vector3d(v).mul(x, y, z);
            Vector3d result = new Vector3fx(v).mul(x, y, z).toVector3d();

            if (expected.get(expected.maxComponent()) > Vector3fx.MAX_FIXPOINT) {
                continue; // overflow on the fixed-point
            }

            double error = expected.distance(result);
            if (error > MAX_ERROR) {
                fail(String.format("v = %s: expected %s but got %s", v, expected.toString(NI), result.toString(NI)));
            }
        }
    }

    @Test
    public void mul2() {
        Vector3fx base = new Vector3fx(0.74f, 1000 + Vector3fx.MIN_FIXPOINT, -42);

        for (Vector3dc v : testVecs) {
            Vector3d expected = new Vector3d(v).mul(base.toVector3d());
            Vector3d result = new Vector3fx(v).mul(base).toVector3d();

            if (expected.get(expected.maxComponent()) > Vector3fx.MAX_FIXPOINT) {
                continue; // overflow on the fixed-point
            }

            double error = expected.distance(result);
            if (error > MAX_ERROR) {
                fail(String.format("v = %s: expected %s but got %s", v, expected.toString(NI), result.toString(NI)));
            }
        }
    }

    @Test
    public void div0() {
        Vector3fc base = new Vector3f(0.74f, Float.intBitsToFloat(Float.floatToIntBits(1000f) + 1), -42);

        for (Vector3dc v : testVecs) {
            Vector3d expected = new Vector3d(v).div(base);
            Vector3d result = new Vector3fx(v).div(base).toVector3d();

            if (expected.get(expected.maxComponent()) > Vector3fx.MAX_FIXPOINT) {
                continue; // overflow on the fixed-point
            }

            double error = expected.distance(result);
            if (error > MAX_ERROR) {
                fail(String.format("v = %s: expected %s but got %s", v, expected.toString(NI), result.toString(NI)));
            }
        }
    }

    @Test
    public void div1() {
        float x = 0.74f;
        float y = Float.intBitsToFloat(Float.floatToIntBits(1000f) + 1);
        int z = -42;

        for (Vector3dc v : testVecs) {
            Vector3d expected = new Vector3d(v).div(x, y, z);
            Vector3d result = new Vector3fx(v).div(x, y, z).toVector3d();

            if (expected.get(expected.maxComponent()) > Vector3fx.MAX_FIXPOINT) {
                continue; // overflow on the fixed-point
            }

            double error = expected.distance(result);
            if (error > MAX_ERROR) {
                fail(String.format("v = %s: expected %s but got %s", v, expected.toString(NI), result.toString(NI)));
            }
        }
    }

    @Test
    public void div2() {
        Vector3fx base = new Vector3fx(0.74f, 1000 + Vector3fx.MIN_FIXPOINT, -42);

        for (Vector3dc v : testVecs) {
            Vector3d expected = new Vector3d(v).div(base.toVector3d());
            Vector3d result = new Vector3fx(v).div(base).toVector3d();

            if (expected.get(expected.maxComponent()) > Vector3fx.MAX_FIXPOINT) {
                continue; // overflow on the fixed-point
            }

            double error = expected.distance(result);
            if (error > MAX_ERROR) {
                fail(String.format("v = %s: expected %s but got %s", v, expected.toString(NI), result.toString(NI)));
            }
        }
    }

    @Test
    public void mulPosition() {
        Matrix4fc base = new Matrix4f().setPerspective(1, 0.7f, 0.1f, 1000f);

        for (Vector3dc v : testVecs) {
            Vector3d expected = new Vector3d(v).mulPosition(base);
            Vector3d result = new Vector3fx(v).mulPosition(base).toVector3d();

            if (expected.get(expected.maxComponent()) > Vector3fx.MAX_FIXPOINT) {
                continue; // overflow on the fixed-point
            }

            double error = expected.distance(result);
            if (error > MAX_ERROR) {
                fail(String.format("v = %s: expected %s but got %s", v, expected.toString(NI), result.toString(NI)));
            }
        }
    }

    @Test
    public void mulDirection() {
        Matrix4fc base = new Matrix4f().setPerspective(1, 0.7f, 0.001f, 1000f);

        for (Vector3dc v : testVecs) {
            Vector3d expected = new Vector3d(v).mulDirection(base);
            Vector3d result = new Vector3fx(v).mulDirection(base).toVector3d();

            if (expected.get(expected.maxComponent()) > Vector3fx.MAX_FIXPOINT) {
                continue; // overflow on the fixed-point
            }

            double error = expected.distance(result);
            if (error > MAX_ERROR) {
                fail(String.format("v = %s: expected %s but got %s", v, expected.toString(NI), result.toString(NI)));
            }
        }
    }

    @Test
    public void rotateAxis() {
        Vector3f base = new Vector3f(0.74f, 1000 + Vector3fx.MIN_FIXPOINT, -42).normalize();
        float x = base.x;
        float y = base.y;
        int z = (int) base.z;

        for (Vector3dc v : testVecs) {
            for (float angle : new float[]{1, -0.01f, 6.29f}) {
                Vector3d expected = new Vector3d(v).rotateAxis(angle, x, y, z);
                Vector3d result = new Vector3fx(v).rotateAxis(angle, x, y, z).toVector3d();

                if (expected.length() > Vector3fx.MAX_FIXPOINT) {
                    continue; // overflow on the fixed-point
                }

                double error = expected.distance(result);
                if (error / expected.length() > ROT_REL_ERROR) {
                    fail(String.format("v = %s, a = %s: expected %s but got %s", v, angle, expected.toString(NI), result
                            .toString(NI)));
                }
            }
        }
    }

    @Test
    public void length() {
        for (Vector3dc v : testVecs) {
            Vector3fx fixed = new Vector3fx(v);
            float result = fixed.length();
            float expected = (float) v.length();

            if (Float.isNaN(result)) fail("v = " + v + ": result == NaN");

            double error = Math.abs(expected - result);
            if (error > MAX_ERROR) fail(String.format("v = %s: expected %s but got %s", v, expected, result));
        }
    }

    @Test
    public void distance() {
        Vector3dc base = new Vector3d(0.74f, Float.intBitsToFloat(Float.floatToIntBits(1000f) + 1), -42);

        for (Vector3dc v : testVecs) {
            float expected = (float) new Vector3d(v).distance(base);
            float result = new Vector3fx(v).distance(new Vector3fx(base));

            double error = Math.abs(expected - result);
            if (error > MAX_ERROR) fail(String.format("v = %s: expected %s but got %s", v, expected, result));
        }
    }

    @Test
    public void dot() {
        Vector3dc base = new Vector3d(0.74f, Float.intBitsToFloat(Float.floatToIntBits(1000f) + 1), -42);

        for (Vector3dc v : testVecs) {
            float expected = (float) new Vector3d(v).dot(base);
            float result = new Vector3fx(v).dot(new Vector3fx(base));

            double error = Math.abs(1 - expected / result);
            if (error > DOT_REL_ERROR) fail(String.format("v = %s: expected %s but got %s", v, expected, result));
        }
    }
}
