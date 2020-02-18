package NG.Actions;

import NG.Animations.BodyAnimation;
import NG.Animations.UniversalAnimation;
import NG.Settings.Settings;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * an action very similar to actionJump, but than involuntarily
 * @author Geert van Ieperen created on 12-2-2020.
 */
public class ActionFall implements EntityAction {
    protected final float speed;
    protected final Vector3fc start;
    protected final Vector3fc end;

    private final float startHeight;

    public ActionFall(Vector3fc startPosition, Vector3fc direction, float speed) {
        this.speed = speed;
        Vector3f endPosition = new Vector3f(direction).normalize().add(startPosition);

        startHeight = startPosition.z();

        start = startPosition;
        end = endPosition;
    }

    @Override
    public Vector3f getPositionAt(float timeSinceStart) {
        if (timeSinceStart <= 0) return new Vector3f(start);

        return new Vector3f(
                Toolbox.interpolate(start.x(), end.x(), timeSinceStart * speed),
                Toolbox.interpolate(start.y(), end.y(), timeSinceStart * speed),
                startHeight - Settings.GRAVITY_CONSTANT * timeSinceStart * timeSinceStart
        );
    }

    @Override
    public float duration() {
        return 0f;
    }

    @Override
    public boolean hasWorldCollision() {
        return true;
    }

    @Override
    public UniversalAnimation getAnimation() {
        return BodyAnimation.BASE_POSE;
    }

    @Override
    public String toString() {
        return "Fall (from " + Vectors.toString(start) + ")";
    }
}
