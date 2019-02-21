package NG.MonsterSoul;

import NG.Storable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Geert van Ieperen created on 5-2-2019.
 */
public enum Emotion {
    /** Shallow emotions */
    EXCITEMENT, CURIOUSNESS, FRIGHT,

    /** Hidden emotions */
    ANGER, PATIENCE,

    /** Deep emotions */
    RESPECT, SELF_CONFIDENCE, STRESS;

    private static final float MINIMUM_PROCESS_DELTA = 0.1f;
    public static final int count = Emotion.values().length;

    public static class Collection implements Storable { // TODO MAKE TEST JAR FOR TILING
        private final float[][] transformationMatrix;
        private final short[] values;

        public Collection(Path file) {
            transformationMatrix = new float[Emotion.count][Emotion.count];
            values = new short[Emotion.count];
            // read file
            // map names to new numbers
        }

        public void process(float deltaTime) {
            while (deltaTime > MINIMUM_PROCESS_DELTA) {
                process(MINIMUM_PROCESS_DELTA);
                deltaTime -= MINIMUM_PROCESS_DELTA;
            }

            for (int i = 0; i < Emotion.count; i++) {
                float[] transformations = transformationMatrix[i];
                float newValue = 0;

                for (int j = 0; j < Emotion.count; j++) {
                    newValue += transformations[j] * values[j];
                }

                values[i] = (short) (newValue * deltaTime);
            }
        }

        public short get(Emotion type) {
            return values[type.ordinal()];
        }

        public void add(Emotion target, int value) {
            values[target.ordinal()] += value;
        }

        @Override
        public void writeToFile(DataOutput out) throws IOException {
            out.writeInt(Emotion.count);

            for (short value : values) {
                out.writeShort(value);
            }

            for (float[] array : transformationMatrix) {
                for (float f : array) {
                    out.writeFloat(f);
                }
            }
        }

        // TODO add robustness in matching names
        public Collection(DataInput in) throws IOException {
            int nrOfEmotions = in.readInt();
            if (nrOfEmotions != Emotion.count) {
                throw new IOException("Source emotion set is of different size as current");
            }

            transformationMatrix = new float[Emotion.count][Emotion.count];
            values = new short[Emotion.count];

            for (int i = 0; i < nrOfEmotions; i++) {
                values[i] = in.readShort();
            }

            for (int i = 0; i < nrOfEmotions; i++) {
                for (int j = 0; j < nrOfEmotions; j++) {
                    transformationMatrix[i][j] = in.readFloat();
                }
            }
        }
    }
}
