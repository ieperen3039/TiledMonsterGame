package NG.Entities.Actions;

import NG.Engine.Game;
import org.joml.Vector2ic;
import org.joml.Vector3f;

/**
 * Stand still and do nothing at all. Can be interrupted any time.
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class ActionIdle implements EntityAction {
    private final float duration;
    private final Vector3f position;

    /**
     * idle for a given duration after executing the given task
     * @param preceding the action that happens just before this idling
     * @param duration  the duration of staying idle.
     */
    public ActionIdle(EntityAction preceding, float duration) {
        this(preceding.getEndPosition(), duration);
    }

    /**
     * idle for an undetermined duration
     * @param position the position to idle
     */
    public ActionIdle(Game game, Vector2ic position) {
        this(game, position, 0);
    }

    /**
     * idle for a given duration.
     * @param position the position to idle
     * @param duration how long to stay idle
     */
    public ActionIdle(Game game, Vector2ic position, float duration) {
        this(game.map().getPosition(position), duration);
    }

    /**
     * idle for an given duration starting at a given moment.
     * @param position  the exact position of idling
     * @param duration  how long to stay idle
     */
    private ActionIdle(Vector3f position, float duration) {
        if (duration < 0) throw new IllegalArgumentException("Idle duration must be greater than zero");
        this.position = position;
        this.duration = duration;
    }

    @Override
    public Vector3f getPositionAfter(float passedTime) {
        return position;
    }

    @Override
    public float duration() {
        return duration;
    }

    @Override
    public float interrupt(float passedTime) {
        return passedTime; // always succeeds
    }
}
