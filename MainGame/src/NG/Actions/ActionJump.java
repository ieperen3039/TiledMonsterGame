package NG.Actions;

import NG.Actions.ActionMarkers.ActionMarker;
import NG.Actions.ActionMarkers.ActionMarkerGenerated;
import NG.Animations.BodyAnimation;
import NG.Animations.UniversalAnimation;
import NG.Core.Game;
import NG.GameMap.GameMap;
import NG.InputHandling.MouseTools.CommandProvider;
import NG.Resources.GeneratorResource;
import NG.Resources.Resource;
import NG.Settings.Settings;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Math;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static java.lang.Math.*;

/**
 * @author Geert van Ieperen created on 21-3-2019.
 */
public class ActionJump implements EntityAction {
    public static final CommandProvider JUMP_COMMAND = CommandProvider.actionCommand(
            "Jump", (g, e, s, t, gameTime) -> new ActionJump(g, s, t, e.getController().props.jumpSpeed)
    );

    protected final Vector3fc start;
    protected final Vector3fc end;
    protected final float duration; // planned duration

    private final float a;
    private final float b;
    private final float c;
    private final Resource<ActionMarker> marker;

    public ActionJump(Game game, Vector3fc startPosition, Vector2ic endCoord, float jumpSpeed) {
        this(startPosition, game.get(GameMap.class).getPosition(endCoord), jumpSpeed);
    }

    public ActionJump(Vector3fc startPosition, Vector3fc endPosition, float jumpSpeed) {
        this.start = startPosition;
        this.end = endPosition;

        float xDiff = end.x() - start.x();
        float yDiff = end.y() - start.y();
        float hz = Math.sqrt(Math.fma(xDiff, xDiff, yDiff * yDiff));
        float vt = end.z() - start.z();
        float g = Settings.GRAVITY_CONSTANT;
        float vSq = jumpSpeed * jumpSpeed;

        // see https://gamedev.stackexchange.com/questions/53552/how-can-i-find-a-projectiles-launch-angle
        float determinant = vSq * vSq - g * (g * hz * hz + 2 * vt * vSq);

        double theta;
        if (determinant < 0) {
            theta = PI / 4;

        } else {
            theta = Math.min(
                    atan((vSq + Math.sqrt(determinant)) / (g * hz)),
                    atan((vSq - Math.sqrt(determinant)) / (g * hz))
            );
        }
        a = -0.5f * g;
        b = (float) (jumpSpeed * sin(theta));
        c = start.z();
        duration = (float) (hz / (Math.cos(theta) * jumpSpeed));

        marker = new GeneratorResource<>(() -> new ActionMarkerGenerated(this, duration), null);
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
    public ActionMarker getMarker() {
        return marker.get();
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
    public Vector3f getDerivative(float timeSinceStart) {
        return new Vector3f(
                (end.x() - start.x()) / duration,
                (end.y() - start.y()) / duration,
                a * timeSinceStart + b
        );
    }

    @Override
    public UniversalAnimation getAnimation() {
        return BodyAnimation.BASE_POSE;
    }

    @Override
    public String toString() {
        return "Jump to " + Vectors.toString(end);
    }
}


