package NG.GameMap;

import NG.Core.Version;
import org.joml.Vector2ic;

import java.util.Collections;
import java.util.Map;

/**
 * Generates the heightmap back from the given map, by querying the heights.
 * @author Geert van Ieperen created on 17-2-2019.
 */
public class CopyGenerator implements MapGeneratorMod {
    private final GameMap target;
    private int xSize = -1;
    private int ySize = -1;
    protected int progress = 0;

    public CopyGenerator(GameMap target) {
        this.target = target;
    }

    @Override
    public Map<String, Integer> getProperties() {
        return Collections.emptyMap();
    }

    @Override
    public float[][] generateHeightMap() {
        progress = 0;
        Vector2ic size = target.getSize();
        if (xSize == -1) xSize = size.x();
        if (ySize == -1) ySize = size.y();

        float[][] floats = new float[xSize][ySize];

        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                floats[x][y] = target.getHeightAt(x, y);
                progress++;
            }
        }

        return floats;
    }

    @Override
    public int getMapSeed() {
        return -1;
    }

    @Override
    public float heightmapProgress() {
        return (float) progress / (xSize * ySize); // either progress is 0 or xSize and ySize are positive
    }

    @Override
    public void setXSize(int xSize) {
        this.xSize = xSize;
    }

    @Override
    public void setYSize(int ySize) {
        this.ySize = ySize;
    }

    @Override
    public Version getVersionNumber() {
        return new Version(0, 0);
    }

    @Override
    public void cleanup() {
//        target.cleanup();
    }
}
