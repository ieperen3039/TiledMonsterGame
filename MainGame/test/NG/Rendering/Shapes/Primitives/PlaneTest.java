package NG.Rendering.Shapes.Primitives;

import NG.Tools.Toolbox;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.junit.Before;

/**
 * Created by s152717 on 14-2-2017.
 */
public class PlaneTest {
    Plane instance;

    @Before
    public void setUp() {
        instance = null;
    }

    /**
     * testclass for stopVector
     * @param first    startpoint of line
     * @param second   endpoint of line
     * @param expected the expected point where it would hit the environment defined in {@code instance}
     */
    void testIntersect(Vector3f first, Vector3f second, Vector3f expected) {
        Vector3f dir = new Vector3f(second).sub(first);
        System.out.println("\nVector " + first + " towards " + second + ": direction is " + dir);
        if (expected == null) System.out.println("Expecting no intersection");

        float scalar = instance.getIntersectionScalar(first, dir);
        Vector3fc result = new Vector3f(dir).mul(scalar).add(first);

        boolean hasIntersect = (scalar <= 1);

        if (!hasIntersect) {
            System.out.println("No intersection");
        } else {
            System.out.println("Scalar: " + scalar + ", result: " + result);
        }

        if (expected == null) {
            if (hasIntersect) {
                throw new AssertionError(String.format(
                        "Testcase gave %s where no intersection was expected",
                        result
                ));
            }
        } else {
            if (!hasIntersect) {
                throw new AssertionError(String.format(
                        "Testcase gave no intersection where %s was expected",
                        expected
                ));
            }

            float diff = expected.distance(result);
            if (!Toolbox.almostZero(diff)) {
                throw new AssertionError(String.format(
                        "Testcase gave %s where %s was expected: difference is %f units",
                        result, expected, diff
                ));
            }
        }
    }
}
