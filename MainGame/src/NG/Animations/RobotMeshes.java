package NG.Animations;

import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Resources.Resource;
import NG.Tools.Directory;
import org.joml.AABBf;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public enum RobotMeshes implements Mesh {
    ROBOT_UPPER_ARM("robot", "leg1.obj"),
    ROBOT_LOWER_ARM("robot", "leg2.obj"),
    ROBOT_TORSO("robot", "torso.obj"),
    ROBOT_EAR("robot", "ear.obj"),
    ROBOT_HEAD("robot", "head.obj"),
    ROBOT_FOOT("robot", "foot.obj"),
    ROBOT_CLAW("robot", "finger.obj"),

    ;

    private Resource<Mesh> mesh;
    private final AABBf bounds;

    RobotMeshes(String... filePath) {
        Resource<MeshFile> meshFile = MeshFile.createResource(Directory.meshes, filePath);
        mesh = Resource.derive(meshFile, MeshFile::getMesh, Mesh::dispose);

        bounds = new AABBf();
        for (Vector3fc vertex : meshFile.get().getVertices()) {
            bounds.union(vertex);
        }
    }

    public Resource<Mesh> meshResource() {
        return mesh;
    }

    @Override
    public void render(SGL.Painter lock) {
        mesh.get().render(lock);
    }

    @Override
    public void dispose() {
        mesh.drop();
    }

    public AABBf getBounds() {
        return bounds;
    }
}
