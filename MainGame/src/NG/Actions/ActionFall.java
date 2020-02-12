package NG.Actions;

import NG.Animations.BodyAnimation;
import NG.Animations.UniversalAnimation;
import NG.GameMap.GameMap;
import NG.Settings.Settings;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * an action very similar to actionJump, but than involuntarily
 * @author Geert van Ieperen created on 12-2-2020.
 */
public class ActionFall implements EntityAction {
    private static final float TIME_FACTOR = 0.2f;

    protected final Vector3fc start;
    protected final Vector3fc end;
    protected final float duration;

    private final float a = -Settings.GRAVITY_CONSTANT;
    private final float b;
    private final float c;

    public ActionFall(Vector3fc startPosition, Vector3fc direction, GameMap gameMap) {
        Vector2i landingCoord = gameMap.getCoordinate(startPosition);

        landingCoord.add(
                direction.x() == 0 ? 0 : (direction.x() > 0 ? 1 : -1),
                direction.y() == 0 ? 0 : (direction.y() > 0 ? 1 : -1)
        );
        Vector3f endPosition = gameMap.getPosition(landingCoord);

        duration = startPosition.distance(endPosition) * TIME_FACTOR;

        // ax == 0
        float ay = startPosition.z();
        float bx = duration;
        float by = endPosition.z();
        // derivation is somewhere on paper.
//        b = ((ay - by) + (a * ax * ax - a * bx * bx)) / (ax - bx);
//        c = a * ax * bx + (ax * by - ay * bx) / (ax - bx);

        if (bx == 0) {
            b = 0;
            c = endPosition.z();
        } else {
            b = -((ay - by) + (a * bx * bx)) / (bx);
            c = (ay * bx) / (bx);
        }

        start = startPosition;
        end = endPosition;
    }

    @Override
    public Vector3f getPositionAt(float timeSinceStart) {
        if (timeSinceStart <= 0) return new Vector3f(start);

        return new Vector3f(
                Toolbox.interpolate(start.x(), end.x(), timeSinceStart / duration),
                Toolbox.interpolate(start.y(), end.y(), timeSinceStart / duration),
                a * timeSinceStart * timeSinceStart + b * timeSinceStart + c
        );
    }

    @Override
    public float duration() {
        return duration;
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
        return "Fall (to " + Vectors.toString(end) + ")";
    }
}
