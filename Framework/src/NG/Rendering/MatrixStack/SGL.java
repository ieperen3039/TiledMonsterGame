package NG.Rendering.MatrixStack;

import NG.Camera.Camera;
import NG.Entities.Entity;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Settings.Settings;
import org.joml.Matrix4f;

/**
 * This resembles the {@link org.lwjgl.opengl.GL} object.
 * @author Geert van Ieperen created on 15-11-2017.
 */
public interface SGL extends MatrixStack {

    /**
     * instructs the graphical card to render the specified mesh
     * @param object       A Mesh that has not been disposed.
     * @param sourceEntity the entity that is currently drawn
     */
    void render(Mesh object, Entity sourceEntity);

    /** @return the shader that is used for rendering. */
    ShaderProgram getShader();

    /**
     * Objects should use GPU calls only in their render method. To prevent invalid uses of the {@link
     * Mesh#render(Painter)} object, a Painter object is required to call that render method.
     */
    class Painter {
        /**
         * Objects should call GPU calls only in their render method. This render method may only be called by a GL2
         * object, to prevent drawing calls while the GPU is not initialized. For this reason, the Painter constructor
         * is protected.
         */
        protected Painter() {
        }
    }

    /**
     * Calculates a projection matrix based on a camera position and the given parameters of the viewport
     * @param windowWidth  the width of the viewport in pixels
     * @param windowHeight the height of the viewport in pixels
     * @param camera       the camera position and orientation.
     * @param isometric    if true, an isometric projection will be calculated. Otherwise a perspective transformation
     *                     is used.
     * @return a projection matrix, such that modelspace vectors multiplied with this matrix will be transformed to
     * viewspace.
     */
    static Matrix4f getViewProjection(float windowWidth, float windowHeight, Camera camera, boolean isometric) {
        Matrix4f vpMatrix = new Matrix4f();

        // Set the projection.
        float aspectRatio = windowWidth / windowHeight;

        if (isometric) {
            float visionSize = camera.vectorToFocus().length();
            vpMatrix.orthoSymmetric(aspectRatio * visionSize, visionSize, Settings.Z_NEAR, Settings.Z_FAR);
        } else {
            vpMatrix.setPerspective(Settings.FOV, aspectRatio, Settings.Z_NEAR, Settings.Z_FAR);
        }

        // set the view
        vpMatrix.lookAt(
                camera.getEye(),
                camera.getFocus(),
                camera.getUpVector()
        );

        return vpMatrix;
    }
}
