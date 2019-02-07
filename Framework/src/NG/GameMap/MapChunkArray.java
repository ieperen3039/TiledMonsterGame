package NG.GameMap;

import NG.Rendering.MatrixStack.SGL;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
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

    public MapChunkArray(int size, float[][] heightmap, int fromX, int fromY, int randomSeed) {
        this.size = size;
        this.tiles = new MapTileInstance[size][];
        random = new Random(randomSeed);

        for (int cx = 0; cx < size; cx++) {
            int hx = fromX + cx;

            float[] xHeight = heightmap[hx]; // can be optimized further
            float[] x2Height = heightmap[hx + 1];
            MapTileInstance[] strip = new MapTileInstance[size];

            for (int cy = 0; cy < size; cy++) {
                int hy = fromY + cy;

                int pos_pos = (int) x2Height[hy + 1];
                int pos_neg = (int) xHeight[hy + 1];
                int neg_neg = (int) xHeight[hy];
                int neg_pos = (int) x2Height[hy];

                strip[cy] = MapTile.getRandomOf(random, pos_pos, pos_neg, neg_neg, neg_pos);
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
        if (x < 0 || y < 0 || x >= size || y >= size) return 0;
        MapTileInstance tile = tiles[x][y];
        return tile.height + tile.type.baseHeight;
    }

    @Override
    public void draw(SGL gl) {
        int x = 0;
        while (x < size) {
            MapTileInstance[] strip = tiles[x];
//            if (strip == null) break;

            int y = 0;
            while (y < size) {
                MapTileInstance tile = strip[y];
//                if (tile == null) break;

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

    @Override
    public void writeToFile(DataOutput out) throws IOException {
        for (MapTileInstance[] tileStrip : tiles) {
            for (MapTileInstance tile : tileStrip) {
                out.writeInt(tile.type.tileID);
                out.writeByte(tile.rotation);
                out.writeByte(tile.height);
            }
        }
    }

    @Override
    public void readFromFile(DataInput in, MapTile[] mapping) throws IOException {
        for (MapTileInstance[] tileStrip : tiles) {
            for (int i = 0; i < tileStrip.length; i++) {

                int typeID = in.readInt();
                byte rotation = in.readByte();
                byte height = in.readByte();

                tileStrip[i] = new MapTileInstance(
                        height,
                        rotation,
                        mapping[typeID]
                );
            }
        }
    }
}
