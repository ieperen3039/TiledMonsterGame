package NG.Camera;

import NG.ActionHandling.KeyPressListener;
import NG.ActionHandling.KeyReleaseListener;
import NG.ActionHandling.MousePositionListener;
import NG.DataStructures.Tracked.ExponentialSmoothFloat;
import NG.Engine.Game;
import NG.Settings.KeyBinding;
import NG.Settings.Settings;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static NG.Camera.TycoonFixedCamera.MoveDirection.*;

/**
 * A camera implementation that can be moved by holding the mouse to the corners of the screen
 * @author Geert van Ieperen. Created on 18-11-2018.
 */
public class TycoonFixedCamera implements Camera, MousePositionListener, KeyPressListener, KeyReleaseListener {
    private static final int SCREEN_MOVE_MINIMUM_PIXELS = 50;
    private static final float SCROLL_SPEED = 0.02f;
    private static final float SCROLL_SPEED_LIMIT = 0.05f;
    private static final float ROTATION_MODIFIER = 1f;
    private static final float HEIGHT_GAIN = 0.95f;
    private final Vector3f focus = new Vector3f();
    private final ExponentialSmoothFloat height = new ExponentialSmoothFloat(0f, 1 - HEIGHT_GAIN);
    private final Vector3f eyeOffset;

    private Game game;
    private int mouseXPos;
    private int mouseYPos;
    private MoveDirection cameraRotation = NOT;

    public TycoonFixedCamera(Vector3fc initialFocus, int eyeDistance) {
        eyeOffset = new Vector3f(-eyeDistance, -eyeDistance, eyeDistance);
        focus.set(initialFocus);
    }

    @Override
    public void init(Game game) {
        this.game = game;
        game.inputHandling().addMousePositionListener(this);
        game.inputHandling().addKeyPressListener(this);
        game.inputHandling().addKeyReleaseListener(this);
    }

    @Override
    public Vector3fc vectorToFocus() {
        return new Vector3f(eyeOffset).negate();
    }

    @Override
    public void updatePosition(float deltaTime) {
        Vector3f eyeDir = new Vector3f(eyeOffset);
        // x movement
        if (mouseXPos < SCREEN_MOVE_MINIMUM_PIXELS) {
            float value = positionToMovement(mouseXPos) * deltaTime;
            eyeDir.normalize(value).cross(getUpVector());
            focus.add(eyeDir.x, eyeDir.y, 0);

        } else {
            int xInv = game.window().getWidth() - mouseXPos;
            if (xInv < SCREEN_MOVE_MINIMUM_PIXELS) {
                float value = positionToMovement(xInv) * deltaTime;
                eyeDir.normalize(value).cross(getUpVector());
                focus.sub(eyeDir.x, eyeDir.y, 0);
            }
        }

        eyeDir.set(eyeOffset);
        // y movement
        if (mouseYPos < SCREEN_MOVE_MINIMUM_PIXELS) {
            float value = positionToMovement(mouseYPos) * deltaTime;
            eyeDir.normalize(value);
            focus.sub(eyeDir.x, eyeDir.y, 0);

        } else {
            int yInv = game.window().getHeight() - mouseYPos;
            if (yInv < SCREEN_MOVE_MINIMUM_PIXELS) {
                float value = positionToMovement(yInv) * deltaTime;
                eyeDir.normalize(value);
                focus.add(eyeDir.x, eyeDir.y, 0);
            }
        }

        float tgtHeight = game.map().getHeightAt(focus.x, focus.y);
        height.updateFluent(tgtHeight, deltaTime);
        focus.z = height.current();

        if (cameraRotation != NOT) {
            float angle = deltaTime * ROTATION_MODIFIER;
            if (cameraRotation == RIGHT) angle = -angle;
            eyeOffset.rotateZ(angle);
        }
    }

    @Override
    public Vector3fc getEye() {
        return new Vector3f(focus).add(eyeOffset);
    }

    @Override
    public Vector3fc getFocus() {
        return focus;
    }

    @Override
    public Vector3fc getUpVector() {
        return Vectors.zVector();
    }

    @Override
    public void set(Vector3fc focus, Vector3fc eye) {
        this.focus.set(focus);
        this.height.update(focus.z());
        Vector3f offset = new Vector3f(eye).sub(focus);
        this.eyeOffset.set(offset);
    }

    @Override
    public void onScroll(float value) {
        Settings s = game.settings();
        float zoomSpeed = s.CAMERA_ZOOM_SPEED;
        int maxZoom = s.MAX_CAMERA_DIST;

        float v = Math.max(Math.min(zoomSpeed * -value, SCROLL_SPEED_LIMIT), -SCROLL_SPEED_LIMIT);
        eyeOffset.mul(v + 1f);
        float minZoom = s.MIN_CAMERA_DIST;

        if (eyeOffset.lengthSquared() > maxZoom * maxZoom) {
            eyeOffset.normalize(maxZoom);
        } else if (eyeOffset.lengthSquared() < minZoom * minZoom) {
            eyeOffset.normalize(minZoom);
        }
    }

    @Override
    public void cleanup() {
        game.inputHandling().removeListener(this);
    }

    @Override
    public void mouseMoved(int xPos, int yPos) {
        mouseXPos = xPos;
        mouseYPos = yPos;
    }

    /**
     * gives how much the camera should move given how many pixels the mouse is from the edge of the screen
     * @param pixels the number of pixels between the mouse and the edge of the screen, at least 0
     * @return how fast the camera should move in the direction
     */
    protected float positionToMovement(int pixels) {
        if (pixels >= SCREEN_MOVE_MINIMUM_PIXELS) return 0;
        return (SCREEN_MOVE_MINIMUM_PIXELS - pixels) * eyeOffset.length() * SCROLL_SPEED;
    }

    @Override
    public void keyPressed(int keyCode) {
        switch (KeyBinding.get(keyCode)) {
            case CAMERA_LEFT:
                cameraRotation = LEFT;
                break;
            case CAMERA_RIGHT:
                cameraRotation = RIGHT;
                break;
        }
    }

    @Override
    public void keyReleased(int keyCode) {
        switch (KeyBinding.get(keyCode)) {
            case CAMERA_LEFT:
                cameraRotation = NOT;
                break;
            case CAMERA_RIGHT:
                cameraRotation = NOT;
                break;
        }
    }

    enum MoveDirection {
        LEFT, NOT, RIGHT
    }
}

