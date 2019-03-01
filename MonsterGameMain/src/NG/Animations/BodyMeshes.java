package NG.Animations;

import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Rendering.MeshLoading.TexturedMesh;
import NG.Rendering.Shapes.FlatMesh;
import NG.Tools.Directory;
import NG.Tools.Logger;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public enum BodyMeshes implements Mesh {
    ROBOT_UPPER_ARM("Robot", "leg1.obj"),
    ROBOT_LOWER_ARM("Robot", "leg2.obj"),
    ROBOT_TORSO("Robot", "torso.obj"),
    ROBOT_EAR("Robot", "leg1.obj"),
    ROBOT_HEAD("Robot", "leg2.obj"),
    ROBOT_FOOT("Robot", "foot.obj"),
    ;

    private final Mesh mesh;

    BodyMeshes(String... filePath) {
        Path path = Directory.meshes.getPath(filePath);

        MeshFile pars;
        try {
            pars = MeshFile.loadFile(path);

        } catch (IOException ex) {
            Logger.ERROR.print(ex);
            mesh = null;
            return;
        }

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
    }}
