package NG.GameMap;

import org.joml.Vector3f;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Geert van Ieperen created on 10-2-2020.
 */
public class MapTileTest {
    MapTile.Instance instance;

    @Before
    public void setUp() throws Exception {
        instance = new MapTile.Instance(0, 0, MapTile.DEFAULT_TILE);
    }

    @Test
    public void testIntersect0() {
        float f = instance.intersectFraction(new Vector3f(0, 0, 1.5f), new Vector3f(0, 0, -1));
        assertEquals(0.5f, f, 1e-6);
    }

    @Test
    public void testIntersect1() {
        // collision at (0, 0, 1)
        float f = instance.intersectFraction(new Vector3f(0, 0, 5), new Vector3f(0, 0, -1));
        assertEquals(4, f, 1e-6);
    }

    @Test
    public void testIntersect2() {
        // collision at (1. 0, 0)
        float f = instance.intersectFraction(new Vector3f(5, 0, 0), new Vector3f(-1, 0, 0));
        assertEquals(4, f, 1e-6);
    }

    @Test
    public void testIntersect3() {
        // collision at (1. 1, 0)
        float f = instance.intersectFraction(new Vector3f(5, 5, 0), new Vector3f(-1, -1, 0));
        assertEquals(4, f, 1e-6);
    }
}
