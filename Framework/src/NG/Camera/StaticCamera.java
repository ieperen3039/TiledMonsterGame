package NG.Camera;


import NG.Engine.Game;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 22-12-2017. a camera that doesn't move
 */
public class StaticCamera implements Camera {

    private Vector3f eye, focus;
    private Vector3f up;

    public StaticCamera(Vector3f eye, Vector3f focus, Vector3f up) {
        this.eye = eye;
        this.focus = focus;
        this.up = up;
    }

    @Override
    public void init(Game game) throws Exception {

    }

    @Override
    public Vector3f vectorToFocus() {
        return new Vector3f(focus).sub(eye);
    }

    @Override
    public void updatePosition(float deltaTime) {

    }

    @Override
    public Vector3fc getEye() {
        return eye;
    }

    @Override
    public Vector3fc getFocus() {
        return focus;
    }

    @Override
    public Vector3fc getUpVector() {
        return up;
    }

    @Override
    public void set(Vector3fc focus, Vector3fc eye) {
        this.focus.set(focus);
        this.eye.set(eye);
    }

    @Override
    public void onScroll(float value) {

    }

    @Override
    public void cleanup() {

    }
}
