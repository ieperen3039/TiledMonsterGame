package NG.Actions;

import NG.Animations.BodyAnimation;
import NG.Animations.UniversalAnimation;
import NG.Engine.Game;
import NG.GameMap.GameMap;
import NG.Settings.Settings;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 21-3-2019.
 */
public class ActionJump implements EntityAction {

    protected final Vector3fc start;
    protected final Vector3fc end;
    protected final float duration;
    protected final float distance;

    private final float a = Settings.GRAVITY_CONSTANT;
    private final float b;

    public ActionJump(Game game, Vector3fc startPosition, Vector2ic endCoord, float jumpSpeed) {
        this(startPosition, game.get(GameMap.class).getPosition(endCoord), jumpSpeed);
    }

    public ActionJump(Vector3fc startPosition, Vector3f endPosition, float jumpSpeed) {
        this.start = startPosition;
        this.end = endPosition;
        this.distance = startPosition.distance(endPosition); // not entirely true, as this does not take z into account
        this.duration = jumpDuration(jumpSpeed, distance);

        // relative
        float bx = distance;
        float by = endPosition.z - startPosition.z();
        // derivation is somewhere on paper. (ax, ay) = (u, v) && (bx, by) = (m, n)
//        b = ((ay - by) + (a * ax * ax - a * bx * bx)) / (ax - bx);
//        c = a * ax * bx + (ax * by - ay * bx) / (ax - bx);
        if (bx == 0) {
            b = 0;
        } else {
            b = (a * bx) + (by / bx);
        }
    }

    @Override
    public float duration() {
        return duration;
    }

    @Override
    public boolean hasWorldCollision() {
        return true;
    }

    public static float jumpDuration(float jumpSpeed, float distance) {
        return jumpSpeed / distance;
    }

    @Override
    public Vector3f getPositionAt(float timeSinceStart) {
        if (distance == 0) return new Vector3f(end);
        if (timeSinceStart < 0) return new Vector3f(start);
        if (timeSinceStart > duration) return new Vector3f(end);

        float fraction = Math.max(Toolbox.interpolate(-.1f, 1f, timeSinceStart / duration), 0);
        float x = distance * fraction; // NOT timeSinceStart

        return new Vector3f(
                Toolbox.interpolate(start.x(), end.x(), fraction),
                Toolbox.interpolate(start.y(), end.y(), fraction),
                a * x * x + b * x + start.z()
        );
    }


    @Override
    public UniversalAnimation getAnimation() {
        return BodyAnimation.BASE_POSE;
    }

    @Override
    public String toString() {
        return "Jump (to " + Vectors.toString(end) + ")";
    }
}


