package NG.Actions;

import NG.Actions.ActionMarkers.ActionMarker;
import NG.Actions.ActionMarkers.ActionMarkerArrow;
import NG.Animations.UniversalAnimation;
import NG.Core.Game;
import NG.GameMap.GameMap;
import NG.Tools.Vectors;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static NG.Animations.BodyAnimation.BASE_POSE;

/**
 * A linear movement from point A to point B
 * @author Geert van Ieperen created on 2-4-2019.
 */
public class ActionLinearMove implements EntityAction {
    protected final Vector3fc start;
    protected final Vector3fc end;
    protected final float duration;
    private final ActionMarkerArrow marker;

    public ActionLinearMove(
            Game game, Vector3fc startPosition, Vector2ic endCoord, float speed
    ) {
        this(startPosition, game.get(GameMap.class).getPosition(endCoord), speed);
    }

    public ActionLinearMove(Vector3fc startPosition, Vector3fc endPosition, float speed) {
        this.start = startPosition;
        this.end = endPosition;
        this.duration = speed / start.distance(end);
        marker = new ActionMarkerArrow(start, end);
    }

    @Override
    public Vector3f getPositionAt(float timeSinceStart) {
        if (timeSinceStart <= 0) return new Vector3f(start);
        float fraction = timeSinceStart / duration;
        return new Vector3f(start).lerp(end, fraction);
    }

    @Override
    public UniversalAnimation getAnimation() {
        return BASE_POSE;
    }

    @Override
    public String toString() {
        return "Fly (to " + Vectors.toString(end) + ")";
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
    public ActionMarker getMarker() {
        return marker;
    }
}
