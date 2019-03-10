package NG.Animations;

import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Tools.Vectors;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public class BoneElement {
    private final Mesh mesh;
    private final Vector3fc scaling;
    private final boolean doScale;

    /**
     * @param mesh    what to draw for this bone, may be null
     * @param scaling scaling relative to original bone
     */
    public BoneElement(Mesh mesh, Vector3fc scaling) {
        this.mesh = mesh;
        this.scaling = scaling;
        doScale = !(scaling.equals(Vectors.Scaling.UNIFORM));
    }

    public void draw(SGL gl, Entity entity) {
        if (mesh == null) return;

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
