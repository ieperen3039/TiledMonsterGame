package NG.GameMap;

import NG.DataStructures.Direction;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Rendering.Shaders.TextureShader;
import NG.Rendering.Shapes.GenericShapes;
import NG.Rendering.Shapes.Shape;
import NG.Rendering.Textures.Texture;
import NG.Resources.Resource;
import org.joml.AABBf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Random;

import static NG.Settings.Settings.TILE_SIZE_Z;

/**
 * a MapTile represents the mesh with its properties, and is fully immutable. Tiles can be queried with methods like
 * {@link MapTiles#getRandomOf(Random, int, int, int, int)} and {@link MapTiles#getByName(String)}.
 * @author Geert van Ieperen created on 3-2-2019.
 * @see Instance
 */
public class MapTile
        implements Serializable { // Could have been a resource on itself, but the current system works fine and is very efficient in storage
    public static final MapTile DEFAULT_TILE = new MapTile();

    private static int nextIdentity = 0; // basic tile has id 0

    public final String name;
    public final int tileID;
    public final MapTiles.RotationFreeFit fit;
    public final EnumSet<TileProperties> properties;
    public final int baseHeight; // height of the middle part, the height the user stands on

    // all eight height values in clockwise order
    private final int[] heights; // pp, pm, pn, mn, nn, nm, np, mp

    private final Resource<Shape> shape;
    private final Resource<Mesh> mesh;
    private final Resource<Texture> texture; // may be null

    /**
     * @param name       a unique name for this tile
     * @param meshFile
     * @param shapeFile
     * @param texture    the path to the texture of this tile
     * @param properties the properties of this tile
     */
    MapTile(
            String name, Resource<Mesh> meshFile, Resource<Shape> shapeFile, Resource<Texture> texture, int[] heights,
            int baseHeight, EnumSet<TileProperties> properties
    ) {
        this.name = name;
        this.texture = texture;
        this.tileID = nextIdentity++;
        // the order is important
        this.fit = MapTiles.createRFF(heights[0], heights[2], heights[4], heights[6]);// pp, pn, nn, np
        this.properties = properties;
        this.baseHeight = baseHeight;

        this.heights = heights;
        this.shape = shapeFile;
        this.mesh = meshFile;
    }

    /** basic tile */
    public MapTile() {
        // circumvent registration to allow initialisation of static fields
        this(
                "default tile", GenericShapes.CUBE.meshResource(), GenericShapes.CUBE.shapeResource(), null,
                new int[]{1, 1, 1, 1, 1, 1, 1, 1}, 1, EnumSet.noneOf(TileProperties.class)
        );
    }

    public int orientationBits() {
        return fit.id;
    }

    @Override
    public String toString() {
        return name;
    }

    public AABBf getBoundingBox() {
        return shape.get().getBoundingBox();
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

                ShaderProgram shader = gl.getShader();
                if (type.texture != null && shader instanceof TextureShader) {
                    TextureShader tShader = (TextureShader) shader;
                    tShader.setTexture(type.texture.get());
                }

                gl.render(type.mesh.get(), null);
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
         * @param localOrigin origin relative to this tile, will be modified
         * @param direction   the direction of the ray
         * @return fraction t of (origin + direction * t), or Float.POSITIVE_INFINITY if it does not hit.
         */
        public float intersectFraction(Vector3f localOrigin, Vector3fc direction) {
            Shape shape = type.shape.get();
            boolean doIntersect = shape.getBoundingBox().testRay(
                    localOrigin.x, localOrigin.y, localOrigin.z,
                    direction.x(), direction.y(), direction.z()
            );
            if (!doIntersect) {
                return Float.POSITIVE_INFINITY;
            }

            // rotation
            Vector3f localDirection = new Vector3f(direction);
            for (byte i = 0; i < rotation; i++) {
                //noinspection SuspiciousNameCombination
                localOrigin.set(localOrigin.y, -localOrigin.x, localOrigin.z);
                //noinspection SuspiciousNameCombination
                localDirection.set(localDirection.y, -localDirection.x, localDirection.z);
            }

            return shape.getIntersectionScalar(localOrigin, localDirection);
        }
    }
}
