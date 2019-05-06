package NG.Rendering.MeshLoading;

import NG.DataStructures.Generic.Color4f;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Geert van Ieperen created on 6-5-2018.
 */
public class OBJFile implements MeshFile {
    private final List<Vector2fc> textureCoords;
    private final List<Vector3fc> vertices;
    private final List<Vector3fc> normals;
    private final List<Mesh.Face> faces;
    private final String name;

    /**
     * @param offSet offset of the gravity middle in this mesh as the negative of the vector to the gravity middle
     * @param scale  the scaling applied to the loaded object
     * @param path   the path to the object
     * @param name   debug name of the shape
     */
    public OBJFile(Vector3fc offSet, Vector3fc scale, Path path, String name) throws IOException {
        this.name = name;
        vertices = new ArrayList<>();
        normals = new ArrayList<>();
        textureCoords = new ArrayList<>();
        faces = new ArrayList<>();

        List<String> lines = Files.readAllLines(path);

        for (String line : lines) {
            String[] tokens = Toolbox.WHITESPACE_PATTERN.split(line);
            switch (tokens[0]) {
                case "v":
                    // Geometric vertex
                    vertices.add(new Vector3f(
                                    Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                            )
                                    .mul(scale)
                                    .add(offSet)
                    );
                    break;
                case "vn":
                    // Vertex normal
                    normals.add(new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    ));
                    break;
                case "vt":
                    textureCoords.add(new Vector2f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2])
                    ));
                    break;
                case "f":
                    faces.add(Mesh.Face.parseOBJ(tokens));
                    break;
                default:
                    // Ignore other lines
                    break;
            }
        }

        if (vertices.isEmpty() || faces.isEmpty()) {
            Logger.ERROR.print("Empty mesh loaded: " + path + " (this may result in errors)");
        }
    }

    @Override
    public List<Vector2fc> getTextureCoords() {
        return textureCoords;
    }

    @Override
    public List<Vector3fc> getVertices() {
        return vertices;
    }

    @Override
    public List<Vector3fc> getNormals() {
        return normals;
    }

    @Override
    public List<Color4f> getColors() {
        return Collections.emptyList();
    }

    @Override
    public List<Mesh.Face> getFaces() {
        return faces;
    }

    @Override
    public String toString() {
        return "WaveFront file " + name;
    }
}
