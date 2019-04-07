package NG.Living;

import NG.Living.Commands.Command;
import NG.Tools.Toolbox;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 24-2-2019.
 */
public interface Stimulus {
    /**
     * @return the type of stimulus
     */
    default Type getType() {
        return new Type.ClassEquality(this);
    }

    /**
     * @param position
     * @return how loud/bright/obvious this stimulus is. A value of 1 is 'almost negligible', a value of 1000 is 'like
     * an explosion'. This value must be positive to be noticed.
     */
    default float getMagnitude(Vector3fc position) {
        return 1;
    }

    static Type getByName(String name) {
        String[] results = Toolbox.PERIOD_MATCHER.split(name);
        if (results[0].equals("BaseStimulus")) {
            return BaseStimulus.valueOf(results[1]);
        }
        if (results[0].equals("Command")) {
            return Command.CType.valueOf(results[1]);
        }

        return null;
    }

}
