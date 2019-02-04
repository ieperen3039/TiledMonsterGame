package NG.GameMap;

import NG.Engine.Game;
import NG.Engine.Version;
import NG.Tools.Toolbox;
import org.joml.SimplexNoise;

/**
 * @author Geert van Ieperen. Created on 27-9-2018.
 */
public class SimpleMapGenerator implements MapGeneratorMod {
    private static final float PRIMARY_DENSITY = 0.05f;
    private static final float AMPLITUDE = 8f;
    private int progress = 0;
    private int seed;
    private int width;
    private int height;

    @Override
    public void init(Game game) throws Version.MisMatchException {
        seed = Math.abs(Toolbox.random.nextInt());
    }

    @Override
    public int[][] generateHeightMap() {
        int[][] map = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = (int) (AMPLITUDE * SimplexNoise.noise(PRIMARY_DENSITY * x, PRIMARY_DENSITY * y));
            }
        }
        progress = 1;
        return map;
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
