package NG.GameMap;

import org.joml.Vector3f;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Geert van Ieperen created on 10-2-2020.
 */
public class MapTileTest {
    @Test
    public void testInstanceIntersection() {
        MapTile.Instance instance = new MapTile.Instance(0, 0, MapTile.DEFAULT_TILE);
        {
            float f = instance.intersectFraction(new Vector3f(0, 0, 1.5f), new Vector3f(0, 0, -1));
            assertEquals(f, 0.5f, 1e-6);
        }

        {
            // collision at (0, 0, 1)
            float f = instance.intersectFraction(new Vector3f(0, 0, 5), new Vector3f(0, 0, -1));
            assertEquals(f, 4, 1e-6);
        }

        {
            // collision at (1. 0, 0)
            float f = instance.intersectFraction(new Vector3f(5, 0, 0), new Vector3f(-1, 0, 0));
            assertEquals(f, 4, 1e-6);
        }

        {
            // collision at (1. 1, 0)
            float f = instance.intersectFraction(new Vector3f(5, 5, 0), new Vector3f(-1, -1, 0));
            assertEquals(f, 4, 1e-6);
        }
    }
}
