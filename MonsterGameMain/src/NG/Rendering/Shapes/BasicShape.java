package NG.Rendering.Shapes;

import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Rendering.Shapes.Primitives.Plane;
import NG.Tools.Logger;
import org.joml.Vector3fc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Geert van Ieperen created on 11-11-2017.
 */
public class BasicShape implements Shape {

    private List<Vector3fc> vertices;
    private List<Plane> triangles;

    public BasicShape(MeshFile model) {
        this(model.getVertices(), model.getNormals(), model.getFaces());

        Logger.DEBUG.printf("loaded %s: [Faces: %d, vertices: %d]",
                model, model.getFaces().size(), model.getVertices().size());
    }

    /**
     * reads a model from the given file.
     */
    public BasicShape(
            List<Vector3fc> vertices, List<Vector3fc> normals, List<Mesh.Face> faces
    ) {
        this.vertices = Collections.unmodifiableList(vertices);
        this.triangles = faces.stream()
                .parallel()
                .map(f -> f.toPlanes(vertices, normals))
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<? extends Plane> getPlanes() {
        return Collections.unmodifiableList(triangles);
    }

    @Override
    public Iterable<Vector3fc> getPoints() {
        return Collections.unmodifiableList(vertices);
    }

    @Override
    public Stream<? extends Plane> getPlaneStream() {
        return triangles.stream();
    }

    @Override
    public Stream<? extends Vector3fc> getPointStream() {
        return vertices.stream();
    }

}
