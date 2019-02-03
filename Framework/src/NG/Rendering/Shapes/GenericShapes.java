package NG.Rendering.Shapes;

import NG.Rendering.MatrixStack.Mesh;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shapes.Primitives.Plane;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.stream.Stream;

/**
 * @author Geert van Ieperen created on 1-2-2019.
 */
public enum GenericShapes implements Mesh, Shape {
    /** a quad of size 2x2 on the xy plane */
    QUAD(makeSingleQuad());

    private final Mesh mesh;
    private final Shape shape;

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
