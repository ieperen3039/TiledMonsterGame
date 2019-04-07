package NG.Living;

import NG.Storable;
import NG.Tools.Toolbox;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Geert van Ieperen created on 5-2-2019.
 */
public enum Emotion {
    /** Shallow emotions */
    EXCITEMENT, CURIOUSNESS, FRIGHT, // focus,

    /** Hidden emotions */
    ANGER, PATIENCE,

    /** Deep emotions */
    SELF_CONFIDENCE, STRESS;

    private static final float PROCESS_DELTA = 1f; // update interval in seconds
    private static final Emotion[] VALUES = values();
    public static final int count = VALUES.length;
    private static final short MAX_VALUE = Short.MAX_VALUE;
    private static final short MIN_VALUE = 0;
    private static final Pattern PIPES = Pattern.compile("\\|");

    /**
     * a translation that can be applied on a emotion collection
     */
    public static class Translation {
        private EnumMap<Emotion, Integer> content;

        public Translation() {
            content = new EnumMap<>(Emotion.class);
        }

        public void set(Emotion tgt, int frac) {
            content.put(tgt, frac);
        }

        public void addTo(Collection emotions) {
            content.forEach(emotions::add);
        }

        public void addTo(Collection emotions, float multiplier) {
            content.forEach((target, value) -> emotions.add(target, (int) (value * multiplier)));
        }

        public float calculateValue(EnumMap<Emotion, Float> values) {
            float acc = 0;
            for (Emotion emotion : content.keySet()) {
                if (values.containsKey(emotion)) {
                    acc += content.get(emotion) * values.get(emotion);
                }
            }

            return acc;
        }
    }

    /**
     * a collection of emotions, with a processing element such that the emotions affect each other.
     */
    public static class Collection implements Storable {
        private final float[][] transformationMatrix; // row-major
        private final short[] values;
        private float stateTime = PROCESS_DELTA;

        public Collection(Scanner reader) {
            transformationMatrix = new float[Emotion.count][Emotion.count];
            values = new short[Emotion.count];

            List<Emotion> seen = new ArrayList<>(count);
            List<float[]> tgtMatrix = new ArrayList<>();

            String line;
            while (reader.hasNext()) {
                line = reader.nextLine().trim();
                if (line.equals("end")) break;
                if (line.isEmpty() || line.charAt(0) == '#') continue;

                line = PIPES.matcher(line).replaceAll("");
                String[] elts = Toolbox.WHITESPACE_PATTERN.split(line);

                Emotion emotion = valueOf(elts[0]);
                short value = (short) Integer.parseInt(elts[1]);
                this.values[emotion.ordinal()] = value;

                seen.add(emotion);
                float[] fractions = new float[elts.length - 2];
                for (int i = 0; i < fractions.length; i++) {
                    fractions[i] = Float.parseFloat(elts[i + 2]);
                }
                tgtMatrix.add(fractions);
            }

            // now we know all emotions
            for (int i = 0; i < seen.size(); i++) {
                float[] row = tgtMatrix.get(i);
                for (int j = 0; j < row.length; j++) {
                    int firstInd = seen.get(i).ordinal();
                    int secondInd = seen.get(j).ordinal();
                    float fraction = (1 + row[j]) / count; // apply normalisation
                    transformationMatrix[firstInd][secondInd] = (float) Math.pow(fraction, 1f / PROCESS_DELTA);
                }
            }

//            Logger.DEBUG.print("Transformation matrix:\n" + asMatrix());
        }

        public void process(float currentTime) {
            while (stateTime < currentTime) {
                for (int i = 0; i < Emotion.count; i++) {
                    float newValue = 0;

                    for (int j = 0; j < Emotion.count; j++) {
                        newValue += values[j] * transformationMatrix[i][j];
                    }

                    values[i] = (short) (newValue * PROCESS_DELTA);
                }

                stateTime += PROCESS_DELTA;
            }
        }

        public short get(Emotion type) {
            return values[type.ordinal()];
        }

        public void add(Emotion target, int value) {
            int index = target.ordinal();
            int newValue = values[index] + value;
            values[index] = (short) Math.max(Math.min(newValue, MAX_VALUE), MIN_VALUE);
        }

        @Override
        public void writeToDataStream(DataOutput out) throws IOException {
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

        /**
         * Translates the matrix into MATLAB-format
         * @return a column-major transformation matrix, with columns separated with a ';' and packed in square
         * brackets.
         */
        public String asMatrix() {
            StringBuilder matrix = new StringBuilder("[");

            for (int j = 0; j < count; j++) {
                matrix.append("[");
                matrix.append(String.format(Locale.US, "%1.05f", transformationMatrix[0][j]));

                for (int i = 1; i < count; i++) {
                    float v = transformationMatrix[i][j];
                    matrix.append(String.format(Locale.US, ", %1.05f", v));
                }
                matrix.append("];");
            }

            return matrix.toString() + "]";
        }

        /**
         * @param judgement
         * @return the emotions of this collection, each multiplied with the given mapping and summed
         */
        public float calculateJoy(EnumMap<Emotion, Float> judgement) {
            float acc = 0;
            for (int i = 0; i < count; i++) {
                Emotion tgt = VALUES[i];

                if (judgement.containsKey(tgt)) {
                    acc += values[i] * judgement.get(tgt);
                }
            }

            return acc;
        }
    }
}
