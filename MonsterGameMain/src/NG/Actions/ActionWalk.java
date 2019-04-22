package NG.Actions;

import NG.Animations.UniversalAnimation;
import NG.Engine.Game;
import NG.GameMap.GameMap;
import NG.Tools.Vectors;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static NG.Animations.BodyAnimation.WALK_CYCLE;

/**
 * A linear movement from point A to point B, where A and B are adjacent
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class ActionWalk implements EntityAction {
    protected final Vector3fc start;
    protected final Vector3fc end;
    protected final float duration;
    private final UniversalAnimation animation;

    /**
     * @param game       the current game instance
     * @param startCoord start coordinate
     * @param endCoord   target coordinate where to walk to
     * @param walkSpeed  the time when this action ends in seconds.
     */
    public ActionWalk(Game game, Vector2ic startCoord, Vector2ic endCoord, float walkSpeed) {
        this(game, game.get(GameMap.class).getPosition(startCoord), endCoord, walkSpeed);
    }

    /**
     * @param game          the current game instance
     * @param startPosition the exact position where to start walking
     * @param endCoord      target coordinate where to walk to
     * @param walkSpeed     the time when this action ends in seconds.
     */
    public ActionWalk(Game game, Vector3fc startPosition, Vector2ic endCoord, float walkSpeed) {
        GameMap map = game.get(GameMap.class);
        start = new Vector3f(startPosition);
        end = map.getPosition(endCoord);
        duration = walkSpeed / (start.distance(end));
        animation = WALK_CYCLE;
    }

    @Override
    public Vector3f getPositionAt(float timeSinceStart) {
        if (timeSinceStart <= 0) return new Vector3f(start);
        if (timeSinceStart >= duration) return new Vector3f(end);

        // TODO more precise movement, taking animation into account (maybe)
        float fraction = timeSinceStart / duration;
        return new Vector3f(start).lerp(end, fraction);
    }

    @Override
    public UniversalAnimation getAnimation() {
        return animation;
    }

    @Override
    public String toString() {
        return "Walk (to " + Vectors.toString(end) + ")";
    }

    @Override
    public float duration() {
        return duration;
    }
}
