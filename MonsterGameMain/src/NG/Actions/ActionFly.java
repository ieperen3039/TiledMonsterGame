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
    protected final Vector2ic startCoord;
    protected final Vector2ic endCoord;

    public ActionFly(Game game, Vector2ic startCoord, Vector2ic endCoord, float speed, float height) {
        GameMap map = game.get(GameMap.class);
        this.start = map.getPosition(startCoord).add(0, 0, height);
        this.end = map.getPosition(endCoord).add(0, 0, height);
        this.startCoord = startCoord;
        this.endCoord = endCoord;
        this.duration = speed / start.distance(end);
    }

    @Override
    public Vector3f getPositionAt(float timeSinceStart) {
        if (timeSinceStart < 0) return new Vector3f(start);
        if (timeSinceStart > duration) return new Vector3f(end);

        // TODO more precise movement, taking animation into account (maybe)
        float fraction = timeSinceStart / duration;
        return new Vector3f(start).lerp(end, fraction);
    }

    @Override
    public UniversalAnimation getAnimation() {
        return IDLE;
    }

    @Override
    public String toString() {
        return "Fly (" + Vectors.toString(startCoord) + " to " + Vectors.toString(endCoord) + ")";
    }

    @Override
    public Vector2ic getStartCoordinate() {
        return startCoord;
    }

    @Override
    public Vector2ic getEndCoordinate() {
        return endCoord;
    }

    @Override
    public float duration() {
        return duration;
    }
}
