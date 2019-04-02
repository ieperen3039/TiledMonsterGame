package NG.Animations;

import org.joml.Matrix4fc;

/**
 * An animation that has an entry for every body model
 * @author Geert van Ieperen created on 24-3-2019.
 */
public interface UniversalAnimation {
    /**
     * @param bone           a bone
     * @param timeSinceStart the time since the start of this animation
     * @return the transformation of the joint that controls the given bone. If {@code timeSinceStart} is more than
     * {@link #duration()}, the result is undefined.
     */
    Matrix4fc transformationOf(AnimationBone bone, float timeSinceStart);

    /**
     * query the duration of this animation.
     * @return the last timestamp for which this animation has values.
     */
    float duration();
}
