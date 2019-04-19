package NG.GameMap;

import NG.Camera.Camera;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.Game;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Rendering.Shapes.GenericShapes;
import NG.Tools.AStar;
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
 * A map that is built from cubes on the coordinates, which takes the heightmap as exact heights of each block.
 * @author Geert van Ieperen created on 17-2-2019.
 */
public class BlockMap implements GameMap {
    private static final Color4f SELECTION_COLOR = Color4f.BLUE;
    private final List<ChangeListener> changeListeners = new ArrayList<>();
    private Map<Integer, Set<Integer>> highlightedTiles = new HashMap<>();
    private Game game;

    private short[][] map; // strictly positive
    private int xSize;
    private int ySize;

    public BlockMap() {
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
    }

    @Override
    public void generateNew(MapGeneratorMod mapGenerator) {
        float[][] heightMap = mapGenerator.generateHeightMap();

        xSize = heightMap.length;
        ySize = heightMap[0].length;
        short[][] intMap = new short[xSize][ySize];

        // we should copy anyway
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                intMap[x][y] = (short) Math.max(heightMap[x][y], 0);
            }
        }

        map = intMap;

        changeListeners.forEach(ChangeListener::onMapChange);
    }

    public void setTile(int x, int y, int height) {
        if (map == null || x < 0 || y < 0 || x >= xSize || y >= ySize) return;

        map[x][y] = (short) height;

        changeListeners.forEach(ChangeListener::onMapChange);
    }

    @Override
    public float getHeightAt(float x, float y) {
        int ix = (int) (x / TILE_SIZE);
        int iy = (int) (y / TILE_SIZE);

        return getHeightAt(ix, iy) * TILE_SIZE_Z;
    }

    @Override
    public int getHeightAt(int x, int y) {
        if (map == null || x < 0 || y < 0 || x >= xSize || y >= ySize) return 0;

        return map[x][y];
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
                x * TILE_SIZE,
                y * TILE_SIZE,
                getHeightAt(x, y) * TILE_SIZE_Z
        );
    }

    @Override
    public void draw(SGL gl) {
        ShaderProgram shader = gl.getShader();
        MaterialShader mShader = (diffuse, specular, reflectance) -> {};

        boolean doHighlight = shader instanceof MaterialShader;
        if (doHighlight) {
            mShader = (MaterialShader) shader;
            mShader.setMaterial(Material.ROUGH, Color4f.WHITE);
        }

        float totalYTranslation = -1 * ySize * TILE_SIZE;

        gl.pushMatrix();
        {
            for (int x = 0; x < xSize; x++) {
                short[] slice = map[x];

                for (int y = 0; y < ySize; y++) {
                    boolean highlightThis = doHighlight &&
                            highlightedTiles.containsKey(x) &&
                            highlightedTiles.get(x).contains(y);

                    if (highlightThis) {
                        mShader.setMaterial(Material.ROUGH, SELECTION_COLOR);
                    } else {
                        mShader.setMaterial(Material.ROUGH, Color4f.WHITE);
                    }

                    // +/- mesh size (-0.5 * MESH_SIZE)
                    float height = slice[y] * TILE_SIZE_Z;

                    if (height > 0) {
                        gl.scale(1, 1, height);
                        gl.render(GenericShapes.CUBE, null); // todo make half cube
                        gl.scale(1, 1, 1 / height);

                    } else {
                        gl.render(GenericShapes.QUAD, null);
                    }

                    gl.translate(0, TILE_SIZE, 0);
                }
                gl.translate(TILE_SIZE, totalYTranslation, 0);
            }
        }
        gl.popMatrix();
    }

    @Override
    public Vector3f intersectWithRay(Vector3fc origin, Vector3fc direction) {
        Vector3f temp = new Vector3f();

        Vector3fc point = game.get(Camera.class).getFocus();
        float t = Intersectionf.intersectRayPlane(origin, direction, point, Vectors.Z, 1E-6f);

        return origin.add(direction.mul(t, temp), temp);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    @Override
    public void setHighlights(Vector2ic... coordinates) {
        highlightedTiles.clear();
        for (Vector2ic vec : coordinates) {
            Set<Integer> xSet = highlightedTiles.computeIfAbsent(vec.x(), k -> new HashSet<>(4));
            xSet.add(vec.y());
        }
    }

    @Override
    public Vector2ic getSize() {
        return new Vector2i(xSize, ySize);
    }

    @Override
    public boolean checkMouseClick(MouseTool tool, int xSc, int ySc) {
        return checkMouseClick(tool, xSc, ySc, game);
    }

    @Override
    public void cleanup() {
        changeListeners.clear();
    }

    @Override
    public void writeToDataStream(DataOutput out) throws IOException {
        out.writeInt(xSize);
        out.writeInt(ySize);

        for (int x = 0; x < xSize; x++) {
            short[] slice = map[x];
            for (int y = 0; y < ySize; y++) {
                out.writeShort(slice[y]);
            }
        }
    }

    public BlockMap(DataInput in) throws IOException {
        xSize = in.readInt();
        ySize = in.readInt();

        map = new short[xSize][ySize];
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                map[x][y] = in.readShort();
            }
        }
    }

    @Override
    public Collection<Vector2i> findPath(
            Vector2ic source, Vector2ic target, float walkSpeed, float climbSpeed
    ) {
        return new AStar(source, target, xSize - 1, ySize - 1) {
            @Override
            public float distanceAdjacent(int x1, int y1, int x2, int y2) {
                int dx = x1 - x2;
                int dy = y1 - y2;

                float distDiff = (dx == 0 || dy == 0) ?
                        (float) Math.abs(dx + dy) : // manhattan distance
                        (float) Math.sqrt(dx * dx + dy * dy); // real distance

                float heightDiff = getHeightAt(x2, y2) - getHeightAt(x1, y1);
                float climbTime = (heightDiff > 0) ? (climbSpeed * heightDiff * TILE_SIZE_Z) : 0;

                return (walkSpeed * distDiff * TILE_SIZE) + climbTime;
            }

        }.call();
    }


}
