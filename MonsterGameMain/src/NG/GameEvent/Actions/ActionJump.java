package NG.GameEvent.Actions;

import NG.Animations.Animation;
import NG.Animations.BodyAnimation;
import NG.Engine.Game;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Vector2ic;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen created on 21-3-2019.
 */
public class ActionJump extends ActionMovement {

    public static final float GRAVITY_CONSTANT = 9.81f;

    public ActionJump(Game game, Vector2ic startCoord, Vector2ic endCoord, float duration) {
        super(game, startCoord, endCoord, duration);
    }

    @Override
    public Vector3f getPositionAfter(float timeSinceStart) {
        if (timeSinceStart < 0) return new Vector3f(start);
        if (timeSinceStart > duration) return new Vector3f(end);

        float fraction = cap(Toolbox.interpolate(0.1f, 0.9f, timeSinceStart / duration));
        float x = duration * fraction; // NOT timeSinceStart

        return new Vector3f(
                Toolbox.interpolate(start.x(), end.x(), fraction),
                Toolbox.interpolate(start.y(), end.y(), fraction),
                // z = -9.81 x^2 + a x ; a = 9.81 * duration (result of f(duration) = 0)
                -GRAVITY_CONSTANT * x * x + GRAVITY_CONSTANT * duration * x
        );
    }

    /** caps the value between 0 and 1 */
    private float cap(float value) {
        return Math.min(1.0f, Math.max(0.0f, value));
    }

    @Override
    public Animation getAnimation() {
        return BodyAnimation.IDLE;
    }

    @Override
    public String toString() {
        return "Jump (" + Vectors.toString(startCoord) + " to " + Vectors.toString(endCoord) + ")";
    }
}

// calculation z-value (f(distance) = 0) //
// -9.81 * duration * duration + a * duration = 0
// duration = 0 V -9.81 * duration + a = 0
// a = 9.81 * duration
