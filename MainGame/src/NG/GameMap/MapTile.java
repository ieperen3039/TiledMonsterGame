package NG.GameMap;

import NG.DataStructures.Direction;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Rendering.Shaders.TextureShader;
import NG.Rendering.Shapes.BasicShape;
import NG.Rendering.Shapes.Shape;
import NG.Rendering.Textures.FileTexture;
import NG.Rendering.Textures.Texture;
import NG.Tools.Directory;
import NG.Tools.Logger;
import org.joml.AABBf;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Random;

import static NG.Settings.Settings.TILE_SIZE_Z;

/**
 * a MapTile represents the mesh with its properties, and is fully immutable. Tiles can be queried with methods like
 * {@link MapTiles#getRandomOf(Random, int, int, int, int)} and {@link MapTiles#getByName(String)}.
 * @author Geert van Ieperen created on 3-2-2019.
 * @see Instance
 */
public class MapTile {
    public static final MapTile DEFAULT_TILE = new MapTile();

    private static int nextIdentity = 0; // basic tile has id 0

    public final String name;
    public final int tileID;
    public final MapTiles.RotationFreeFit fit;
    public final EnumSet<TileProperties> properties;
    public final int baseHeight; // height of the middle part, the height the user stands on
    public final TileThemeSet sourceSet;

    // all eight height values in clockwise order
    private final int[] heights;// pp, pm, pn, mn, nn, nm, np, mp
    private final Shape shape;
    private MeshFile meshFile;

    private Mesh mesh; // lazy initialized
    private final Path texturePath;
    private Texture texture = null;

    /**
     * @param name       a unique name for this tile
     * @param meshFile
     * @param hitbox
     * @param texture    the path to the texture of this tile
     * @param properties the properties of this tile
     * @param sourceSet  the tileset where this tile is part of, or null if this is a custom tile.
     */
    MapTile(
            String name, MeshFile meshFile, Shape hitbox, Path texture, int[] heights,
            int baseHeight, EnumSet<TileProperties> properties, TileThemeSet sourceSet
    ) {
        this.name = name;
        this.texturePath = texture;
        this.sourceSet = sourceSet;
        this.tileID = nextIdentity++;
        // the order is important
        this.fit = MapTiles.createRFF(heights[0], heights[2], heights[4], heights[6]);// pp, pn, nn, np
        this.properties = properties;
        this.baseHeight = baseHeight;

        this.heights = heights;
        this.meshFile = meshFile;
        this.shape = hitbox;
        // the order is important
    }

    /** basic tile */
    public MapTile() {
        // circumvent registration due to initialisation of static fields
        this.name = "default";
        this.texturePath = null;
        this.sourceSet = null;
        this.tileID = nextIdentity++;
        this.fit = MapTiles.createRFF(1, 1, 1, 1);// pp, pn, nn, np
        this.properties = EnumSet.noneOf(TileProperties.class);
        this.heights = new int[]{1, 1, 1, 1, 1, 1, 1, 1};
        this.baseHeight = 1;

        Path path = Directory.meshes.getPath("general", "cube.obj");
        this.meshFile = MeshFile.loadFileRequired(path);
        this.shape = new BasicShape(meshFile);
    }

    public int orientationBits() {
        return fit.id;
    }

    @Override
    public String toString() {
        return name;
    }

    public AABBf getBoundingBox() {
        return shape.getBoundingBox();
    }

    private void loadMesh() {
        try {
            mesh = meshFile.getMesh();
            meshFile = null;

            if (texturePath != null) {
                texture = FileTexture.get(texturePath);
            }

        } catch (IOException ex) {
            Logger.ERROR.print(ex);
        }
    }

    public static int index(Direction direction) {
        switch (direction) { // pp, pm, pn, mn, nn, nm, np, mp
            case POSITIVE_X:
                return 1;
            case NEGATIVE_Y:
                return 3;
            case NEGATIVE_X:
                return 5;
            case POSITIVE_Y:
                return 7;
            default:
                throw new IllegalArgumentException(String.valueOf(direction));
        }
    }

    /**
     * @author Geert van Ieperen created on 3-2-2019.
     */
    public static class Instance {

        public final byte offset;
        public final byte rotation;
        public final MapTile type;

        Instance(int offset, int rotation, MapTile type) {
            assert type != null;
            this.offset = (byte) (offset);
            while (rotation < 0) rotation += 4;
            this.rotation = (byte) rotation;
            this.type = type;
        }

        public void draw(SGL gl) {
            gl.pushMatrix();
            {
                gl.translate(0, 0, offset * TILE_SIZE_Z);
                gl.rotateQuarter(0, 0, rotation);

                if (type.mesh == null) {
                    type.loadMesh();
                }

                ShaderProgram shader = gl.getShader();
                if (type.texture != null && shader instanceof TextureShader) {
                    TextureShader tShader = (TextureShader) shader;
                    tShader.setTexture(type.texture);
                }

                gl.render(type.mesh, null);
            }
            gl.popMatrix();
        }

        public Instance replaceWith(MapTile newType) {
            int newRotation = this.rotation + (newType.fit.rotation - type.fit.rotation);

            return new Instance(offset, newRotation, newType);
        }

        public int heightOf(Direction direction) {
            if (direction == Direction.NONE) return getHeight();

            return heightOfInd(index(direction));
        }

        public int heightOfCorner(boolean positiveX, boolean positiveY) {
            int hInd;
            if (positiveX && positiveY) {// pp, pm, pn, mn, nn, nm, np, mp
                hInd = 0;
            } else if (positiveX) {
                hInd = 2;
            } else if (positiveY) {
                hInd = 4;
            } else {
                hInd = 6;
            }

            return heightOfInd(hInd);
        }

        private int heightOfInd(int hInd) {
            int shift = rotation * 2;
            int index = (16 - (shift + hInd)) % 8;
            return offset + type.heights[index]; // heights array replaces baseheight
        }

        public int getHeight() {
            return offset + type.baseHeight;
        }


        /**
         * calculates the fraction t such that (origin + direction * t) lies on this tile, or Float.POSITIVE_INFINITY if
         * it does not hit.
         * @param tilePosition the real position of this tile in the xy-plane
         * @param origin       a local origin of a ray
         * @param dir          the direction of the ray
         * @return fraction t of (origin + direction * t), or Float.POSITIVE_INFINITY if it does not hit.
         */
        public float intersectFraction(Vector2fc tilePosition, Vector3fc origin, Vector3fc dir) {
            // apply position and rotation elements of the tile
            // translation
            Vector3f localOrigin = new Vector3f(
                    origin.x() - tilePosition.x(),
                    origin.y() - tilePosition.y(),
                    origin.z() - offset * TILE_SIZE_Z
            );

            boolean doIntersect = type.shape.getBoundingBox().testRay(
                    localOrigin.x, localOrigin.y, localOrigin.z,
                    dir.x(), dir.y(), dir.z()
            );

            if (!doIntersect) {
                return Float.POSITIVE_INFINITY;
            }

            // rotation
            Vector3f localDirection = new Vector3f(dir);
            for (byte i = 0; i < rotation; i++) {
                //noinspection SuspiciousNameCombination
                localOrigin.set(localOrigin.y, -localOrigin.x, localOrigin.z);
                //noinspection SuspiciousNameCombination
                localDirection.set(localDirection.y, -localDirection.x, localDirection.z);
            }

            return type.shape.getIntersectionScalar(localOrigin, localDirection);
        }
    }
}
