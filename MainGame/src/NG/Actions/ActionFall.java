package NG.Actions;

import NG.Actions.ActionMarkers.ActionMarker;
import NG.Actions.ActionMarkers.ActionMarkerGenerated;
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
    private final ActionMarkerGenerated marker;

    /**
     * @param startPosition origin
     * @param direction     direction to fall to (does not have to be normalized)
     * @param speed         fall speed (length of the velocity)
     */
    public ActionFall(Vector3fc startPosition, Vector3fc direction, float speed) {
        this(startPosition, new Vector3f(direction).normalize(speed));
    }

    /**
     * @param startPosition   origin
     * @param initialVelocity actual initial velocity (movement per second)
     */
    public ActionFall(Vector3fc startPosition, Vector3fc initialVelocity) {
        this.movement = initialVelocity;
        this.start = startPosition;
        this.marker = new ActionMarkerGenerated(this);
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
        return "Fall (from " + Vectors.toString(start) + " with v = " + movement.length() + ")";
    }

    @Override
    public ActionMarker getMarker() {
        return marker;
    }
}
