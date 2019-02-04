package NG.GameMap;

import NG.Mods.Mod;

/**
 * @author Geert van Ieperen. Created on 29-9-2018.
 */
public interface MapGeneratorMod extends Mod {

    /**
     * generate a heightmap which will be used to render the world.
     * @return the new heightmap, of size (xSize x ySize)
     */
    int[][] generateHeightMap();

    /**
     * @return the seed used to create the map
     */
    int getMapSeed();

    /**
     * gives an indication of how far {@link #generateHeightMap()} is progressed.
     * @return a float [0, 1] indicating the generation progress. 0 indicates that it hasn't started, 1 indicates that
     * it is done.
     */
    float heightmapProgress();

    void setXSize(int xSize);

    void setYSize(int ySize);

    default void setSize(int x, int y) {
        setXSize(x);
        setYSize(y);
    }

    ;
}
