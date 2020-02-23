package NG.Rendering.Shapes.Primitives;

import org.joml.Vector3f;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Geert van Ieperen created on 23-2-2020.
 */
public class JOMLTest {
    @Test
    public void testCross() {
        Vector3f a = new Vector3f(0, 2, 0);
        Vector3f b = new Vector3f(0, 0, 2);
        Vector3f cross = a.cross(b);
        assertEquals(new Vector3f(4, 0, 0), cross);
    }
}
