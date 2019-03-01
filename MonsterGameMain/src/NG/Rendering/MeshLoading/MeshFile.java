package NG.Rendering.MeshLoading;

import NG.DataStructures.Generic.Color4f;
import NG.Tools.Vectors;
import org.joml.Vector2fc;
import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public interface MeshFile {

    default boolean isTextured() {
        return !getTextureCoords().isEmpty();
    }

    default boolean isColored() {
        return !getColors().isEmpty();
    }

    List<Vector2fc> getTextureCoords();

    List<Vector3fc> getVertices();

    List<Vector3fc> getNormals();

    List<Color4f> getColors();

    List<Mesh.Face> getFaces();

    static MeshFile loadFile(Path file) throws IOException {
        return loadFile(file, Vectors.O, 1f);
    }

    static MeshFile loadFile(Path file, Vector3fc offset, float scaling) throws IOException {
        String fileName = file.getFileName().toString();

        assert fileName.contains(".");
        String extension = fileName.substring(fileName.lastIndexOf('.'));

        switch (extension) {
            case ".obj":
                return new OBJFile(offset, scaling, file, fileName);
            case ".ply":
                return new PLYFile(offset, scaling, file, fileName);
            default:
                throw new UnsupportedMeshFileException(fileName);
        }
    }

    class UnsupportedMeshFileException extends IOException {
        public UnsupportedMeshFileException(String fileName) {
            super(fileName);
        }
    }
}
