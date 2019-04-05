package NG.Actions;

import NG.Engine.Game;
import NG.GameMap.GameMap;
import org.joml.Vector2ic;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 1-3-2019.
 */
public abstract class ActionMovement implements EntityAction {
    protected final Vector2ic startCoord;
    protected final Vector2ic endCoord;
    protected final Vector3fc start;
    protected final float startTime;
    protected Vector3fc end;
    protected float endTime;
    private boolean cancelled = false;

    public ActionMovement(Game game, Vector2ic startCoord, Vector2ic endCoord, float startTime, float duration) {
        this.startTime = startTime;
        GameMap map = game.get(GameMap.class);
        this.start = map.getPosition(startCoord);
        this.end = map.getPosition(endCoord);
        this.startCoord = startCoord;
        this.endCoord = endCoord;
        this.endTime = startTime + duration;
    }

    @Override
    public float startTime() {
        return startTime;
    }

    @Override
    public float endTime() {
        return endTime;
    }

    @Override
    public void interrupt(float time) {
        if (time > endTime) return;
        if (time < startTime) {
            cancelled = true;
            return;
        }
        end = getPositionAt(time);
        endTime = time;
    }

    @Override
    public Vector2ic getStartCoordinate() {
        return startCoord;
    }

    @Override
    public Vector2ic getEndCoordinate() {
        return endCoord;
    }

    public float duration() {
        return endTime - startTime;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
