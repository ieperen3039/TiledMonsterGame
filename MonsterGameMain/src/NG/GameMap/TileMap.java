package NG.GameMap;

import NG.DataStructures.Direction;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.Game;
import NG.InputHandling.MouseTools.MouseTool;
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
import java.lang.Math;
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

    private int xChunks = 0;
    private int yChunks = 0;
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
        this.xChunks = xChunks;
        this.yChunks = yChunks;
    }

    @Override
    public int getHeightAt(int x, int y) {
        int cx = x / chunkSize;
        int cy = y / chunkSize;

        if (cx >= xChunks || cx < 0 || cy >= yChunks || cy < 0) {
            return 0;
        }

        MapChunk chunk = map[cx][cy];

        return chunk == null ? 0 : chunk.getHeightAt(x - cx * chunkSize, y - cy * chunkSize);
    }

    @Override
    public Vector2i getCoordinate(Vector3fc position) {
        return new Vector2i(
                (int) (position.x() / TILE_SIZE),
                (int) (position.y() / TILE_SIZE)
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
        MapTile.Instance tile = getTileData(ix, iy);
        if (tile == null) return 0;

        float CHECK_HEIGHT = 100;
        float f = tile.intersectFraction(
                new Vector2f(0, 0),
                new Vector3f(0, 0, CHECK_HEIGHT),
                new Vector3f(0, 0, -1)
        );

        return tile.getHeight() + (CHECK_HEIGHT - f);
    }

    @Override
    public void draw(SGL gl) {
        if (map == null) return;
        assert gl.getPosition(Vectors.O).equals(Vectors.O) : "gl object not placed at origin";

        ShaderProgram shader = gl.getShader();
        boolean isMaterialShader = shader instanceof MaterialShader;
        if (isMaterialShader) {
            ((MaterialShader) shader).setMaterial(Material.ROUGH, new Color4f(85, 153, 0, 1));
        }

        gl.pushMatrix();
        {
            // tile 1 stretches form (0, 0) to (TILE_SIZE, TILE_SIZE)
            gl.translate(TILE_SIZE * 0.5f, TILE_SIZE * 0.5f, 0);

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

            if (cx >= xChunks || cx < 0 || cy >= yChunks || cy < 0) continue;

            MapChunk chunk = map[cx][cy];
            highlightedChunks.add(chunk);

            int rx = c.x() - cx * chunkSize;
            int ry = c.y() - cy * chunkSize;

            chunk.highlight(rx, ry);
        }
    }

    @Override
    public Vector2ic getSize() {
        return new Vector2i(xChunks * chunkSize, yChunks * chunkSize);
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
        out.writeInt(xChunks);
        out.writeInt(yChunks);

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
        this.xChunks = in.readInt();
        this.yChunks = in.readInt();

        // now read chunks themselves
        map = new MapChunk[xChunks][];
        for (int mx = 0; mx < xChunks; mx++) {
            MapChunk[] yStrip = new MapChunk[yChunks];

            for (int my = 0; my < yChunks; my++) {
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

        if (cx >= xChunks || cy >= yChunks) return null;

        MapChunk chunk = map[cx][cy];

        int rx = x - cx * chunkSize;
        int ry = y - cy * chunkSize;
        return chunk.get(rx, ry);
    }

    public void setTile(int x, int y, MapTile.Instance instance) {
        int cx = x / chunkSize;
        int cy = y / chunkSize;

        if (cx >= xChunks || cx < 0 || cy >= yChunks || cy < 0) return;

        MapChunk chunk = map[cx][cy];

        int rx = x - cx * chunkSize;
        int ry = y - cy * chunkSize;

        chunk.set(rx, ry, instance);
    }

    @Override
    public Collection<Vector2i> findPath(
            Vector2ic beginPosition, Vector2ic target, float walkSpeed, float climbSpeed
    ) {
        int xMax = (xChunks * chunkSize) - 1;
        int yMax = (yChunks * chunkSize) - 1;

        return new AStar(beginPosition, target, xMax, yMax) {
            @Override
            public float distanceAdjacent(int x1, int y1, int x2, int y2) {
                // the total duration of this movement
                float duration = 0;

                if (x1 == x2 || y1 == y2) {
                    MapTile.Instance fromTile = getTileData(x1, y1);
                    MapTile.Instance toTile = getTileData(x2, y2);
                    assert fromTile != null && toTile != null;

                    Direction move = Direction.get(x2 - x1, y2 - y1);

                    int fromHeight = fromTile.heightOf(move);
                    int toHeight = toTile.heightOf(move.inverse());

                    // steepness
                    float t1inc = (fromHeight - fromTile.getHeight()) / (TILE_SIZE / 2);
                    float t2inc = (toHeight - toTile.getHeight()) / (TILE_SIZE / 2);

                    // actual duration of walking
                    float walkSpeedT1 = (1f / hypoLength(t1inc)) * walkSpeed;
                    float walkSpeedT2 = (1f / hypoLength(t2inc)) * walkSpeed;

                    // duration += walkspeed(steepness_1) * dist + walkspeed(steepness_2) * dist
                    duration += (walkSpeedT1 + walkSpeedT2) * TILE_SIZE;

                    // height difference on the sides of the tiles
                    float cliffHeight = (toHeight - fromHeight) * TILE_SIZE_Z;

                    // climbing if more than 'an acceptable height'
                    if (cliffHeight > 0) {
                        duration += (cliffHeight / climbSpeed);
                    }

                } else {
                    // TODO allow diagonal tracing
                    Logger.WARN.print(String.format(
                            "Pathfinding (%s) asked for non-adjacent tiles (%d, %d) (%d, %d)",
                            getClass(), x1, y1, x2, y2
                    ));

                    return Float.POSITIVE_INFINITY;
                }

                return duration;
            }

        }.call();
    }

    @Override
    public float intersectFraction(Vector3fc origin, Vector3fc direction, float maximum) {
        if (direction.x() == 0 && direction.y() == 0) return maximum;

        Vector2f coordPos = new Vector2f(origin.x() / TILE_SIZE, origin.y() / TILE_SIZE);
        Vector2f coordDir = new Vector2f(direction.x() / TILE_SIZE, direction.y() / TILE_SIZE); // also scale this vector

        Vector2f worldClip = new Vector2f();
        boolean doIntersect = Intersectionf.intersectRayAab(
                coordPos.x(), coordPos.y(), 0,
                coordDir.x(), coordDir.y(), 0,
                0, 0, -1,
                xChunks * chunkSize - 1, yChunks * chunkSize - 1, 1,
                worldClip
        );
        if (!doIntersect) return maximum;

        float adjMax = Math.min(worldClip.y, maximum);
        float adjMin = Math.max(worldClip.x, 0);

        coordPos.add(new Vector2f(coordDir).mul(adjMin));
        coordDir.mul(adjMax - adjMin);
        Vector2i lineTraverse = new Vector2i((int) coordPos.x, (int) coordPos.y);

        while (lineTraverse != null) {
            int xCoord = lineTraverse.x;
            int yCoord = lineTraverse.y;
            MapTile.Instance tileData = getTileData(xCoord, yCoord);
            if (tileData == null) {
                Logger.WARN.print(String.format(
                        "%s, %s %s is not on the map (ray is %s - %s)",
                        xCoord, yCoord, Vectors.toString(lineTraverse),
                        Vectors.toString(coordPos),
                        Vectors.toString(new Vector2f(coordDir).add(coordPos))
                ));
                return maximum;
            }

            Vector2f tilePosition = new Vector2f((xCoord + 0.5f) * TILE_SIZE, (yCoord + 0.5f) * TILE_SIZE);

            float secFrac = tileData.intersectFraction(tilePosition, origin, direction);

            if (secFrac >= 0 && secFrac < 1) {
                return secFrac;
            }

            // no luck, try next coordinate
            lineTraverse = nextCoordinate(xCoord, yCoord, coordPos, coordDir);
        }

        return maximum;
    }

    /**
     * assuming you are at (xCoord, yCoord), computes the next coordinate hit by the given ray
     * @param xCoord    the current x coordinate
     * @param yCoord    the current y coordinate
     * @param origin    the origin of the ray
     * @param direction the direction of the ray
     * @return the next coordinate hit by the ray. If the previous coordinate was not hit by the ray, return a
     * coordinate closer to the ray than this one (will eventually return coordinates on the ray)
     */
    private Vector2i nextCoordinate(int xCoord, int yCoord, Vector2fc origin, Vector2fc direction) {
        boolean xIsPos = direction.x() > 0;
        boolean yIsPos = direction.y() > 0;

        float xIntersect = Intersectionf.intersectRayPlane(
                origin.x(), origin.y(), 0, direction.x(), direction.y(), 0,
                (xIsPos ? xCoord + 1 : xCoord), yCoord, 0, (xIsPos ? -1 : 1), 0, 0,
                1e-3f
        );
        float yIntersect = Intersectionf.intersectRayPlane(
                origin.x(), origin.y(), 0, direction.x(), direction.y(), 0,
                xCoord, (yIsPos ? yCoord + 1 : yCoord), 0, 0, (yIsPos ? -1 : 1), 0,
                1e-3f
        );

        if (xIntersect >= 1 && yIntersect >= 1) {
            return null;
        }

        Vector2i next = new Vector2i(xCoord, yCoord);

        if (xIntersect <= yIntersect) next.add((xIsPos ? 1 : -1), 0);
        if (yIntersect <= xIntersect) next.add(0, (yIsPos ? 1 : -1));

        return next;
    }
}
