package NG.Rendering.Shapes.Primitives;

import NG.Tools.Toolbox;
import org.joml.Vector3f;
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

        Collision box = instance.getCollisionWith(first, dir, second);

        Vector3f result = new Vector3f();

        if (box != null) {
            // get position of the new vector
            System.out.println("Hitpoint: " + box.hitPosition());
        }

        if (expected == null) {
            assert box == null :
                    String.format("Testcase gave %s where no intersection was expected",
                            result);
        } else {
            assert box != null :
                    String.format("Testcase gave no intersection where %s was expected",
                            expected);

            float diff = expected.distance(result);
            if (!Toolbox.almostZero(diff)) {
                throw new AssertionError(
                        String.format("Testcase gave %s where %s was expected: difference is %f units",
                                result, expected, diff)
                );
            }
        }
    }
}
