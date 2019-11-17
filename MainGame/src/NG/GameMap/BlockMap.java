package NG.GameMap;

import NG.Camera.Camera;
import NG.Core.Game;
import NG.DataStructures.Generic.Color4f;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Rendering.Shapes.GenericShapes;
import NG.Tools.AStar;
import org.joml.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Math;
import java.util.*;

import static NG.Settings.Settings.TILE_SIZE;
import static NG.Settings.Settings.TILE_SIZE_Z;

/**
 * A map that is built from cubes on the coordinates, which takes the heightmap as exact heights of each block.
 * @author Geert van Ieperen created on 17-2-2019.
 */
public class BlockMap extends AbstractMap {
    private static final Color4f SELECTION_COLOR = Color4f.BLUE;
    private static final Color4f WHITE = Color4f.WHITE;

    private final List<ChangeListener> changeListeners = new ArrayList<>();
    private Map<Integer, Set<Integer>> highlightedTiles = new HashMap<>();
    private Game game;

    private float hBlockSize;
    private float hBlockHeight;
    private float blockElevation;

    private short[][] map; // strictly positive
    private int xSize;
    private int ySize;

    public BlockMap() {
        this(TILE_SIZE, 4f * TILE_SIZE_Z, 0f);
    }

    public BlockMap(float blockSize, float blockHeight, float blockElevation) {
        this.hBlockSize = blockSize / 2;
        this.blockElevation = blockElevation;
        hBlockHeight = blockHeight / 2;
    }

    public BlockMap(DataInputStream in) throws IOException {
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
    public void init(Game game) throws Exception {
        this.game = game;
    }

    @Override
    public void generateNew(MapGeneratorMod mapGenerator) {
        float[][] heightMap = mapGenerator.generateHeightMap();

        int xSize = heightMap.length;
        int ySize = heightMap[0].length;
        short[][] intMap = new short[xSize][ySize];

        // we must copy anyway
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                intMap[x][y] = (short) heightMap[x][y];
            }
        }

        synchronized (this) {
            this.map = intMap;
            this.xSize = xSize;
            this.ySize = ySize;
        }

        changeListeners.forEach(ChangeListener::onMapChange);
    }

    public void setTile(int x, int y, int height) {
        if (map == null || x < 0 || y < 0 || x >= xSize || y >= ySize) return;

        map[x][y] = (short) height;

        changeListeners.forEach(ChangeListener::onMapChange);
    }

    @Override
    public float getHeightAt(float x, float y) {
        int ix = (int) ((x + 0.5f) / TILE_SIZE);
        int iy = (int) ((y + 0.5f) / TILE_SIZE);

        return getHeightAt(ix, iy) * TILE_SIZE_Z;
    }

    @Override
    public int getHeightAt(int x, int y) {
        if (map == null || x < 0 || y < 0 || x >= xSize || y >= ySize) return 0;

        return map[x][y];
    }

    @Override
    public Vector2i getCoordinate(Vector3fc position) {
        return new Vector2i(
                (int) (position.x() / TILE_SIZE + 0.5f),
                (int) (position.y() / TILE_SIZE + 0.5f)
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
        if (map == null) return;
        ShaderProgram shader = gl.getShader();
        MaterialShader mShader = (diffuse, specular, reflectance) -> {};

        boolean doHighlight = shader instanceof MaterialShader;
        if (doHighlight) {
            mShader = (MaterialShader) shader;
            mShader.setMaterial(Material.ROUGH, WHITE);
        }

        Camera camera = game.get(Camera.class);
        Vector3fc eye = camera.getEye();
        Vector3fc viewDir = camera.vectorToFocus();
        float fraction = gridMapIntersection(eye, viewDir);
        Vector3fc focus = new Vector3f(viewDir).mul(fraction).add(eye);
        float radius = viewDir.length() / TILE_SIZE + 10;

        synchronized (this) {
            int xMin = (int) Math.max(0, (focus.x() - radius) / TILE_SIZE);
            int yMin = (int) Math.max(0, (focus.y() - radius) / TILE_SIZE);
            int xMax = (int) Math.min(xSize, (focus.x() + radius) / TILE_SIZE);
            int yMax = (int) Math.min(ySize, (focus.y() + radius) / TILE_SIZE);

            float totalYTranslation = (yMax - yMin) * TILE_SIZE;

            gl.pushMatrix();
            {
                gl.translate(xMin * TILE_SIZE, yMin * TILE_SIZE, 0);
                // tile 1 stretches from (-TILE_SIZE / 2, -TILE_SIZE / 2) to (TILE_SIZE / 2, TILE_SIZE / 2)

                for (int x = xMin; x < xMax; x++) {
                    short[] slice = map[x];

                    for (int y = yMin; y < yMax; y++) {
                        boolean highlightThis = doHighlight &&
                                highlightedTiles.containsKey(x) &&
                                highlightedTiles.get(x).contains(y);

                        if (highlightThis) {
                            mShader.setMaterial(Material.ROUGH, SELECTION_COLOR);
                        } else {
                            mShader.setMaterial(Material.ROUGH, WHITE);
                        }

                        float height = slice[y] * TILE_SIZE_Z;

                        float offset = height - hBlockHeight + blockElevation;

                        gl.translate(0, 0, offset);
                        gl.scale(hBlockSize, hBlockSize, hBlockHeight);

                        gl.render(GenericShapes.CUBE, null); // todo make half cube

                        gl.scale(1 / hBlockSize, 1 / hBlockSize, 1 / hBlockHeight);
                        gl.translate(0, 0, -offset);

                        gl.translate(0, TILE_SIZE, 0);
                    }
                    gl.translate(TILE_SIZE, -totalYTranslation, 0);
                }
            }
            gl.popMatrix();
        }
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
    public void writeToDataStream(DataOutputStream out) throws IOException {
        synchronized (this) {
            out.writeInt(xSize);
            out.writeInt(ySize);

            for (int x = 0; x < xSize; x++) {
                short[] slice = map[x];
                for (int y = 0; y < ySize; y++) {
                    out.writeShort(slice[y]);
                }
            }
        }
    }

    @Override
    public Collection<Vector2i> findPath(
            Vector2ic source, Vector2ic target, float walkSpeed, float climbSpeed
    ) {
        return new AStar(source, target, 0, 0, xSize - 1, ySize - 1) {
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

    @Override
    public Float getTileIntersect(Vector3fc origin, Vector3fc direction, int xCoord, int yCoord) {
        if (xCoord < 0 || xCoord >= xSize || yCoord < 0 || yCoord >= ySize) return null;

        Vector3f p = getPosition(xCoord, yCoord);//.sub(TILE_SIZE / 2, TILE_SIZE / 2, 0);
        float top = p.z + blockElevation;

        Vector2f result = new Vector2f();

        boolean doIntersect = Intersectionf.intersectRayAab(
                origin.x(), origin.y(), origin.z(),
                direction.x(), direction.y(), direction.z(),
                p.x - hBlockSize, p.y - hBlockSize, top - 2 * hBlockHeight,
                p.x + hBlockSize, p.y + hBlockSize, top,
                result
        );

        return doIntersect ? result.x : Float.POSITIVE_INFINITY;
    }
}
