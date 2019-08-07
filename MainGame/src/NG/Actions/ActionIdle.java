package NG.Actions;

import NG.Animations.BodyAnimation;
import NG.Animations.UniversalAnimation;
import NG.Core.Game;
import NG.GameMap.GameMap;
import NG.Tools.Vectors;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Stand still and do nothing at all. Can be interrupted any time.
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class ActionIdle implements EntityAction {
    private final float duration;
    private final Vector3fc position;

    /**
     * idle for an undetermined duration
     * @param position the position to idle
     */
    public ActionIdle(Game game, Vector2ic position) {
        this(game, position, Float.POSITIVE_INFINITY);
    }

    /**
     * idle for a given duration.
     * @param coordinate the position to idle
     * @param duration how long to stay idle
     */
    public ActionIdle(Game game, Vector2ic coordinate, float duration) {
        this(game.get(GameMap.class).getPosition(coordinate), duration);
    }

    /**
     * idle for an undetermined duration on the given position
     * @param position exact position to idle
     */
    public ActionIdle(Vector3fc position) {
        this(position, Float.POSITIVE_INFINITY);
    }

    /**
     * idle for a given duration.
     * @param position the exact position where to execute the idling
     * @param duration how long to stay idle
     */
    public ActionIdle(Vector3fc position, float duration) {
        if (duration < 0) throw new IllegalArgumentException("Idle duration must be greater than zero");
        this.position = position;
        this.duration = duration;
    }

    @Override
    public Vector3f getPositionAt(float timeSinceStart) {
        return new Vector3f(position);
    }

    @Override
    public float duration() {
        return duration;
    }

    @Override
    public String toString() {
        return "Idle (at " + Vectors.toString(position) + ")";
    }

    @Override
    public float getMagnitude(Vector3fc otherPosition) {
        return 0;
    }

    @Override
    public UniversalAnimation getAnimation() {
        return BodyAnimation.BASE_POSE; // TODO make a real idle animation
    }

    @Override
    public boolean hasWorldCollision() {
        return false;
    }
}
