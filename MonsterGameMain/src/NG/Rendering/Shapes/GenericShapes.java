package NG.Rendering.Shapes;

import NG.Rendering.MatrixStack.Mesh;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shapes.Primitives.Plane;
import NG.Tools.Directory;
import NG.Tools.Logger;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * A collection of generic shapes
 * @author Geert van Ieperen. Created on 14-9-2018.
 */
public enum GenericShapes implements Mesh, Shape {
    ARROW("general", "arrow.obj"),
    ICOSAHEDRON("general", "icosahedron.obj"),
    INV_CUBE("general", "inverseCube.obj"),
    CUBE("general", "cube.obj"),
    TEXTURED_QUAD("general", "quad.obj"),

    /** a quad of size 2x2 on the xy plane */
    QUAD(makeSingleQuad());

    private final Mesh mesh;
    private final Shape shape;

    GenericShapes(String... path) {
        Path asPath = Directory.meshes.getPath(path);

        ShapeParameters pars;
        try {
            pars = new ShapeParameters(asPath, path[path.length - 1]);

        } catch (IOException ex) {
            Logger.ERROR.print(ex);
            shape = null;
            mesh = null;
            return;
        }

        shape = new BasicShape(pars);

        if (pars.isTextured()) {
            mesh = new TexturedMesh(pars);
        } else {
            mesh = new FlatMesh(pars.vertices, pars.normals, pars.faces);
        }
    }

    GenericShapes(CustomShape frame) {
        mesh = frame.asFlatMesh();
        shape = frame.wrapToShape();
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

    private static CustomShape makeSingleQuad() {
        CustomShape frame = new CustomShape();
        frame.addQuad(
                new Vector3f(1, 1, 0),
                new Vector3f(-1, 1, 0),
                new Vector3f(-1, -1, 0),
                new Vector3f(1, -1, 0),
                new Vector3f(0, 0, 1)
        );
        return frame;
    }
}
