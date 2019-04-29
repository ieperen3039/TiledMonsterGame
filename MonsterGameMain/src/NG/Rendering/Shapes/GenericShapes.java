package NG.Rendering.Shapes;

import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Rendering.Shapes.Primitives.Plane;
import NG.Tools.Directory;
import NG.Tools.Logger;
import org.joml.AABBf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

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

        MeshFile pars;
        try {
            pars = MeshFile.loadFile(asPath);

        } catch (IOException ex) {
            Logger.ERROR.print(ex);
            shape = null;
            mesh = null;
            return;
        }

        shape = new BasicShape(pars);
        mesh = pars.getMesh();
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
