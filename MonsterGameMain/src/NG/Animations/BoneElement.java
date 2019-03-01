package NG.Animations;

import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public class BoneElement {
    private final Mesh mesh;
    private final Vector3fc scaling;

    public BoneElement(Mesh mesh, Vector3fc scaling) {
        this.mesh = mesh;
        this.scaling = scaling;
    }

    public void draw(SGL gl, Entity entity) {
        gl.pushMatrix();
        {
            gl.scale(scaling);
            gl.render(mesh, entity);
        }
        gl.popMatrix();
    }

    /**
     * @return the scaling applied on this mesh, such that the joint positions also get displaced by that amount
     */
    public Vector3fc scalingFactor() {
        return scaling;
    }
}
