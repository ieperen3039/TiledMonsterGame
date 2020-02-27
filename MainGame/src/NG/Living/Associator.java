package NG.Living;

import NG.DataStructures.PriorityCollection;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @author Geert van Ieperen created on 22-2-2019.
 */
public class Associator<T extends Serializable> implements Serializable {
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
    private PriorityCollection<StimulusType> attention;

    /**
     * allows association of stimuli that occur frequently after each other (Hebbian learning)
     */
    public Associator(Class<T> mapClass, int attentionSize, int associationSize) {
        this.mapClass = mapClass;
        memory = new HashMap<>();
        this.attentionSize = attentionSize;
        attention = new PriorityCollection<>(new StimulusType[attentionSize]);
        associationMapSize = associationSize;
    }

    /**
     * collects a number of associations most closely related to the given stimulus. Does not actually record the
     * stimulus.
     * @param event a stimulus to query
     * @return a collection of size {@code resultSize} with the stimuli most closely related to the given stimulus.
     * @see #process(StimulusType, T, float)
     */
    public PriorityCollection<T> query(StimulusType event, int resultSize) {
        PriorityCollection<T> results = new PriorityCollection<>(mapClass, resultSize);

        for (StimulusType stimulus : attention) {
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
     * notifies this associator about the given stimulus. Future calls to {@link #query(StimulusType, int)} may return this
     * stimulus if the argument is one of the stimuli currently in the attention space, and the importance is high
     * enough.
     * @param event      the stimulus
     * @param importance a value that indicates how important this stimulus is relative to others. The least important
     *                   stimuli are easiest forgotten.
     */// TODO anti-hebbian learning
    public void record(T event, float importance) {
        for (StimulusType first : attention) {
            if (first == null) break; // break one layer

            for (StimulusType second : attention) {
                if (second == null) break; // break one layer
                StimulusPair pair = new StimulusPair(first, second);

                PriorityCollection<T> associations = memory.computeIfAbsent(pair, this::createAssociations);
                associations.reduceImportance((v) -> v *= (1 - ASSOCIATION_REDUCTION));
                associations.add(event, importance, ASSOCIATION_FUNCTION);
            }
        }
    }

    public void notice(StimulusType event, float importance) {
        attention.reduceImportance((v) -> v *= (1 - ATTENTION_REDUCTION));
        attention.add(event, importance, Math::max);
    }

    /**
     * notify this associator about the given simulus, and return the stimuli related to this event. Combines {@link
     * #record(T, float)} and {@link #query(StimulusType, int)}.
     * @param clue       the stimulus
     * @param importance a value that indicates how important this stimulus is relative to others. The least important
     *                   stimuli are easiest forgotten.
     * @return a number of stimuli most associated with the given source stimuli
     */
    public PriorityCollection<T> process(StimulusType clue, T source, float importance) {
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

    /**
     * a symmetric pair of {@link StimulusType} objects
     */
    private static class StimulusPair implements Serializable {
        final StimulusType one;
        final StimulusType two;

        private StimulusPair(StimulusType one, StimulusType two) {
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
