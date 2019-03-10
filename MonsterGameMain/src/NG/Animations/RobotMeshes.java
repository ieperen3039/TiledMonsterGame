package NG.Animations;

import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Tools.Directory;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.AABBf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public enum RobotMeshes implements BodyMesh {
    ROBOT_UPPER_ARM("Robot", "leg1.obj"),
    ROBOT_LOWER_ARM("Robot", "leg2.obj"),
    ROBOT_TORSO("Robot", "torso.obj"),
    ROBOT_EAR("Robot", "ear.obj"),
    ROBOT_HEAD("Robot", "head.obj"),
    ROBOT_FOOT("Robot", "foot.obj"),
    ;

    private Mesh mesh;
    private final AABBf bounds;
    private MeshFile pars;

    RobotMeshes(String... filePath) {
        Path path = Directory.meshes.getPath(filePath);

        try {
            pars = MeshFile.loadFile(path);

        } catch (IOException ex) {
            Logger.ERROR.print(ex);
            bounds = null;
            return;
        }

        bounds = new AABBf();
        for (Vector3fc vertex : pars.getVertices()) {
            bounds.union(vertex);
        }
    }

    @Override
    public void render(SGL.Painter lock) {
        if (mesh == null) {
            mesh = pars.getMesh();
            pars = null;
        }
        mesh.render(lock);
    }

    @Override
    public void dispose() {
        if (mesh == null) {
            pars = null;
        } else {
            mesh.dispose();
        }
    }

    @Override
    public Vector3f getSize() {
        return Vectors.sizeOf(bounds);
    }
}
