package NG.Animations;

import NG.Storable;
import org.joml.Matrix4fc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Several {@link Animation Animations} combined into one animation.
 * @author Geert van Ieperen created on 4-3-2019.
 */
public class CompoundAnimation implements Animation {
    private final Animation[] animations;
    private final Animation lastAnimation;
    private final Set<AnimationBone> domain;
    private final float totalDuration;

    public CompoundAnimation(Animation... aniArray) {
        int nrOfAnimations = aniArray.length;
        assert nrOfAnimations > 0;

        float duration = aniArray[0].duration();
        this.domain = new HashSet<>();

        for (int i = 1; i < aniArray.length; i++) {
            Animation action = aniArray[i];

            if (action == null) {
                throw new NullPointerException(String.format("animation %d/%d was null", i + 1, aniArray.length));
            }

            domain.addAll(action.getDomain());
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

        for (Animation anim : animations) {
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

    @Override
    public Set<AnimationBone> getDomain() {
        return domain;
    }

    @Override
    public void writeToDataStream(DataOutput out) throws IOException {
        out.writeInt(animations.length);
        for (Animation ani : animations) {
            Storable.write(out, ani);
        }
    }

    public CompoundAnimation(DataInput in) throws IOException, ClassNotFoundException {
        int nrOfAnimations = in.readInt();
        animations = new Animation[nrOfAnimations];
        domain = new HashSet<>();
        float duration = 0;

        for (int i = 0; i < nrOfAnimations; i++) {
            Animation ani = Storable.read(in, Animation.class);
            duration += ani.duration();
            animations[i] = ani;
            domain.addAll(ani.getDomain());
        }

        lastAnimation = animations[nrOfAnimations - 1];
        totalDuration = duration;
    }
}
