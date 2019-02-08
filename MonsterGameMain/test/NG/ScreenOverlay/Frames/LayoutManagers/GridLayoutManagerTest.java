package NG.ScreenOverlay.Frames.LayoutManagers;

import NG.ScreenOverlay.Frames.Components.SPanel;
import org.joml.Vector2i;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Geert van Ieperen. Created on 22-9-2018.
 */
public class GridLayoutManagerTest extends GridLayoutManager {
    @Test
    public void minimumInvalidationTest() {
        add(new SPanel(10, 10, 1, 1, false, true), 0, 0);
        add(new SPanel(15, 15, 1, 2, false, false), 1, 1);
        super.recalculateProperties();
        super.placeComponents();

        System.out.println("cols = " + Arrays.toString(getMinColWidth()) + " | " + "colGrow = " + Arrays.toString(getColWantGrow()));

        int[] minColWidth = super.getMinColWidth();
        assertEquals(10, minColWidth[0]);
        assertEquals(15, minColWidth[1]);
        assertEquals(0, minColWidth[2]);

        int[] minRowHeight = super.getMinRowHeight();
        assertEquals(10, minRowHeight[0]);
        assertEquals(15, minRowHeight[1]);
        assertEquals(0, minRowHeight[2]);

        boolean[] colWantGrow = getColWantGrow();
        assertFalse(colWantGrow[0]);
        assertFalse(colWantGrow[1]);
        assertFalse(colWantGrow[2]);

        boolean[] rowWantGrow = getRowWantGrow();
        assertTrue(rowWantGrow[0]);
        assertFalse(rowWantGrow[1]);
        assertFalse(rowWantGrow[2]);

        add(new SPanel(10, 10, true), 0, 1);
    }

    @Test
    public void placeComponentsTest() {
        SPanel panel1 = new SPanel(10, 10, 1, 1, false, false);
        SPanel panelGrow = new SPanel(10, 10, 1, 1, true, true);
        SPanel panel2 = new SPanel(10, 10, 1, 1, false, false);

        add(panel1, 0, 0);
        add(panelGrow, 0, 1);
        add(panel2, 1, 1);

        super.recalculateProperties();

        final Vector2i pos = new Vector2i(100, 50);
        final Vector2i dim = new Vector2i(200, 400);
        super.setDimensions(pos, dim);
        super.placeComponents();

        System.out.println("cols = " + Arrays.toString(getMinColWidth()) + " | " + "colGrow = " + Arrays.toString(getColWantGrow()));

        assertEquals(pos.x, panel1.getX());
        assertEquals(pos.x + dim.x - 10, panel2.getX());
        assertEquals(pos.x, panelGrow.getX());
        assertEquals(pos.y, panel1.getY());
        assertEquals(pos.y + 10, panel2.getY());
        assertEquals(pos.y + 10, panelGrow.getY());
    }

    @Test
    public void createLayoutDimensionTest() {
        final int size = 100;

        int[] layout = calculateDimensionSizes(new int[]{10, 20, 30}, 1, new boolean[]{false, true, false}, size);
        System.out.println("Layout = " + Arrays.toString(layout));

        assertEquals(10, layout[0]);
        assertEquals(size - (10 + 30), layout[1]);
        assertEquals(30, layout[2]);
    }
}
