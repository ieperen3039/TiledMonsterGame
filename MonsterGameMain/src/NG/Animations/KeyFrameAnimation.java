package NG.Animations;

import NG.Animations.ColladaLoader.AnimationData;
import NG.Animations.ColladaLoader.KeyFrameData;
import NG.Storable;
import NG.Tools.Logger;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public class KeyFrameAnimation implements Animation {
    /** maps the bone to the index in data */
    private final Map<AnimationBone, ArrayFrame> transformations;
    /** total duration of the animation */
    private float duration;

    public KeyFrameAnimation(AnimationData data) {
        this();
        addData(data);
    }

    public KeyFrameAnimation() {
        transformations = new HashMap<>();
        duration = 0;
    }

    private void addData(AnimationData data) {
        Map<AnimationBone, ListFrame> mapping = new HashMap<>();

        for (KeyFrameData keyFrame : data.keyFrames) {
            Map<String, Matrix4f> transforms = keyFrame.jointTransforms;

            for (String boneName : transforms.keySet()) {
                AnimationBone bone = AnimationBone.getByName(boneName);
                if (bone == null) {
                    Logger.WARN.print("Unknown bone: " + boneName);
                    continue;
                }

                ListFrame frame;
                if (mapping.containsKey(bone)) {
                    frame = mapping.get(bone);

                } else if (transformations.containsKey(bone)) {
                    frame = new ListFrame(transformations.get(bone)); // #1

                } else {
                    frame = new ListFrame();
                }

                Matrix4f mat = transforms.get(boneName);
                // check for affine transformation.
                assert (mat.determineProperties().properties() & Matrix4fc.PROPERTY_AFFINE) != 0 : "\n" + mat;

                frame.add(keyFrame.time, mat);
                mapping.put(bone, frame);
            }
        }

        for (AnimationBone bone : mapping.keySet()) {
            ListFrame frame = mapping.get(bone);
            // if this overrides, then it used the source at #1
            transformations.put(bone, frame.toArrayFrame());
        }

        if (data.lengthSeconds > duration) {
            duration = data.lengthSeconds;
        }
    }

    @Override
    public Matrix4fc transformationOf(AnimationBone bone, float timeSinceStart) {
        if (timeSinceStart > duration) {
            Logger.WARN.print("Time was " + timeSinceStart + " but duration is " + duration);
        }

        ArrayFrame frame = transformations.get(bone);
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
        out.writeInt(transformations.size());
        out.writeFloat(duration);

        for (AnimationBone key : transformations.keySet()) {
            out.writeUTF(key.getName());
            ArrayFrame frame = transformations.get(key);
            frame.writeToDataStream(out);
        }
    }

    /**
     * Reads an animation from the data input.
     * @param in the data input stream
     * @throws IOException if anything goes wrong
     */
    public KeyFrameAnimation(DataInput in) throws IOException {
        int size = in.readInt();
        transformations = new HashMap<>(size);
        duration = in.readFloat();

        for (int i = 0; i < size; i++) {
            String boneName = in.readUTF();
            AnimationBone bone = AnimationBone.getByName(boneName);

            ArrayFrame frame = new ArrayFrame(in);

            transformations.put(bone, frame);
        }

    }

    private class ArrayFrame implements Storable {
        // sorted arrays
        private final int size;
        private final float[] timeStamps;
        private final Matrix4fc[] frames;

        /**
         * @param timestamps sorted arrays of timestamps
         * @param frames     matrices, of the same length
         */
        public ArrayFrame(float[] timestamps, Matrix4fc[] frames) {
            if (timestamps.length != frames.length) {
                throw new IllegalArgumentException("Arrays are of unequal length");
            }

            this.timeStamps = timestamps;
            this.frames = frames;
            this.size = timestamps.length;
        }

        public ArrayFrame(DataInput in) throws IOException {
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
    }

    private class ListFrame {
        // these lists are always sorted
        private final ArrayList<Float> timestamps;
        private final ArrayList<Matrix4fc> frames;

        private ListFrame() {
            frames = new ArrayList<>();
            timestamps = new ArrayList<>();
        }

        private ListFrame(ArrayFrame source) {
            this();
            for (int i = 0; i < source.timeStamps.length; i++) {
                add(source.timeStamps[i], source.frames[i]);
            }
        }

        public void add(float time, Matrix4fc frame) {
            int index = Collections.binarySearch(timestamps, time);

            if (index < 0) {
                // index = -(insertion point) - 1  <=>  insertion point = -index - 1
                index = -index - 1;
            }

            timestamps.add(index, time);
            frames.add(index, frame);
        }

        public ArrayFrame toArrayFrame() {
            float[] timestamps = getTimestamps();
            Matrix4fc[] matrices = frames.toArray(new Matrix4fc[0]);

            return new ArrayFrame(
                    timestamps, matrices
            );
        }

        private float[] getTimestamps() {
            float[] floats = new float[timestamps.size()];
            for (int i = 0; i < timestamps.size(); i++) {
                floats[i] = timestamps.get(i);
            }
            return floats;
        }

    }
}
