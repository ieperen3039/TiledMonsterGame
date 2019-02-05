package NG.GameMap;

import NG.ActionHandling.MouseTools.MouseTool;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.Game;
import NG.Entities.Entity;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Settings.Settings;
import NG.Tools.Vectors;
import org.joml.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static NG.Settings.Settings.TILE_SIZE;
import static NG.Settings.Settings.TILE_SIZE_Z;

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
    private HashMap<Vector2ic, Entity> claimRegistry;
    private Lock claimLock;
    private Game game;

    public TileMap(int chunkSize) {
        this.chunkSize = chunkSize;
        this.realChunkSize = chunkSize * Settings.TILE_SIZE;
        this.changeListeners = new ArrayList<>();
        this.claimRegistry = new HashMap<>();
        this.claimLock = new ReentrantLock(false);
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

        xSize = (heightmap.length - 1) / chunkSize;
        ySize = (heightmap[0].length - 1) / chunkSize;

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
    public Vector3i getCoordinate(Vector3fc position) {
        return new Vector3i(
                (int) (position.x() / TILE_SIZE),
                (int) (position.y() / TILE_SIZE),
                (int) (position.z() / TILE_SIZE_Z)
        );
    }

    @Override
    public Vector3f getPosition(int x, int y) {
        return new Vector3f(
                (x + 0.5f) * TILE_SIZE, // middle of the tile
                (y + 0.5f) * TILE_SIZE,
                getHeightAt(x, y) * TILE_SIZE_Z
        );
    }

    @Override
    public float getHeightAt(float x, float y) {
        int ix = (int) (x / TILE_SIZE);
        int iy = (int) (y / TILE_SIZE);

        float avgHeights = getHeightAt(ix, iy);

        return avgHeights * TILE_SIZE_Z;
    }

    @Override
    public void draw(SGL gl) {
        if (map == null) return;
        assert gl.getPosition(new Vector3f()).equals(new Vector3f()) : "gl object not placed at origin";

        ShaderProgram shader = gl.getShader();
        if (shader instanceof MaterialShader) {
            ((MaterialShader) shader).setMaterial(Material.ROUGH, new Color4f(85, 153, 0, 1));
        }

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
        float t = Intersectionf.intersectRayPlane(origin, direction, Vectors.O, Vectors.Z, 1E-6f);
        return origin.add(direction.mul(t, temp), temp);
    }

    @Override
    public boolean createClaim(Vector2ic coordinate, Entity entity) {
        // check bounds
        int cx = coordinate.x() / chunkSize;
        int cy = coordinate.y() / chunkSize;
        if (cx >= xSize || cx < 0 || cy >= ySize || cy < 0) {
            return false;
        }

        // perform claim
        claimLock.lock();
        try {
            boolean isClaimed = claimRegistry.containsKey(coordinate);

            if (!isClaimed) {
                Vector2i copy = new Vector2i(coordinate);
                claimRegistry.put(copy, entity);
                return true;
            }
            return false;

        } finally {
            claimLock.unlock();
        }
    }

    @Override
    public Entity getClaim(Vector2ic coordinate) {
        return claimRegistry.get(coordinate); // no lock needed
    }

    @Override
    public boolean dropClaim(Vector2ic coordinate, Entity entity) {
        claimLock.lock();
        try {
            Entity claimant = claimRegistry.get(coordinate);

            if (entity == null || entity == claimant) {
                claimRegistry.remove(coordinate);
                return true;
            }
            return false;

        } finally {
            claimLock.unlock();
        }
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
}
