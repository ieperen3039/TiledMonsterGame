package NG.Entities.Actions;

import NG.Engine.Game;
import org.joml.Vector2ic;
import org.joml.Vector3f;

/**
 * Stand still and do nothing at all.
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class ActionIdle implements EntityAction {
    private final float duration;
    private final Vector3f position;

    private Float startTime;

    /**
     * idle for an undetermined duration
     * @param position the position to idle
     */
    public ActionIdle(Game game, Vector2ic position) {
        this(game, position, TIME_UNDEFINED, 0);
    }

    /**
     * idle for a given duration.
     * @param position the position to idle
     * @param duration how long to stay idle
     */
    public ActionIdle(Game game, Vector2ic position, float duration) {
        this(game, position, TIME_UNDEFINED, duration);
    }

    /**
     * idle for an given duration starting at a given moment.
     * @param position  the position to idle
     * @param startTime the moment at which the idling starts
     * @param duration  how long to stay idle
     */
    public ActionIdle(Game game, Vector2ic position, Float startTime, float duration) {
        this(startTime, duration, game.map().getPosition(position));
    }

    /**
     * idle for an given duration starting at a given moment.
     * @param startTime the moment at which the idling starts
     * @param duration  how long to stay idle
     * @param position  the exact position of idling
     */
    public ActionIdle(Float startTime, float duration, Vector3f position) {
        this.position = position;
        this.startTime = startTime;
        this.duration = duration;
    }

    @Override
    public Vector3f getPositionAt(float currentTime) {
        return position;
    }

    @Override
    public Float getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(float time) {
        startTime = time;
    }

    @Override
    public float duration() {
        return duration;
    }

    @Override
    public void interrupt(float moment) {

    }
}
