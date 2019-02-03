package NG.Rendering.Shapes;

import NG.Rendering.MatrixStack.Mesh;
import NG.Rendering.Shapes.Primitives.Plane;
import NG.Rendering.Shapes.Primitives.Quad;
import NG.Rendering.Shapes.Primitives.Triangle;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Geert van Ieperen created on 11-11-2017.
 */
public class BasicShape implements Shape {

    private List<Vector3fc> vertices;
    private List<Plane> triangles;

    public BasicShape(ShapeParameters model) {
        this(model.vertices, model.normals, model.faces);

        Logger.DEBUG.printf("loaded model %s: [Faces: %d, vertices: %d]",
                model.name, model.faces.size(), model.vertices.size());
    }

    /**
     * reads a model from the given file.
     */
    public BasicShape(
            List<Vector3fc> vertices, List<Vector3fc> normals, List<Mesh.Face> faces
    ) {
        this.vertices = Collections.unmodifiableList(vertices);
        this.triangles = faces.stream()
                .map(f -> BasicShape.toPlanes(f, vertices, normals))
                .collect(Collectors.toList());
    }

    /**
     * loads a mesh, splitting it into sections of size containersize.
     * @param containerSize size of splitted container, which is applied in 3 dimensions
     * @param scale         possible scaling factor upon loading
     * @param path          path to the .obj file without extension
     * @param debugName     a name to identify this shape
     * @return a list of shapes, each being roughly containersize in size
     */
    public static List<Shape> loadSplit(float containerSize, float scale, Path path, String debugName) {
        ShapeParameters file = new ShapeParameters(Vectors.zeroVector(), scale, path, debugName);
        HashMap<Vector3i, CustomShape> world = new HashMap<>();

        for (Mesh.Face f : file.faces) {
            Vector3fc[] edges = new Vector3fc[f.size()];
            Vector3f minimum = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
            for (int i = 0; i < f.size(); i++) {
                Vector3fc p = file.vertices.get(f.vert[i]);
                minimum.min(p);
                edges[i] = p;
            }

            int x = (int) (minimum.x / containerSize);
            int y = (int) (minimum.y / containerSize);
            int z = (int) (minimum.z / containerSize);

            Vector3i key = new Vector3i(x, y, z);
            CustomShape container = world.computeIfAbsent(key, k ->
                    new CustomShape(new Vector3f(x + 0.5f, y + 0.5f, -Float.MAX_VALUE))
            );

            Vector3f normal = new Vector3f();
            for (int ind : f.norm) {
                if (ind < 0) continue;
                normal.add(file.normals.get(ind));
            }
            if (Vectors.isScalable(normal)) {
                normal.normalize();
            } else {
                normal = null;
                Logger.DEBUG.printSpamless(file.name, file.name + " has at least one not-computed normal");
            }

            container.addPlane(normal, edges);
        }

        Collection<CustomShape> containers = world.values();
        Logger.DEBUG.print("Loaded model " + file.name + " in " + containers.size() + " parts");

        List<Shape> shapes = new ArrayList<>();
        for (CustomShape frame : containers) {
            shapes.add(frame.wrapToShape());
        }
        return shapes;
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

    /**
     * creates a plane object, using the indices on the given lists
     * @param vertices a list where the vertex indices of A, B and C refer to
     * @param normals  a list where the normal indices of A, B and C refer to
     * @return a triangle whose normal is the average of those of A, B and C, in Shape-space
     */
    public static Plane toPlanes(Mesh.Face face, List<Vector3fc> vertices, List<Vector3fc> normals) {
        final Vector3fc[] border = new Vector3fc[face.size()];
        Arrays.setAll(border, i -> vertices.get(face.vert[i]));
        // take average normal as normal of plane, or use default method if none are registered
        Vector3f normal = new Vector3f();
        for (int index : face.norm) {
            if (index >= 0) normal.add(normals.get(index));
        }

        switch (face.size()) {
            case 3:
                return Triangle.createTriangle(border[0], border[1], border[2], normal);
            case 4:
                return Quad.createQuad(border[0], border[1], border[2], border[3], normal);
            default:
                throw new UnsupportedOperationException("polygons with " + face.size() + " edges are not supported");
        }
    }

}
