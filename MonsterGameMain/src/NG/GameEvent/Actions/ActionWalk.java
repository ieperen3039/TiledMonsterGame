package NG.GameEvent.Actions;

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
public class ActionWalk extends ActionMovement {

    /**
     * @param game      the current game instance
     * @param start     coordinate where the move origins
     * @param end       coordinate where the move ends, adjacent to start
     * @param walkSpeed the time when this action ends in seconds.
     */
    public ActionWalk(Game game, Vector2ic start, Vector2ic end, float walkSpeed) {
        super(game, start, end, walkSpeed / TILE_SIZE);
    }

    @Override
    public Vector3fc getPositionAfter(float timeSinceStart) {
        if (timeSinceStart < 0) return start;
        if (timeSinceStart > duration) return end;

        // TODO more precise movement, taking animation into account (maybe)
        float fraction = timeSinceStart / duration;
        return new Vector3f(start).lerp(end, fraction);
    }

    @Override
    public String toString() {
        return "Walk (" + Vectors.toString(startCoord) + " to " + Vectors.toString(endCoord) + ")";
    }
}
