package NG.Rendering.Shapes;

import NG.Rendering.MatrixStack.Mesh;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Geert van Ieperen created on 6-5-2018.
 */
public class ShapeParameters {
    public final List<Vector2fc> textureCoords;
    public final List<Vector3fc> vertices;
    public final List<Vector3fc> normals;
    public final List<Mesh.Face> faces;
    public final String name;

    /**
     * calls {@link #ShapeParameters(Vector3fc, float, Path, String)} on the file of the given path without offset and
     * scale of 1
     * @param path the path to the file to read
     * @param name the generic name of this shape
     */
    public ShapeParameters(Path path, String name) throws IOException {
        this(Vectors.O, 1f, path, name);
    }

    /**
     * @param offSet offset of the gravity middle in this mesh as the negative of the vector to the gravity middle
     * @param scale  the scaling applied to the loaded object
     * @param path   the path to the object
     * @param name   debug name of the shape
     */
    public ShapeParameters(Vector3fc offSet, float scale, Path path, String name) throws IOException {
        this.name = name;
        vertices = new ArrayList<>();
        normals = new ArrayList<>();
        textureCoords = new ArrayList<>();
        faces = new ArrayList<>();

        List<String> lines = openMesh(path);

        for (String line : lines) {
            String[] tokens = Toolbox.SPACES.split(line);
            switch (tokens[0]) {
                case "v":
                    // Geometric vertex
                    vertices.add(new Vector3f(
                                    Float.parseFloat(tokens[3]),
                                    Float.parseFloat(tokens[1]),
                                    Float.parseFloat(tokens[2])
                            )
                                    .mul(scale)
                                    .add(offSet)
                    );
                    break;
                case "vn":
                    // Vertex normal
                    normals.add(new Vector3f(
                            Float.parseFloat(tokens[3]),
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2])
                    ));
                    break;
                case "vt":
                    textureCoords.add(new Vector2f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2])
                    ));
                    break;
                case "f":
                    faces.add(new Mesh.Face(tokens));
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

    public boolean isTextured() {
        return !textureCoords.isEmpty();
    }

    private static List<String> openMesh(Path path) throws IOException {
        return Files.readAllLines(path);
    }
}
