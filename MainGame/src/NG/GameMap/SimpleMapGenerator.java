package NG.GameMap;

import NG.Core.Version;
import NG.Tools.OpenSimplexNoise;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Geert van Ieperen. Created on 27-9-2018.
 */
public class SimpleMapGenerator implements MapGeneratorMod {
    private static final double AMPLITUDE_SCALE_FACTOR = 0.1;
    private static final double MAJOR_DENSITY = 0.02;
    private static final double MINOR_DENSITY = 0.2;

    private static final String MAJOR_AMPLITUDE = "Major amplitude";
    private static final String MINOR_AMPLITUDE = "Minor amplitude";

    private float progress = 0;
    private int seed;
    private int width;
    private int height;
    private final OpenSimplexNoise majorGenerator;
    private final OpenSimplexNoise minorGenerator;

    private HashMap<String, Integer> properties;

    public SimpleMapGenerator(int seed) {
        this.seed = seed;
        majorGenerator = new OpenSimplexNoise(seed);
        minorGenerator = new OpenSimplexNoise(seed + 1);
        properties = new HashMap<>();
        properties.put(MAJOR_AMPLITUDE, 200);
        properties.put(MINOR_AMPLITUDE, 20);
    }

    @Override
    public Map<String, Integer> getProperties() {
        return properties;
    }

    @Override
    public float[][] generateHeightMap() {
        float[][] map = new float[width][height];

        double majorAmplitude = properties.get(MAJOR_AMPLITUDE) * AMPLITUDE_SCALE_FACTOR;
        addNoiseLayer(map, majorGenerator, MAJOR_DENSITY, majorAmplitude);
        progress = 0.5f;

        double minorAmplitude = properties.get(MINOR_AMPLITUDE) * AMPLITUDE_SCALE_FACTOR;
        addNoiseLayer(map, minorGenerator, MINOR_DENSITY, minorAmplitude);
        progress = 1;

        return map;
    }

    private void addNoiseLayer(
            float[][] map, OpenSimplexNoise noise, double density, double amplitude
    ) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] += (float) (amplitude * noise.eval(x * density, y * density));
            }
        }
    }

    @Override
    public float heightmapProgress() {
        return progress;
    }

    @Override
    public void cleanup() {
    }

    @Override
    public int getMapSeed() {
        return seed;
    }

    @Override
    public Version getVersionNumber() {
        return new Version(0, 0);
    }

    @Override
    public void setXSize(int xSize) {
        this.width = xSize;
    }

    @Override
    public void setYSize(int ySize) {
        this.height = ySize;
    }
}
