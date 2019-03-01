package NG.GameEvent.Actions;

import NG.Animations.Animation;
import NG.Animations.BodyModel;
import NG.Engine.Game;
import org.joml.Vector2ic;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 1-3-2019.
 */
public abstract class ActionMovement implements EntityAction {
    protected final Vector3fc start;
    protected final Vector3fc end;
    protected final float duration;
    protected final Vector2ic startCoord;
    protected final Vector2ic endCoord;

    public ActionMovement(Game game, Vector2ic start, Vector2ic end, float duration) {
        this.start = game.map().getPosition(start);
        this.end = game.map().getPosition(end);
        this.startCoord = start;
        this.endCoord = end;
        this.duration = duration;
    }

    @Override
    public Vector2ic getStartCoordinate() {
        return startCoord;
    }

    @Override
    public Vector2ic getEndCoordinate() {
        return endCoord;
    }

    @Override
    public float duration() {
        return duration;
    }

    @Override
    public Animation getAnimation(BodyModel model) {
        return null; // TODO map animations
    }
}
