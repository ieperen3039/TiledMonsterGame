package NG.Camera;

import NG.ActionHandling.MouseScrollListener;
import NG.Engine.GameAspect;
import NG.Settings.Settings;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

/**
 * A camera class manages movement and position of the camera. The actual implementation of creating the perspective
 * matrix is done based on the values of {@link #getEye()}, {@link #getFocus()} and {@link #getUpVector()}
 * @author Geert van Ieperen created on 29-10-2017.
 * @see TycoonFixedCamera
 */
public interface Camera extends GameAspect, MouseScrollListener {
    /**
     * a copy of the direction vector of the eye of the camera to the focus of the camera.
     * @return {@link #getEye()}.to({@link #getFocus()}) The length of this vector may differ by implementation
     */
    Vector3fc vectorToFocus();

    /**
     * updates the state of this camera according to the given passed time.
     * @param deltaTime the number of seconds passed since last update. This may be real-time or in-game time
     */
    void updatePosition(float deltaTime);

    /** a copy of the position of the camera itself */
    Vector3fc getEye();

    /** a copy of the point in space where the camera looks to */
    Vector3fc getFocus();

    /** a copy of the direction of up, the length of this vector is undetermined. */
    Vector3fc getUpVector();

    void set(Vector3fc focus, Vector3fc eye);

    /**
     * Calculates a projection matrix based on a camera position and the given parameters of the viewport
     * @param windowWidth  the width of the viewport in pixels
     * @param windowHeight the height of the viewport in pixels
     * @param isometric    if true, an isometric projection will be calculated. Otherwise a perspective transformation
     *                     is used.
     * @return a projection matrix, such that modelspace vectors multiplied with this matrix will be transformed to
     * viewspace.
     */
    default Matrix4f getViewProjection(float windowWidth, float windowHeight, boolean isometric) {
        Matrix4f vpMatrix = new Matrix4f();

        // Set the projection.
        float aspectRatio = windowWidth / windowHeight;

        if (isometric) {
            float visionSize = vectorToFocus().length() - Settings.Z_NEAR;
            visionSize /= 4;
            vpMatrix.orthoSymmetric(aspectRatio * visionSize, visionSize, Settings.Z_NEAR, Settings.Z_FAR);
        } else {
            vpMatrix.setPerspective(Settings.FOV, aspectRatio, Settings.Z_NEAR, Settings.Z_FAR);
        }

        // set the view
        vpMatrix.lookAt(
                getEye(),
                getFocus(),
                getUpVector()
        );

        return vpMatrix;
    }
}
