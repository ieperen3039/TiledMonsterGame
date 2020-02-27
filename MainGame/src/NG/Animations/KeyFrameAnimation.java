package NG.Animations;

import NG.DataStructures.Generic.PairList;
import NG.Tools.Logger;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An animation for a single body model, using keyframes as animation source.
 * @author Geert van Ieperen created on 28-2-2019.
 */
public class KeyFrameAnimation implements PartialAnimation {
    protected final BodyModel model;
    /** maps the bone to the index in data */
    private final Map<SkeletonBone, TransformArray> transformations;
    /** total duration of the animation */
    private float duration;

    public KeyFrameAnimation(
            BodyModel model, Map<String, PairList<Float, Matrix4f>> animation, float duration
    ) {
        this.model = model;
        this.duration = duration;
        this.transformations = new HashMap<>();

        for (String boneName : animation.keySet()) {
            PairList<Float, Matrix4f> pairs = animation.get(boneName);
            Float[] timeStamps = pairs.leftToArray(new Float[0]);
            Matrix4fc[] frames = pairs.rightToArray(new Matrix4f[0]);

            SkeletonBone bone = model.getBone(boneName);
            TransformArray transforms = new TransformArray(timeStamps, frames);
            transformations.put(bone, transforms);
        }
    }

    @Override
    public Matrix4fc transformationOf(SkeletonBone bone, float timeSinceStart) {
        if (timeSinceStart > duration) {
            Logger.WARN.print("Time was " + timeSinceStart + " but duration is " + duration);
        }

        TransformArray frame = transformations.get(bone);
        if (frame == null) throw new IllegalArgumentException(bone + " is not a target of this animation");

        return frame.interpolate(timeSinceStart);
    }

    @Override
    public float duration() {
        return duration;
    }

    @Override
    public Set<SkeletonBone> getDomain() {
        return transformations.keySet();
    }

    public String framesOf(SkeletonBone bone) {
        return transformations.get(bone).toString();
    }

    private static class TransformArray implements Serializable {
        // sorted arrays
        private final int size;
        private final float[] timeStamps;
        private final Matrix4fc[] frames;

        /**
         * @param timestamps sorted arrays of timestamps
         * @param frames     matrices, of the same length
         */
        public TransformArray(float[] timestamps, Matrix4fc[] frames) {
            if (timestamps.length != frames.length) {
                throw new IllegalArgumentException("Arrays are of unequal length");
            } else if (timestamps.length == 0) {
                throw new IllegalStateException("Arrays are empty");
            }

            this.timeStamps = timestamps;
            this.frames = frames;
            this.size = timestamps.length;
        }

        public TransformArray(Float[] timeStamps, Matrix4fc[] frames) {
            if (timeStamps.length != frames.length) {
                throw new IllegalArgumentException("Arrays are of unequal length");
            } else if (timeStamps.length == 0) {
                throw new IllegalStateException("Arrays are empty");
            }

            // unwrap floats
            float[] floats = new float[timeStamps.length];
            for (int i = 0; i < timeStamps.length; i++) {
                floats[i] = timeStamps[i];
            }
            this.timeStamps = floats;

            this.frames = frames;
            this.size = timeStamps.length;
        }

        public Matrix4fc interpolate(float timeSinceStart) {
            int index = Arrays.binarySearch(timeStamps, timeSinceStart);
            if (index >= 0) return frames[index]; // precise element

            // index = -(insertion point) - 1  <=>  insertion point = -index - 1
            int lowerPoint = -index - 2;

            if (lowerPoint == -1) return frames[0];
            if (lowerPoint == timeStamps.length - 1) return frames[lowerPoint];

            // TODO: advanced interpolation
            float deltaTime = timeStamps[lowerPoint + 1] - timeStamps[lowerPoint];
            float fraction = (timeSinceStart - timeStamps[lowerPoint]) / deltaTime;

            Matrix4fc lowerPosition = frames[lowerPoint];
            Matrix4fc higherPosition = frames[lowerPoint + 1];
            return new Matrix4f(lowerPosition).lerp(higherPosition, fraction);
        }

        public String toString() {
            StringBuilder chunk = new StringBuilder();
            for (int i = 0; i < size; i++) {
                chunk.append(timeStamps[i]);
                chunk.append('\n');
                chunk.append(frames[i]);
                chunk.append('\n');
            }
            return chunk.toString();
        }
    }

}
