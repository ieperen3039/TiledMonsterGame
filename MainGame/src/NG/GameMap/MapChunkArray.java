package NG.GameMap;

import NG.DataStructures.Generic.Color4f;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static NG.Settings.Settings.TILE_SIZE;
import static NG.Settings.Settings.TILE_SIZE_Z;

/**
 * @author Geert van Ieperen created on 3-2-2019.
 */
public class MapChunkArray implements MapChunk {
    private final MapTile.Instance[][] tiles;
    private final int size;
    private final Set<MapTile.Instance> highlights;
    private boolean doHighlight;

    private Extremes minimumMaximum = new Extremes();

    public MapChunkArray(int size) {
        this.size = size;
        this.tiles = new MapTile.Instance[size][size];
        this.highlights = new HashSet<>();
    }

    public MapChunkArray(int size, float[][] heightmap, int fromX, int fromY, int randomSeed) {
        this.size = size;
        this.tiles = new MapTile.Instance[size][size];
        this.highlights = new HashSet<>();
        Random random = new Random(randomSeed);

        for (int cx = 0; cx < size; cx++) {
            int hx = fromX + cx;

            float[] xHeight = heightmap[hx]; // can be optimized further
            float[] x2Height = heightmap[hx + 1];

            for (int cy = 0; cy < size; cy++) {
                int hy = fromY + cy;

                int pos_pos = (int) x2Height[hy + 1];
                int pos_neg = (int) xHeight[hy + 1];
                int neg_neg = (int) xHeight[hy];
                int neg_pos = (int) x2Height[hy];

                MapTile.Instance randTile = MapTiles.getRandomOf(random, pos_pos, pos_neg, neg_neg, neg_pos);
                float height = randTile.getHeight() * TILE_SIZE_Z;
                minimumMaximum.check(height);

                tiles[cx][cy] = randTile;
            }
        }
    }

    @Override
    public MapTile.Instance get(int x, int y) {
        checkBounds(x, y);
        return tiles[x][y];
    }

    @Override
    public MapTile set(int x, int y, MapTile.Instance tile) {
        checkBounds(x, y);
        MapTile prev = tiles[x][y].type;
        tiles[x][y] = tile;
        minimumMaximum.check(tile.getHeight() * TILE_SIZE_Z);
        return prev;
    }

    @Override
    public int getHeightAt(int x, int y) {
        checkBounds(x, y);
        return tiles[x][y].getHeight();
    }

    private void checkBounds(int x, int y) {
        if (x < 0 || y < 0 || x >= size || y >= size) {
            throw new IndexOutOfBoundsException("(" + x + ", " + y + "), size is " + size);
        }
    }

    @Override
    public void draw(SGL gl) {
        MaterialShader mShader = null;
        if (gl.getShader() instanceof MaterialShader) {
            mShader = (MaterialShader) gl.getShader();
        }

        int x = 0;
        while (x < size) {
            MapTile.Instance[] strip = tiles[x];
//            if (strip == null) break;

            int y = 0;
            while (y < size) {
                MapTile.Instance tile = strip[y];
//                if (tile == null) break;

                boolean highlightThis = doHighlight && mShader != null && highlights.contains(tile);

                if (highlightThis) {
                    mShader.setMaterial(Material.ROUGH, Color4f.WHITE);
                }

                tile.draw(gl);

                if (highlightThis) {
                    mShader.setMaterial(Material.ROUGH, new Color4f(85, 153, 0, 1));
                }

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
    public void highlight(int x, int y) {
        checkBounds(x, y);
        highlights.add(tiles[x][y]);
    }

    @Override
    public void clearHighlight() {
        highlights.clear();
    }

    @Override
    public void writeToFile(DataOutputStream out) throws IOException {
        for (MapTile.Instance[] tileStrip : tiles) {
            for (MapTile.Instance tile : tileStrip) {
                MapTile type = tile.type; // implementation dependent
                out.writeInt(type.tileID);
                out.writeByte(tile.rotation);
                out.writeByte(tile.offset);
            }
        }
    }

    @Override
    public void readFromFile(DataInputStream in, Map<Integer, MapTile> mapping) throws IOException {
        for (MapTile.Instance[] tileStrip : tiles) {
            for (int i = 0; i < tileStrip.length; i++) {

                int typeID = in.readInt();
                byte rotation = in.readByte();
                byte offset = in.readByte();

                MapTile.Instance newTile = new MapTile.Instance(
                        offset,
                        rotation,
                        mapping.get(typeID)
                );
                minimumMaximum.check(newTile.getHeight() * TILE_SIZE_Z);

                tileStrip[i] = newTile;
            }
        }
    }

    @Override
    public Extremes getMinMax() {
        return minimumMaximum;
    }

    @Override
    public void setHighlight(boolean doHighlight) {
        this.doHighlight = doHighlight;
    }
}
