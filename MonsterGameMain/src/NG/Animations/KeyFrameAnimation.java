package NG.Animations;

import NG.Animations.ColladaLoader.AnimationLoader;
import NG.Storable;
import NG.Tools.Logger;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
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
    private final Map<AnimationBone, TransformArray> transformations;
    /** total duration of the animation */
    private float duration;

    public KeyFrameAnimation(
            Map<String, AnimationLoader.TransformList> mapping, float lengthSeconds, BodyModel model
    ) {
        this.model = model;
        this.duration = lengthSeconds;
        this.transformations = new HashMap<>();

        for (String boneName : mapping.keySet()) {
            AnimationLoader.TransformList frame = mapping.get(boneName);
            AnimationBone bone = model.getBone(boneName);

            transformations.put(bone, new TransformArray(frame));
        }
    }

    @Override
    public Matrix4fc transformationOf(AnimationBone bone, float timeSinceStart) {
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
    public Set<AnimationBone> getDomain() {
        return transformations.keySet();
    }

    @Override
    public void writeToDataStream(DataOutput out) throws IOException {
        Storable.writeEnum(out, model);
        out.writeInt(transformations.size());
        out.writeFloat(duration);

        for (AnimationBone key : transformations.keySet()) {
            out.writeUTF(key.getName());
            TransformArray frame = transformations.get(key);
            frame.writeToDataStream(out);
        }
    }

    /**
     * Reads an animation from the data input.
     * @param in the data input stream
     * @throws IOException if anything goes wrong
     */
    public KeyFrameAnimation(DataInput in) throws IOException {
        model = Storable.readEnum(in, BodyModel.class);

        int size = in.readInt();
        transformations = new HashMap<>(size);
        duration = in.readFloat();

        for (int i = 0; i < size; i++) {
            String boneName = in.readUTF();
            AnimationBone bone = model.getBone(boneName);

            TransformArray frame = new TransformArray(in);

            transformations.put(bone, frame);
        }
    }

    public String framesOf(AnimationBone bone) {
        return transformations.get(bone).toString();
    }

    private static class TransformArray implements Storable {
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
            }

            this.timeStamps = timestamps;
            this.frames = frames;
            this.size = timestamps.length;
        }

        public TransformArray(AnimationLoader.TransformList transformList) {
            this(
                    transformList.getTimestamps(),
                    transformList.getFrames()
            );
        }

        public TransformArray(DataInput in) throws IOException {
            size = in.readInt();
            timeStamps = new float[size];
            frames = new Matrix4fc[size];

            for (int j = 0; j < size; j++) {
                timeStamps[j] = in.readFloat();
            }
            for (int j = 0; j < size; j++) {
                frames[j] = Storable.readMatrix4f(in);
            }
        }

        @Override
        public void writeToDataStream(DataOutput out) throws IOException {
            out.writeInt(size);
            for (float f : timeStamps) {
                out.writeFloat(f);
            }
            for (Matrix4fc mat : frames) {
                Storable.writeMatrix4f(out, mat);
            }
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
