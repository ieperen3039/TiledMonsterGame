package NG.Animations;

import org.joml.Matrix4fc;

/**
 * Several {@link PartialAnimation Animations} combined into one animation.
 * @author Geert van Ieperen created on 4-3-2019.
 */
public class CompoundAnimation implements UniversalAnimation {
    private final UniversalAnimation[] animations;
    private final UniversalAnimation lastAnimation;
    private final float totalDuration;

    public CompoundAnimation(UniversalAnimation... aniArray) {
        int nrOfAnimations = aniArray.length;
        assert nrOfAnimations > 0;

        float duration = aniArray[0].duration();

        for (int i = 1; i < aniArray.length; i++) {
            UniversalAnimation action = aniArray[i];

            if (action == null) {
                throw new NullPointerException(String.format("animation %d/%d was null", i + 1, aniArray.length));
            }

            duration += action.duration();
        }

        this.lastAnimation = aniArray[nrOfAnimations - 1];
        this.animations = aniArray;
        this.totalDuration = duration;
    }

    @Override
    public Matrix4fc transformationOf(AnimationBone bone, float timeSinceStart) {
        if (timeSinceStart <= 0) {
            return animations[0].transformationOf(bone, 0);

        } else if (timeSinceStart >= totalDuration) {
            float end = lastAnimation.duration();
            return lastAnimation.transformationOf(bone, end);
        }

        for (UniversalAnimation anim : animations) {
            float duration = anim.duration();
            if (timeSinceStart > duration) {
                timeSinceStart -= duration;

            } else {
                return anim.transformationOf(bone, timeSinceStart);
            }
        }

        throw new AssertionError("invalid value of totalDuration, missing " + timeSinceStart);
    }

    @Override
    public float duration() {
        return totalDuration;
    }
}
