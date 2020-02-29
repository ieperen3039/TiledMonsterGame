package NG.Actions;

import NG.Actions.ActionMarkers.ActionMarker;
import NG.Actions.ActionMarkers.ActionMarkerArrow;
import NG.Animations.BodyAnimation;
import NG.Animations.UniversalAnimation;
import NG.Core.Game;
import NG.GameMap.GameMap;
import NG.Tools.Vectors;
import org.joml.*;

/**
 * A linear movement from point A to point B, where A and B are adjacent
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class ActionWalk implements EntityAction {
    protected final Vector3fc start;
    protected final Vector3fc end;
    protected final Vector2fc startToEnd;
    protected final float duration;
    private final UniversalAnimation animation;
    private final ActionMarkerArrow marker;
    private transient Game game;

    /**
     * @param game          the current game instance
     * @param startPosition the exact position where to start walking
     * @param endCoord      target coordinate where to walk to
     */
    public ActionWalk(Game game, Vector3fc startPosition, Vector2ic endCoord) {
        this(game, startPosition, endCoord, 3f);
    }

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
        this.game = game;
        GameMap map = game.get(GameMap.class);

        assert map.isOnFloor(startPosition) : String.format(
                "Start position is not on the ground: %s should have z = %s",
                Vectors.toString(startPosition), map.getHeightAt(startPosition.x(), startPosition.y()));

        start = new Vector3f(startPosition);
        end = map.getPosition(endCoord);
        startToEnd = new Vector2f(end.x() - startPosition.x(), end.y() - startPosition.y());
        duration = (start.distance(end)) / walkSpeed;
        animation = BodyAnimation.WALK_CYCLE;
        marker = new ActionMarkerArrow(start, end);
    }

    @Override
    public Vector3f getPositionAt(float timeSinceStart) {
        if (timeSinceStart <= 0) return new Vector3f(start);
        if (timeSinceStart >= duration) return new Vector3f(end);

        // TODO more precise movement, wrt jumping and falling, maybe incorporate animation
        float fraction = timeSinceStart / duration;
        GameMap map = game.get(GameMap.class);

        float x = start.x() + fraction * startToEnd.x();
        float y = start.y() + fraction * startToEnd.y();
        float height = map.getHeightAt(x, y);
        return new Vector3f(x, y, height);
    }

    @Override
    public UniversalAnimation getAnimation() {
        return animation;
    }

    @Override
    public String toString() {
        return "Walk from " + Vectors.toString(start) + " to " + Vectors.toString(end);
    }

    @Override
    public float duration() {
        return duration;
    }

    @Override
    public boolean hasWorldCollision() {
        return false;
    }

    public void restore(Game game) {
        this.game = game;
    }

    @Override
    public ActionMarker getMarker() {
        return marker;
    }
}
