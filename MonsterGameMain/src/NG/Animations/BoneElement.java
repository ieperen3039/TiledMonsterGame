package NG.Animations;

import NG.DataStructures.Generic.Color4f;
import NG.Entities.Entity;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Tools.Vectors;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public class BoneElement {
    private final Mesh mesh;
    private final Material mat;
    private final Vector3fc scaling;
    private final boolean doScale;

    /**
     * @param mesh    what to draw for this bone, may be null
     * @param scaling scaling relative to original bone
     * @param material
     */
    public BoneElement(Mesh mesh, Vector3fc scaling, Material material) {
        this.mesh = mesh;
        this.scaling = scaling;
        doScale = !(scaling.equals(Vectors.Scaling.UNIFORM));
        mat = material;
    }

    public void draw(SGL gl, Entity entity) {
        if (mesh == null) return;

        ShaderProgram shader = gl.getShader();
        if (shader instanceof MaterialShader) {
            ((MaterialShader) shader).setMaterial(mat, Color4f.WHITE);
        }

        if (doScale) {
            gl.pushMatrix();
            {
                gl.scale(scaling);
                gl.render(mesh, entity);
            }
            gl.popMatrix();

        } else {
            gl.render(mesh, entity);
        }
    }

    /**
     * @return the scaling applied on this mesh, such that the joint positions also get displaced by that amount
     */
    public Vector3fc scaling() {
        return scaling;
    }
}
