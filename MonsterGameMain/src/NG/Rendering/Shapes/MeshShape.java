package NG.Rendering.Shapes;

import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Rendering.MeshLoading.TexturedMesh;
import NG.Rendering.Shapes.Primitives.Plane;
import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * A mesh that is also a shape.
 * @author Geert van Ieperen. Created on 14-9-2018.
 */
public class MeshShape implements Mesh, Shape {
    private final Mesh mesh;
    private final Shape shape;

    public MeshShape(Path path) throws IOException {
        MeshFile pars = MeshFile.loadFile(path);

        shape = new BasicShape(pars);

        if (pars.isTextured()) {
            mesh = new TexturedMesh(pars);
        } else {
            mesh = new FlatMesh(pars.getVertices(), pars.getNormals(), pars.getFaces());
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
