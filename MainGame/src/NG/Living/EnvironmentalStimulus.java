package NG.Living;

import org.joml.Vector3fc;

import java.util.Objects;

/**
 * @author Geert van Ieperen created on 23-2-2019.
 */
public class EnvironmentalStimulus implements Stimulus {
    private final StimulusType type;
    private final Vector3fc position;
    private final float magnitude;

    /**
     * create a stimulus with the given parameters
     * @param magnitude the magnitude on one unit distance.
     */
    protected EnvironmentalStimulus(StimulusType type, Vector3fc position, float magnitude) {
        this.type = type;
        this.position = position;
        this.magnitude = magnitude;
    }

    /**
     * @return the position of the stimulus
     */
    public Vector3fc getPosition() {
        return position;
    }

    @Override
    public float getMagnitude(Vector3fc entityPosition) {
        return magnitude / (position.distance(entityPosition) + 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EnvironmentalStimulus) {
            EnvironmentalStimulus other = (EnvironmentalStimulus) obj;

            return Objects.equals(type, other.type) &&
                    Objects.equals(position, other.position);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (type.hashCode() ^ position.hashCode());
    }
}
