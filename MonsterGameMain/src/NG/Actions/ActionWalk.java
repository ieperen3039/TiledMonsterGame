package NG.Actions;

import NG.Animations.UniversalAnimation;
import NG.Engine.Game;
import NG.Tools.Vectors;
import org.joml.Vector2ic;
import org.joml.Vector3f;

import static NG.Animations.BodyAnimation.WALK_CYCLE;
import static NG.Settings.Settings.TILE_SIZE;

/**
 * A linear movement from point A to point B
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class ActionWalk extends ActionMovement {
    private final UniversalAnimation animation;

    /**
     * @param game      the current game instance
     * @param start     coordinate where the move origins
     * @param end       coordinate where the move ends, adjacent to start
     * @param walkSpeed the speed of the movement in m/s
     * @param startTime the time the action starts
     */
    public ActionWalk(Game game, Vector2ic start, Vector2ic end, float walkSpeed, float startTime) {
        super(game, start, end, startTime, walkSpeed / TILE_SIZE);

        animation = WALK_CYCLE;
    }

    @Override
    public Vector3f getPositionAt(float currentTime) {
        float timeSinceStart = currentTime - startTime;
        if (timeSinceStart < 0) return new Vector3f(start);
        if (currentTime > endTime) return new Vector3f(end);

        float fraction = timeSinceStart / duration();
        return new Vector3f(start).lerp(end, fraction);
    }

    @Override
    public UniversalAnimation getAnimation() {
        return animation;
    }

    @Override
    public String toString() {
        return "Walk (" + Vectors.toString(startCoord) + " to " + Vectors.toString(endCoord) + ")";
    }
}
