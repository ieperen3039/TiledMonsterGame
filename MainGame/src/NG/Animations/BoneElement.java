package NG.Animations;

import NG.DataStructures.Generic.Color4f;
import NG.Entities.Entity;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Resources.Resource;

import java.io.Serializable;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public class BoneElement implements Serializable {
    private final Resource<Mesh> mesh;
    private final Material mat;

    /**
     * @param mesh     what to draw for this bone, may be null
     * @param material
     */
    public BoneElement(Resource<Mesh> mesh, Material material) {
        this.mesh = mesh;
        mat = material;
    }

    public void draw(SGL gl, Entity entity) {
        if (mesh == null) return;

        ShaderProgram shader = gl.getShader();
        if (shader instanceof MaterialShader) {
            ((MaterialShader) shader).setMaterial(mat, Color4f.WHITE);
        }

        gl.render(mesh.get(), entity);
    }
}
