package NG.Living;

import NG.DataStructures.Generic.PairList;
import NG.DataStructures.PriorityCollection;
import NG.Storable;
import NG.Tools.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiFunction;

/**
 * @author Geert van Ieperen created on 22-2-2019.
 */
public class Associator<T extends Storable> implements Storable {
    /** fraction of importance lost per incoming stimulus */
    private static final float ATTENTION_REDUCTION = 0.05f;
    /** fraction of association lost per new association */
    private static final float ASSOCIATION_REDUCTION = 0.01f;
    /** function that determine the 'growth' of an association when an earlier association is known */
    private static final BiFunction<Float, Float, Float> ASSOCIATION_FUNCTION = Float::sum;

    /** the class type where this associator is mapping to */
    private Class<T> mapClass;
    /** maximum number of elements a stimulus pair can associate with */
    private int associationMapSize;
    /** maximum number of elements in the work-memory */
    private int attentionSize; // magic number 7 +/- 2 (or 4 +/- 1)
    /** maps two stimuli to a number of other stimuli, each with an intensity value */
    private Map<StimulusPair, PriorityCollection<T>> memory; // TODO pairlist
    /** the stimuli that happened recently with the respective dominance */
    private PriorityCollection<Type> attention;

    /**
     * allows association of stimuli that occur frequently after each other (Hebbian learning)
     */
    public Associator(Class<T> mapClass, int attentionSize, int associationSize) {
        this.mapClass = mapClass;
        memory = new HashMap<>();
        this.attentionSize = attentionSize;
        attention = new PriorityCollection<>(new Type[attentionSize]);
        associationMapSize = associationSize;
    }

    /**
     * collects a number of associations most closely related to the given stimulus. Does not actually record the
     * stimulus.
     * @param event a stimulus to query
     * @return a collection of size {@code resultSize} with the stimuli most closely related to the given stimulus.
     * @see #process(Type, T, float)
     */
    public PriorityCollection<T> query(Type event, int resultSize) {
        PriorityCollection<T> results = new PriorityCollection<>(mapClass, resultSize);

        for (Type stimulus : attention) {
            if (stimulus == null) break;
            StimulusPair key = new StimulusPair(event, stimulus);

            if (memory.containsKey(key)) {
                PriorityCollection<T> associations = memory.get(key);
                results.addAll(associations);
            }
        }

        return results;
    }


    /**
     * notifies this associator about the given stimulus. Future calls to {@link #query(Type, int)} may return this
     * stimulus if the argument is one of the stimuli currently in the attention space, and the importance is high
     * enough.
     * @param event      the stimulus
     * @param importance a value that indicates how important this stimulus is relative to others. The least important
     *                   stimuli are easiest forgotten.
     */// TODO anti-hebbian learning
    public void record(T event, float importance) {
        for (Type first : attention) {
            if (first == null) break; // break one layer

            for (Type second : attention) {
                if (second == null) break; // break one layer
                StimulusPair pair = new StimulusPair(first, second);

                PriorityCollection<T> associations = memory.computeIfAbsent(pair, this::createAssociations);
                associations.reduceImportance((v) -> v *= (1 - ASSOCIATION_REDUCTION));
                associations.add(event, importance, ASSOCIATION_FUNCTION);
            }
        }
    }

    public void notice(Type event, float importance) {
        attention.reduceImportance((v) -> v *= (1 - ATTENTION_REDUCTION));
        attention.add(event, importance, Math::max);
    }

    /**
     * notify this associator about the given simulus, and return the stimuli related to this event. Combines {@link
     * #record(T, float)} and {@link #query(Type, int)}.
     * @param clue       the stimulus
     * @param importance a value that indicates how important this stimulus is relative to others. The least important
     *                   stimuli are easiest forgotten.
     * @return a number of stimuli most associated with the given source stimuli
     */
    public PriorityCollection<T> process(Type clue, T source, float importance) {
        PriorityCollection<T> results = query(clue, attentionSize);
        record(source, importance);
        notice(clue, importance);
        return results;
    }

    /**
     * gives an indication of how many stimuli are associated.
     * @return the sum of for each pair of stimuli, the number of associated values.
     */
    public int getNrOfAssociations() {
        int size = 0;
        for (PriorityCollection associations : memory.values()) {
            size += associations.size();
        }
        return size;
    }

    private PriorityCollection<T> createAssociations(StimulusPair pair) {
        return new PriorityCollection<>(mapClass, associationMapSize);
    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        // create mapping of stimuli
        Map<Type, Integer> idMap = new HashMap<>(memory.size());

        for (StimulusPair pair : memory.keySet()) {
            idMap.putIfAbsent(pair.one, idMap.size());
            idMap.putIfAbsent(pair.two, idMap.size());
        }
//        for (PriorityCollection<T> associations : memory.values()) {
//            for (T elt : associations) {
//                // ?
//            }
//        }

        Logger.DEBUG.printf("Writing %d different stimuli", idMap.size());

        out.writeInt(idMap.size());
        for (Type s : idMap.keySet()) {
            Storable.write(out, s);
        }

        // output data
        out.writeInt(memory.size());
        for (StimulusPair pair : memory.keySet()) {
            out.writeInt(idMap.get(pair.one));
            out.writeInt(idMap.get(pair.two));

            PairList<T, Float> associations = memory.get(pair).asPairList();
            out.writeInt(associations.size());
            for (int i = 0; i < associations.size(); i++) {
                Storable elt = associations.left(i);
                Storable.write(out, elt);
                out.writeFloat(associations.right(i));
            }
        }
    }

    public Associator(DataInputStream in, Class<T> expected) throws IOException, ClassNotFoundException {
        // read stimuli mapping
        int nrOfStimuli = in.readInt();
        List<Type> idMap = new ArrayList<>(nrOfStimuli);

        for (int i = 0; i < nrOfStimuli; i++) {
            Type stimulus = Storable.read(in, Type.class);
            idMap.add(stimulus); // each stimulus occurs only once
        }

        // read data
        int nrOfPairs = in.readInt();
        memory = new HashMap<>(nrOfPairs);

        for (int i = 0; i < nrOfPairs; i++) {
            Type one = idMap.get(in.readInt());
            Type two = idMap.get(in.readInt());
            StimulusPair pair = new StimulusPair(one, two);

            int nrOfAssoc = in.readInt();
            //noinspection unchecked
            T[] stimuli = (T[]) Array.newInstance(expected.getComponentType(), nrOfAssoc);
            float[] relevances = new float[nrOfAssoc];

            for (int j = 0; j < nrOfAssoc; j++) {
                stimuli[j] = Storable.read(in, expected);
                relevances[j] = in.readFloat();
            }

            memory.put(pair, new PriorityCollection<>(stimuli, relevances));
        }
        attentionSize = 6;
        associationMapSize = 10;
    }

    /**
     * a symmetric pair of {@link Type} objects
     */
    private static class StimulusPair {
        final Type one;
        final Type two;

        private StimulusPair(Type one, Type two) {
            this.one = one;
            this.two = two;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StimulusPair)) return false;
            StimulusPair other = (StimulusPair) obj;

            if (Objects.equals(one, other.one) && Objects.equals(two, other.two)) return true;
            return Objects.equals(one, other.two) && Objects.equals(two, other.one);
        }

        @Override
        public int hashCode() {
            int oneHash = one.hashCode();
            int twoHash = two.hashCode();
            return (oneHash == twoHash) ? oneHash : oneHash ^ twoHash; // reflexive
        }

        @Override
        public String toString() {
            return "[" + one + ", " + two + "]";
        }
    }
}
