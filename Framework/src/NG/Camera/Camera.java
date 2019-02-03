package NG.Camera;

import NG.ActionHandling.MouseScrollListener;
import NG.Engine.GameAspect;
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
}
