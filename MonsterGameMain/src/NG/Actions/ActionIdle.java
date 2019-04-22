package NG.Actions;

import NG.Animations.AnimationBone;
import NG.Animations.BodyAnimation;
import NG.Animations.PartialAnimation;
import NG.Animations.UniversalAnimation;
import NG.Engine.Game;
import NG.GameMap.GameMap;
import NG.Tools.Vectors;
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

    /**
     * idle for an undetermined duration
     * @param position the position to idle
     */
    public ActionIdle(Game game, Vector2ic position) {
        this(game, position, Float.POSITIVE_INFINITY);
    }

    /**
     * idle for a given duration.
     * @param coordinate the position to idle
     * @param duration how long to stay idle
     */
    public ActionIdle(Game game, Vector2ic coordinate, float duration) {
        this(game.get(GameMap.class).getPosition(coordinate), duration);
    }

    /**
     * idle for an undetermined duration on the given position
     * @param position
     */
    public ActionIdle(Vector3fc position) {
        this(position, Float.POSITIVE_INFINITY);
    }

    /**
     * idle for a given duration.
     * @param position the position mapped to the 3d space
     * @param duration how long to stay idle
     */
    public ActionIdle(Vector3fc position, float duration) {
        if (duration < 0) throw new IllegalArgumentException("Idle duration must be greater than zero");
        this.position = position;
        this.duration = duration;
    }

    @Override
    public Vector3f getPositionAt(float timeSinceStart) {
        return new Vector3f(position);
    }

    @Override
    public float duration() {
        return duration;
    }

    @Override
    public String toString() {
        return "Idle (at " + Vectors.toString(position) + ")";
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
