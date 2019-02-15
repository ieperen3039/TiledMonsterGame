package NG.Entities.Actions;

import NG.Engine.Game;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Objects;

import static NG.Settings.Settings.TILE_SIZE;

/**
 * A linear movement from point A to point B
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class ActionWalk implements EntityAction {
    private final Vector3fc start;
    private final Vector3fc end;
    private final float duration;
    private Float startTime;

    /**
     * @param game      the current game instance
     * @param start     coordinate where the move origins
     * @param end       coordinate where the move ends, adjacent to start
     * @param walkSpeed the time when this action ends in seconds.
     */
    public ActionWalk(Game game, Vector2ic start, Vector2ic end, float walkSpeed) {
        this.start = game.map().getPosition(start);
        this.end = game.map().getPosition(end);
        duration = walkSpeed / TILE_SIZE;
        this.startTime = TIME_UNDEFINED;
    }

    @Override
    public Vector3f getPositionAt(float currentTime) {
        assert !Objects.equals(startTime, TIME_UNDEFINED);
        return start.lerp(end, currentTime - startTime, new Vector3f());
    }

    @Override
    public float duration() {
        return duration;
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
    public void interrupt(float moment) {
        // no support
    }
}
