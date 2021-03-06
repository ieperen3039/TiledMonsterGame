package NG.Animations;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Geert van Ieperen created on 6-3-2019.
 */
public class AnimationCombiner implements UniversalAnimation, PartialAnimation {
    private final Map<SkeletonBone, PartialAnimation> mux;
    private final float duration;

    public AnimationCombiner(PartialAnimation... parts) {
        mux = new HashMap<>();
        duration = parts[0].duration();

        for (PartialAnimation part : parts) {
            assert part.duration() == duration;
            add(part);
        }
    }

    public void add(PartialAnimation part) {
        for (SkeletonBone bone : part.getDomain()) {
            mux.put(bone, part);
        }
    }

    @Override
    public Matrix4fc transformationOf(SkeletonBone bone, float timeSinceStart) {
        PartialAnimation animation = mux.get(bone);
        if (animation == null) {
            return new Matrix4f();
        }
        return animation.transformationOf(bone, timeSinceStart);
    }

    @Override
    public float duration() {
        return duration;
    }

    @Override
    public Set<SkeletonBone> getDomain() {
        return mux.keySet();
    }
}
