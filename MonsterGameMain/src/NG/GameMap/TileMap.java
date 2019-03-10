package NG.GameMap;

import NG.ActionHandling.MouseTools.MouseTool;
import NG.DataStructures.Direction;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.Game;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Settings.Settings;
import NG.Tools.AStar;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

import static NG.Settings.Settings.TILE_SIZE;
import static NG.Settings.Settings.TILE_SIZE_Z;

/**
 * @author Geert van Ieperen created on 3-2-2019.
 */
public class TileMap implements GameMap {
    private final int chunkSize;
    private final float realChunkSize;
    private final List<ChangeListener> changeListeners;

    private int xSize = 0;
    private int ySize = 0;
    private MapChunk[][] map;
    private Game game;

    private Collection<MapChunk> highlightedChunks;

    public TileMap(int chunkSize) {
        this.chunkSize = chunkSize;
        this.realChunkSize = chunkSize * Settings.TILE_SIZE;
        this.changeListeners = new ArrayList<>();
        highlightedChunks = new HashSet<>();
        map = new MapChunk[0][0];
    }

    @Override
    public void init(Game game) {
        this.game = game;
    }

    @Override
    public void generateNew(MapGeneratorMod mapGenerator) {
        changeListeners.forEach(ChangeListener::onMapChange);

        // height map generation
        float[][] heightmap = mapGenerator.generateHeightMap();
        int randomSeed = mapGenerator.getMapSeed();

        if (heightmap.length == 0) throw new IllegalArgumentException("Received map with 0 size in x direction");

        int xChunks = (heightmap.length - 1) / chunkSize;
        int yChunks = (heightmap[0].length - 1) / chunkSize;

        MapChunk[][] newMap = new MapChunk[xChunks][yChunks];
        for (int mx = 0; mx < xChunks; mx++) {
            for (int my = 0; my < yChunks; my++) {
                int fromY = my * chunkSize;
                int fromX = mx * chunkSize;
                MapChunkArray chunk = new MapChunkArray(chunkSize, heightmap, fromX, fromY, randomSeed);

                newMap[mx][my] = chunk;
            }
        }

        this.map = newMap;
        this.xSize = xChunks;
        this.ySize = yChunks;
    }

    @Override
    public int getHeightAt(int x, int y) {
        int cx = x / chunkSize;
        int cy = y / chunkSize;

        if (cx >= xSize || cx < 0 || cy >= ySize || cy < 0) {
            return 0;
        }

        MapChunk chunk = map[cx][cy];

        return chunk == null ? 0 : chunk.getHeightAt(x - cx * chunkSize, y - cy * chunkSize);
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
        boolean isMaterialShader = shader instanceof MaterialShader;
        if (isMaterialShader) {
            ((MaterialShader) shader).setMaterial(Material.ROUGH, new Color4f(85, 153, 0, 1));
        }

        gl.pushMatrix();
        {
            // tile 1 stretches form (0, 0) to (TILE_SIZE, TILE_SIZE)
            gl.translate(TILE_SIZE * 0.5f, TILE_SIZE * 0.5f, -1);

            for (MapChunk[] chunks : map) {
                gl.pushMatrix();
                {
                    for (MapChunk chunk : chunks) {
                        chunk.setHighlight(isMaterialShader);
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

        Vector3fc point = game.camera().getFocus();
        float t = Intersectionf.intersectRayPlane(origin, direction, point, Vectors.Z, 1E-6f);

        return origin.add(direction.mul(t, temp), temp);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    @Override
    public void setHighlights(Vector2ic... coordinates) {
        highlightedChunks.forEach(MapChunk::clearHighlight);
        highlightedChunks.clear();

        for (Vector2ic c : coordinates) {
            int cx = c.x() / chunkSize;
            int cy = c.y() / chunkSize;

            if (cx >= xSize || cx < 0 || cy >= ySize || cy < 0) continue;

            MapChunk chunk = map[cx][cy];
            highlightedChunks.add(chunk);

            int rx = c.x() - cx * chunkSize;
            int ry = c.y() - cy * chunkSize;

            chunk.highlight(rx, ry);
        }
    }

    @Override
    public Vector2ic getSize() {
        return new Vector2i(xSize * chunkSize, ySize * chunkSize);
    }

    @Override
    public boolean checkMouseClick(MouseTool tool, int xSc, int ySc) {
        return checkMouseClick(tool, xSc, ySc, game);
    }

    @Override
    public void cleanup() {
        changeListeners.clear();
        highlightedChunks.clear();
    }

    @Override
    public void writeToDataStream(DataOutput out) throws IOException {
        List<MapTile> tileTypes = MapTile.values();

        // number of tile types
        int nrOfTileTypes = tileTypes.size();
        out.writeInt(nrOfTileTypes);

        // write all tiles in order
        for (MapTile tileType : tileTypes) {
            out.writeInt(tileType.tileID);
            out.writeUTF(tileType.toString());
            out.writeUTF(String.valueOf(tileType.sourceSet));
            out.writeInt(tileType.orientationBytes());
        }

        out.writeInt(chunkSize);
        out.writeInt(xSize);
        out.writeInt(ySize);

        // now write the chunks themselves
        for (MapChunk[] mapChunks : map) {
            for (MapChunk chunk : mapChunks) {
                chunk.writeToFile(out);
            }
        }
    }

    /**
     * Constructs an instance from a data stream. Must be executed on the render thread for loading tile models.
     * @param in the data stream synchonized to the call to {@link #writeToDataStream(DataOutput)}
     * @throws IOException if the data produces unexpected values
     */
    public TileMap(DataInput in) throws IOException {
        changeListeners = new ArrayList<>();

        // get number of tile types
        int nrOfTileTypes = in.readInt();
        Map<Integer, MapTile> types = new HashMap<>(nrOfTileTypes);

        // read tiles in order
        for (int i = 0; i < nrOfTileTypes; i++) {
            int tileID = in.readInt();
            String name = in.readUTF();
            String set = in.readUTF();
            int orientation = in.readInt();

            if (!set.equals("null")) {
                TileThemeSet.valueOf(set).load();
            }
            MapTile presumedTile = MapTile.getByName(name);

            if (presumedTile == MapTile.DEFAULT_TILE) {
                types.put(tileID, MapTile.getByOrientationBytes(orientation, 0));

            } else if (presumedTile.orientationBytes() == orientation) {
                types.put(tileID, presumedTile);

            } else {
                Logger.ASSERT.printf("Closest match for %s (%s) did not have correct rotation. Defaulting to a plain tile",
                        name, presumedTile
                );
                types.put(tileID, MapTile.getByOrientationBytes(orientation, 0));
            }
        }

        chunkSize = in.readInt();
        this.realChunkSize = chunkSize * Settings.TILE_SIZE;
        this.xSize = in.readInt();
        this.ySize = in.readInt();

        // now read chunks themselves
        map = new MapChunk[xSize][];
        for (int mx = 0; mx < xSize; mx++) {
            MapChunk[] yStrip = new MapChunk[ySize];

            for (int my = 0; my < ySize; my++) {
                MapChunkArray chunk = new MapChunkArray(chunkSize);
                chunk.readFromFile(in, types);
                yStrip[my] = chunk;
            }
            map[mx] = yStrip;
        }
    }

    public MapTile.Instance getTileData(int x, int y) {
        if (x < 0 || y < 0) return null;

        int cx = x / chunkSize;
        int cy = y / chunkSize;

        if (cx >= xSize || cy >= ySize) return null;

        MapChunk chunk = map[cx][cy];

        int rx = x - cx * chunkSize;
        int ry = y - cy * chunkSize;
        return chunk.get(rx, ry);
    }

    public void setTile(int x, int y, MapTile.Instance instance) {
        int cx = x / chunkSize;
        int cy = y / chunkSize;

        if (cx >= xSize || cx < 0 || cy >= ySize || cy < 0) return;

        MapChunk chunk = map[cx][cy];

        int rx = x - cx * chunkSize;
        int ry = y - cy * chunkSize;

        chunk.set(rx, ry, instance);
    }

    @Override
    public List<Vector2i> findPath(
            Vector2ic beginPosition, Vector2ic target, float walkSpeed, float climbSpeed
    ) {
        int xMax = (xSize * chunkSize) - 1;
        int yMax = (ySize * chunkSize) - 1;

        return new AStar(beginPosition, target, xMax, yMax) {
            @Override
            public float distanceAdjacent(int x1, int y1, int x2, int y2) {
                assert x1 == x2 || y1 == y2;

                MapTile.Instance fromTile = getTileData(x1, y1);
                MapTile.Instance toTile = getTileData(x2, y2);
                assert fromTile != null && toTile != null;

                Direction move = Direction.get(x2 - x1, y2 - y1);

                // the total duration of this movement
                float duration = 0;

                // heights of tile sides
                int fromHeight = fromTile.heightOf(move);
                int toHeight = toTile.heightOf(move.inverse());
                float hDiff = (fromHeight - toHeight) * TILE_SIZE_Z;

                // climbing if more than 'an acceptable height'
                if (hDiff < 0.5f) {
                    duration += (climbSpeed * hDiff);
                }

                // steepness
                float t1inc = (fromHeight - fromTile.getHeight()) * (TILE_SIZE / 2);
                float t2inc = (toHeight - toTile.getHeight()) * (TILE_SIZE / 2);

                // actual duration of walking
                float walkSpeedT1 = lift(t1inc) * walkSpeed;
                float walkSpeedT2 = lift(t2inc) * walkSpeed;

                // duration += walkspeed(steepness_1) * dist + walkspeed(steepness_2) * dist
                duration += (walkSpeedT1 + walkSpeedT2) * (TILE_SIZE / 2);

                return duration;
            }

        }.call();
    }
}
