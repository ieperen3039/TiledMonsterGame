package NG.Rendering.Shapes;

import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Rendering.Shapes.Primitives.Plane;
import org.joml.AABBf;
import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

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
        mesh = pars.getMesh();
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
    public Collection<? extends Plane> getPlanes() {
        return shape.getPlanes();
    }

    @Override
    public Collection<Vector3fc> getPoints() {
        return shape.getPoints();
    }

    @Override
    public AABBf getBoundingBox() {
        return shape.getBoundingBox();
    }
}
