package NG.Actions;

import NG.Animations.BodyAnimation;
import NG.Animations.UniversalAnimation;
import NG.Settings.Settings;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * an action very similar to actionJump, but than involuntarily
 * @author Geert van Ieperen created on 12-2-2020.
 */
public class ActionFall implements EntityAction {
    protected final Vector3fc start;
    protected final Vector3fc movement;

    /**
     * @param startPosition origin
     * @param direction     normalized direction to fall to
     * @param speed         horizontal fall speed
     */
    public ActionFall(Vector3fc startPosition, Vector3fc direction, float speed) {
        this.movement = new Vector3f(direction).normalize(speed);
        this.start = startPosition;
    }

    @Override
    public Vector3f getPositionAt(float timeSinceStart) {
        if (timeSinceStart <= 0) return new Vector3f(start);
        final float t = timeSinceStart;

        return new Vector3f(
                start.x() + movement.x() * t,
                start.y() + movement.y() * t,
                start.z() + movement.z() * t - Settings.GRAVITY_CONSTANT * t * t
        );
    }

    @Override
    public float duration() {
        return Float.POSITIVE_INFINITY;
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
