package NG.Rendering.Shapes;

import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Rendering.Shapes.Primitives.Plane;
import NG.Resources.GeneratorResource;
import NG.Resources.Resource;
import NG.Tools.Directory;
import org.joml.AABBf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

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
    SELECTION("general", "selection.obj"),

    /** a quad of size 2x2 on the xy plane */
    QUAD(makeSingleQuad()),
    ;

    private Resource<Mesh> mesh;
    private Shape shape; // we use the actual shape as this is an enum

    GenericShapes(String... path) {
        Resource<MeshFile> pars = MeshFile.createResource(Directory.meshes, path);
        shape = pars.get().getShape();
        mesh = Resource.derive(pars, MeshFile::getMesh, Mesh::dispose);
    }

    GenericShapes(CustomShape frame) {
        shape = frame.toShape();
        mesh = new GeneratorResource<>(frame::toFlatMesh, Mesh::dispose);
    }

    public Resource<Mesh> meshResource() {
        return mesh;
    }

    public Resource<Shape> shapeResource() {
        return new GeneratorResource<>(() -> shape, null);
    }

    @Override
    public void render(SGL.Painter lock) {
        mesh.get().render(lock);
    }

    @Override
    public void dispose() {
        mesh.drop();
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
