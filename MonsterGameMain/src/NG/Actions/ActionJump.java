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

    public ActionJump(Game game, Vector3fc startPosition, Vector2ic endCoord, float jumpSpeed) {
        this(startPosition, game.get(GameMap.class).getPosition(endCoord), jumpSpeed);
    }

    public ActionJump(Vector3fc startPosition, Vector3f endPosition, float jumpSpeed) {
        this.start = startPosition;
        this.end = endPosition;
        this.duration = jumpDuration(jumpSpeed, startPosition.distance(endPosition));
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
        if (timeSinceStart < 0) return new Vector3f(start);
        if (timeSinceStart > duration) return new Vector3f(end);

        float fraction = Math.max(Toolbox.interpolate(-.1f, 1f, timeSinceStart / duration), 0);
        float x = duration * fraction; // NOT timeSinceStart

        return new Vector3f(
                Toolbox.interpolate(start.x(), end.x(), fraction),
                Toolbox.interpolate(start.y(), end.y(), fraction),
                // z = -Fg x^2 + a x ; a = Fg * duration ; (result of z(duration) = 0)
                -Settings.GRAVITY_CONSTANT * x * x + Settings.GRAVITY_CONSTANT * duration * x
        );
    }

    /** caps the value between 0 and 1 */
    private float cap(float value) {
        return Math.min(1.0f, Math.max(0.0f, value));
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

// calculation a-value (z(duration) = 0)
// -Fz * duration * duration + a * duration = 0
// duration = 0 V -Fz * duration + a = 0
// a = Fz * duration
