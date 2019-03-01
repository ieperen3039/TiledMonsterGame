package NG.Rendering.MeshLoading;

import NG.DataStructures.Generic.Color4f;
import org.joml.Vector2fc;
import org.joml.Vector3fc;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public class PLYFile implements MeshFile {
    private final String name;

    public PLYFile(Vector3fc offSet, float scale, Path path, String name) {
        this.name = name;
    }

    @Override
    public List<Vector2fc> getTextureCoords() {
        return null;
    }

    @Override
    public List<Vector3fc> getVertices() {
        return null;
    }

    @Override
    public List<Vector3fc> getNormals() {
        return null;
    }

    @Override
    public List<Color4f> getColors() {
        return null;
    }

    @Override
    public List<Mesh.Face> getFaces() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "PLY file " + getName();
    }
}
