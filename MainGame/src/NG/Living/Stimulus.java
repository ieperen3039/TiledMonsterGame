package NG.Living;

import NG.Tools.Toolbox;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 24-2-2019.
 */
public interface Stimulus {

    /**
     * @return the percieved position of the stimulus
     */
    Vector3fc getPosition();

    /**
     * @return the type of stimulus
     */
    default StimulusType getType() {
        return BaseStimulus.UNKNOWN;
    }

    /**
     * @param position
     * @return how loud/bright/obvious this stimulus is. A value of 1 is 'almost negligible', a value of 1000 is 'like
     * an explosion'. This value must be positive to be noticed.
     */
    default float getMagnitude(Vector3fc position) {
        return 1;
    }

    default float getTime() {
        return 0;
    }

    static StimulusType getByName(String name) {
        String[] results = Toolbox.PERIOD_MATCHER.split(name);
        if (results[0].equals("BaseStimulus")) {
            return BaseStimulus.valueOf(results[1]);
        }

        return null;
    }

}
