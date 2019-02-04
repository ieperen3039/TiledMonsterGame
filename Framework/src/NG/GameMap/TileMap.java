package NG.GameMap;

import NG.ActionHandling.MouseTools.MouseTool;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.Game;
import NG.Rendering.MatrixStack.SGL;
import NG.Settings.Settings;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

import static NG.Settings.Settings.TILE_SIZE_Y;

/**
 * @author Geert van Ieperen created on 3-2-2019.
 */
public class TileMap implements GameMap {
    private final int chunkSize;
    private final float realChunkSize;

    private int xSize;
    private int ySize;
    private MapChunk[][] map;

    private List<ChangeListener> changeListeners;
    private Game game;

    public TileMap(int chunkSize) {
        this.chunkSize = chunkSize;
        this.realChunkSize = chunkSize * Settings.TILE_SIZE;
        this.changeListeners = new ArrayList<>();
    }

    @Override
    public void init(Game game) {
        this.game = game;
        game.lights().addDirectionalLight(new Vector3f(1, -1.5f, 2), Color4f.WHITE, 0.5f);
    }

    @Override
    public void generateNew(MapGeneratorMod mapGenerator) {
        changeListeners.forEach(ChangeListener::onMapChange);

        // height map generation
        int[][] heightmap = mapGenerator.generateHeightMap();
        int randomSeed = mapGenerator.getMapSeed();

        xSize = (heightmap.length / chunkSize) + 1;
        ySize = (heightmap[0].length / chunkSize) + 1;

        map = new MapChunk[xSize][];
        for (int mx = 0; mx < xSize; mx++) {
            MapChunk[] yStrip = new MapChunk[ySize];

            for (int my = 0; my < ySize; my++) {
                int fromY = my * chunkSize;
                int fromX = mx * chunkSize;
                MapChunkArray chunk = new MapChunkArray(chunkSize, heightmap, fromX, fromY, randomSeed);

                yStrip[my] = chunk;
            }
            map[mx] = yStrip;
        }

    }

    @Override
    public int getHeightAt(int x, int y) {
        int cx = x / chunkSize;
        int cy = y / chunkSize;

        if (cx >= xSize || cx < 0 || cy >= ySize || cy < 0) {
            return 0;
        }

        MapChunk chunk = map[cx][cy];

        return chunk == null ? 0 : chunk.getHeightAt(x - cx, y - cy);

    }

    @Override
    public void draw(SGL gl) {
        if (map == null) return;

        gl.pushMatrix();
        {
            for (MapChunk[] chunks : map) {
                gl.pushMatrix();
                {
                    for (MapChunk chunk : chunks) {
                        chunk.draw(gl);
                        gl.translate(0, realChunkSize, 0);
                    }
                }
                gl.popMatrix();
                gl.translate(realChunkSize, 0, 0);
            }
        }
        gl.popMatrix();
    }

    @Override
    public Vector3f intersectWithRay(Vector3fc origin, Vector3fc direction) {
        Vector3f temp = new Vector3f();
        float t = Intersectionf.intersectRayPlane(origin, direction, Vectors.zeroVector(), Vectors.zVector(), 1E-6f);
        return origin.add(direction.mul(t, temp), temp);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    @Override
    public boolean checkMouseClick(MouseTool tool, int xSc, int ySc) {
        Vector3f origin = new Vector3f();
        Vector3f direction = new Vector3f();
        Vectors.windowCoordToRay(game, xSc, ySc, origin, direction);

        Vector3f position = intersectWithRay(origin, direction);
        tool.apply(new Vector2f(position.x, position.y));
        return true;
    }

    @Override
    public void cleanup() {
        changeListeners.clear();
    }

    /**
     * @param x an exact x position on the map
     * @param y an exact y position on the map
     * @return the height at position (x, y) on the map
     */
    @Override
    public float getHeightAt(float x, float y) {
        int ix = (int) x;
        int iy = (int) y;

        float xFrac = x - ix;
        float yFrac = y - iy;
        float x1Lerp = Toolbox.interpolate(getHeightAt(ix, iy), getHeightAt(ix + 1, iy), xFrac);
        float x2Lerp = Toolbox.interpolate(getHeightAt(ix, iy + 1), getHeightAt(ix + 1, iy + 1), xFrac);
        return Toolbox.interpolate(x1Lerp, x2Lerp, yFrac) * TILE_SIZE_Y;
    }
}
