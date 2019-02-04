package NG.GameMap;

import NG.Rendering.MatrixStack.SGL;

import java.util.Random;

import static NG.Settings.Settings.TILE_SIZE;

/**
 * @author Geert van Ieperen created on 3-2-2019.
 */
public class MapChunkArray implements MapChunk {
    private final MapTileInstance[][] tiles;
    private final int size;
    private final Random random;

    public MapChunkArray(int size, int randomSeed) {
        this.size = size;
        this.tiles = new MapTileInstance[size][size];
        random = new Random(randomSeed);
    }

    public MapChunkArray(int size, int[][] heightmap, int fromX, int fromY, int randomSeed) {
        this.size = size;
        this.tiles = new MapTileInstance[size][];
        random = new Random(randomSeed);

        for (int cx = 0; cx < size; cx++) {
            int hx = fromX + cx;
            if (hx >= heightmap.length - 1) break;

            int[] xHeight = heightmap[hx]; // can be optimized further
            int[] x2Height = heightmap[hx + 1];
            MapTileInstance[] strip = new MapTileInstance[size];

            for (int cy = 0; cy < size; cy++) {
                int hy = fromY + cy;
                if (hy >= xHeight.length - 1) break;

                strip[cy] = MapTile.getRandomOf(random, x2Height[hy + 1], xHeight[hy + 1], xHeight[hy], x2Height[hy]);
            }
            tiles[cx] = strip;
        }
    }

    @Override
    public MapTile get(int x, int y) {
        return tiles[x][y].type;
    }

    @Override
    public MapTile set(int x, int y, MapTileInstance tile) {
        MapTile prev = tiles[x][y].type;
        tiles[x][y] = tile;
        return prev;
    }

    @Override
    public int getHeightAt(int x, int y) {
        return tiles[x][y].height;
    }

    @Override
    public void draw(SGL gl) {
        int x = 0;
        while (x < size) {
            MapTileInstance[] strip = tiles[x];
            if (strip == null) break;

            int y = 0;
            while (y < size) {
                MapTileInstance tile = strip[y];
                if (tile == null) break;

                tile.draw(gl);
                gl.translate(0, TILE_SIZE, 0);
                y++;
            }

            gl.translate(0, -TILE_SIZE * y, 0);
            gl.translate(TILE_SIZE, 0, 0);
            x++;
        }
        gl.translate(-TILE_SIZE * x, 0, 0);
    }

}
