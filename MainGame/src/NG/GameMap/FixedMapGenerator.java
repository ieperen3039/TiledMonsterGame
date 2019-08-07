package NG.GameMap;

import NG.Core.Version;

import java.util.Collections;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 6-3-2019.
 */
public class FixedMapGenerator implements MapGeneratorMod {
    private final float[][] map;

    public FixedMapGenerator(float[]... map) {
        this.map = map;
    }

    @Override
    public Map<String, Integer> getProperties() {
        return Collections.emptyMap();
    }

    @Override
    public float[][] generateHeightMap() {
        return map;
    }

    @Override
    public int getMapSeed() {
        return 0;
    }

    @Override
    public float heightmapProgress() {
        return 1;
    }

    @Override
    public void setXSize(int xSize) {
        throw new UnsupportedOperationException("map is fixed size");
    }

    @Override
    public void setYSize(int ySize) {
        throw new UnsupportedOperationException("map is fixed size");
    }

    @Override
    public Version getVersionNumber() {
        return new Version(0, 0);
    }

    @Override
    public void cleanup() {

    }
}
