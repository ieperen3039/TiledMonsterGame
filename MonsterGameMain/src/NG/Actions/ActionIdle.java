package NG.Actions;

import NG.Animations.AnimationBone;
import NG.Animations.BodyAnimation;
import NG.Animations.PartialAnimation;
import NG.Animations.UniversalAnimation;
import NG.Engine.Game;
import NG.GameMap.GameMap;
import org.joml.*;

import java.io.DataOutput;
import java.util.Collections;
import java.util.Set;

/**
 * Stand still and do nothing at all. Can be interrupted any time.
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class ActionIdle implements EntityAction {
    private final Vector3fc position;
    private final Vector2ic coordinate;
    private float startTime;
    private float duration;
    private boolean cancelled = false;

    /**
     * idle for a given duration after executing the given task
     * @param game
     * @param preceding the action that happens just before this idling
     * @param duration  the duration of staying idle.
     * @param startTime the timestamp when the action starts
     */
    public ActionIdle(Game game, EntityAction preceding, float duration, float startTime) {
        this(game, preceding.getEndCoordinate(), duration, startTime);
    }

    /**
     * idle for an undetermined duration
     * @param position the position to idle
     * @param startTime
     */
    public ActionIdle(Game game, Vector2ic position, float startTime) {
        this(game, position, 0, startTime);
    }

    /**
     * idle for a given duration.
     * @param coordinate the position to idle
     * @param duration how long to stay idle
     * @param startTime the timestamp when the action starts
     */
    public ActionIdle(Game game, Vector2ic coordinate, float duration, float startTime) {
        this(coordinate, game.get(GameMap.class).getPosition(coordinate), duration, startTime);
    }

    /**
     * idle for a given duration.
     * @param coordinate the position to idle
     * @param position the position mapped to the 3d space
     * @param duration how long to stay idle
     * @param startTime the timestamp when the action starts
     */
    public ActionIdle(Vector2ic coordinate, Vector3fc position, float duration, float startTime) {
        if (duration < 0) throw new IllegalArgumentException("Idle duration must be greater than zero");
        this.position = position;
        this.coordinate = coordinate;
        this.duration = duration;
        this.startTime = startTime;
    }

    @Override
    public Vector3f getPositionAt(float currentTime) {
        return new Vector3f(position);
    }

    @Override
    public void interrupt(float time) {
        if (time > endTime()) return;
        if (time < startTime) {
            cancelled = true;
            return;
        }
        duration = time - startTime;
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
    public float startTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return "Idle (for " + duration + ")";
    }

    @Override
    public float getMagnitude(Vector3fc position) {
        return 0;
    }

    @Override
    public UniversalAnimation getAnimation() {
        return BodyAnimation.IDLE;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public float endTime() {
        return startTime + duration;
    }

    public float getDuration() {
        return duration;
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
