package NG.Actions;

import NG.Animations.UniversalAnimation;
import NG.Engine.Game;
import NG.GameMap.GameMap;
import NG.Tools.Vectors;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static NG.Animations.BodyAnimation.IDLE;

/**
 * A linear movement from point A to point B
 * @author Geert van Ieperen created on 2-4-2019.
 */
public class ActionFly implements EntityAction {
    protected final Vector3fc start;
    protected final Vector3fc end;
    protected final float duration;

    public ActionFly(
            Game game, Vector3f startPosition, Vector2ic endCoord, float speed, float height
    ) {
        this(startPosition, game.get(GameMap.class).getPosition(endCoord), speed, height);
    }

    public ActionFly(Vector3f startPosition, Vector3f endPosition, float speed, float height) {
        this.start = startPosition.add(0, 0, height);
        this.end = endPosition.add(0, 0, height);
        this.duration = speed / start.distance(end);
    }

    @Override
    public Vector3f getPositionAt(float timeSinceStart) {
        if (timeSinceStart < 0) return new Vector3f(start);
        float fraction = timeSinceStart / duration;
        return new Vector3f(start).lerp(end, fraction);
    }

    @Override
    public UniversalAnimation getAnimation() {
        return IDLE;
    }

    @Override
    public String toString() {
        return "Fly (to " + Vectors.toString(end) + ")";
    }

    @Override
    public float duration() {
        return duration;
    }
}
