package NG.Actions;

import NG.Animations.BodyAnimation;
import NG.Animations.UniversalAnimation;
import NG.Core.Game;
import NG.GameMap.GameMap;
import NG.InputHandling.MouseTools.CommandProvider;
import NG.Settings.Settings;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Math;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 21-3-2019.
 */
public class ActionJump implements EntityAction {
    public static final CommandProvider JUMP_COMMAND = CommandProvider.actionCommand("Jump", (g, e, s, t) ->
            new ActionJump(g, s, t, e.getController().props.jumpSpeed));

    protected final Vector3fc start;
    protected final Vector3fc end;
    protected final float duration;

    private final float a = -Settings.GRAVITY_CONSTANT;
    private final float b;
    private final float c;

    public ActionJump(Game game, Vector3fc startPosition, Vector2ic endCoord, float jumpSpeed) {
        this(startPosition, game.get(GameMap.class).getPosition(endCoord), jumpSpeed);
    }

    public ActionJump(Vector3fc startPosition, Vector3fc endPosition, float jumpSpeed) {
        this.start = startPosition;
        this.end = endPosition;
        this.duration = jumpDuration(jumpSpeed, startPosition, endPosition);

        // float ax = 0;
        float ay = startPosition.z();
        float bx = duration;
        float by = endPosition.z();
        // derivation is somewhere on paper.
//        b = ((ay - by) + (a * ax * ax - a * bx * bx)) / (ax - bx);
//        c = a * ax * bx + (ax * by - ay * bx) / (ax - bx);

        if (bx == 0) {
            b = 0;
            c = end.z();
        } else {
            b = -((ay - by) + (a * bx * bx)) / (bx);
            c = ay;
        }

        assert getPositionAt(duration).equals(endPosition, 1 / 128f) :
                String.format("%s | %s | %s | %s", startPosition, endPosition, duration, getPositionAt(duration));

    }

    @Override
    public float duration() {
        return duration;
    }

    @Override
    public boolean hasWorldCollision() {
        return true;
    }

    public static float jumpDuration(float jumpSpeed, Vector3fc startPosition, Vector3fc endPosition) {
        float distance = (float) Math.sqrt(startPosition.distance(endPosition)); // not entirely true, as this does take z into account
        return (distance < (1 / 128f)) ? 0 : (distance / jumpSpeed);
    }

    @Override
    public Vector3f getPositionAt(float timeSinceStart) {
        if (timeSinceStart <= 0) return new Vector3f(start);

        float fraction = timeSinceStart / duration;
        final float x = timeSinceStart;

        return new Vector3f(
                Toolbox.interpolate(start.x(), end.x(), fraction),
                Toolbox.interpolate(start.y(), end.y(), fraction),
                a * x * x + b * x + c
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


