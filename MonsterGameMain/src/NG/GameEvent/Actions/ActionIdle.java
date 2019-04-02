package NG.GameEvent.Actions;

import NG.Animations.AnimationBone;
import NG.Animations.BodyAnimation;
import NG.Animations.PartialAnimation;
import NG.Animations.UniversalAnimation;
import NG.Engine.Game;
import org.joml.*;

import java.io.DataOutput;
import java.util.Collections;
import java.util.Set;

/**
 * Stand still and do nothing at all. Can be interrupted any time.
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class ActionIdle implements EntityAction {
    private final float duration;
    private final Vector3fc position;
    private final Vector2ic coordinate;

    /**
     * idle for a given duration after executing the given task
     * @param game
     * @param preceding the action that happens just before this idling
     * @param duration  the duration of staying idle.
     */
    public ActionIdle(Game game, EntityAction preceding, float duration) {
        this(game, preceding.getEndCoordinate(), duration);
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
     * @param coordinate the position to idle
     * @param duration how long to stay idle
     */
    public ActionIdle(Game game, Vector2ic coordinate, float duration) {
        this(coordinate, game.map().getPosition(coordinate), duration);
    }

    /**
     * idle for a given duration.
     * @param coordinate the position to idle
     * @param position the position mapped to the 3d space
     * @param duration how long to stay idle
     */
    public ActionIdle(Vector2ic coordinate, Vector3fc position, float duration) {
        if (duration < 0) throw new IllegalArgumentException("Idle duration must be greater than zero");
        this.position = position;
        this.coordinate = coordinate;
        this.duration = duration;
    }

    @Override
    public Vector3f getPositionAfter(float timeSinceStart) {
        return new Vector3f(position);
    }

    @Override
    public Vector2ic getStartCoordinate() {
        return coordinate;
    }

    @Override
    public Vector2ic getEndCoordinate() {
        return coordinate;
    }

    @Override
    public float duration() {
        return duration;
    }

    @Override
    public String toString() {
        return "Idle (" + duration + ")";
    }

    @Override
    public float getMagnitude(Vector3fc position) {
        return 0;
    }

    @Override
    public UniversalAnimation getAnimation() {
        return BodyAnimation.IDLE;
    }

    public static PartialAnimation idleAnimation(final float duration) {
        return new PartialAnimation() { // TODO make this into a real animation
            @Override
            public Matrix4fc transformationOf(AnimationBone bone, float timeSinceStart) {
                return new Matrix4f();
            }

            @Override
            public float duration() {
                return duration;
            }

            @Override
            public Set<AnimationBone> getDomain() {
                return Collections.emptySet();
            }

            @Override
            public void writeToDataStream(DataOutput out) {
            }
        };
    }

}
