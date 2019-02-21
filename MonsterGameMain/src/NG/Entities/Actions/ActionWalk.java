package NG.Entities.Actions;

import NG.Engine.Game;
import NG.Tools.Vectors;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static NG.Settings.Settings.TILE_SIZE;

/**
 * A linear movement from point A to point B
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class ActionWalk implements EntityAction {
    private final Vector3fc start;
    private final Vector3fc end;
    private final float duration;
    private final Vector2ic startCoord;
    private final Vector2ic endCoord;

    /**
     * @param game      the current game instance
     * @param start     coordinate where the move origins
     * @param end       coordinate where the move ends, adjacent to start
     * @param walkSpeed the time when this action ends in seconds.
     */
    public ActionWalk(Game game, Vector2ic start, Vector2ic end, float walkSpeed) {
        startCoord = start;
        endCoord = end;
        this.start = game.map().getPosition(start);
        this.end = game.map().getPosition(end);
        duration = walkSpeed / TILE_SIZE;
    }

    @Override
    public Vector3f getPositionAfter(float passedTime) {
        if (passedTime < 0) return getStartPosition();
        if (passedTime > duration) return getEndPosition();

        float fraction = passedTime / duration;
        return new Vector3f(start).lerp(end, fraction);
    }

    @Override
    public Vector3f getStartPosition() {
        return new Vector3f(start);
    }

    @Override
    public Vector3f getEndPosition() {
        return new Vector3f(end);
    }

    @Override
    public float duration() {
        return duration;
    }

    @Override
    public String toString() {
        return "Walk (" + Vectors.toString(startCoord) + " to " + Vectors.toString(endCoord) + ")";
    }
}
