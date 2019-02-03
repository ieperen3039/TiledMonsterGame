package NG.Rendering.Shapes;

import NG.Rendering.MatrixStack.Mesh;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shapes.Primitives.Plane;
import NG.Tools.Directory;
import org.joml.Vector3fc;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * A collection of generic shapes
 * @author Geert van Ieperen. Created on 14-9-2018.
 */
public enum FileShapes implements Mesh, Shape {
    ARROW("general", "arrow.obj"),
    ICOSAHEDRON("general", "icosahedron.obj"),
    INV_CUBE("general", "inverseCube.obj"),
    CUBE("general", "cube.obj"),
    TEXTURED_QUAD("general", "quad.obj");

    private final Mesh mesh;
    private final Shape shape;

    FileShapes(String... path) {
        Path asPath = Directory.meshes.getPath(path);
        ShapeParameters pars = new ShapeParameters(asPath, path[path.length - 1]);

        shape = new BasicShape(pars);

        if (pars.isTextured()) {
            mesh = new TexturedMesh(pars);
        } else {
            mesh = new FlatMesh(pars.vertices, pars.normals, pars.faces);
        }
    }

    @Override
    public void render(SGL.Painter lock) {
        mesh.render(lock);
    }

    @Override
    public void dispose() {
        mesh.dispose();
    }

    @Override
    public Iterable<? extends Plane> getPlanes() {
        return shape.getPlanes();
    }

    @Override
    public Iterable<Vector3fc> getPoints() {
        return shape.getPoints();
    }

    @Override
    public Stream<? extends Plane> getPlaneStream() {
        return shape.getPlaneStream();
    }

    @Override
    public Stream<? extends Vector3fc> getPointStream() {
        return shape.getPointStream();
    }
}
