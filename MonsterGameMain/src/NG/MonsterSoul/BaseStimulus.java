package NG.MonsterSoul;

import java.io.DataOutput;
import java.io.IOException;

/**
 * A base set of stimuli
 * @author Geert van Ieperen created on 23-2-2019.
 */
public enum BaseStimulus implements Type {
    /** negative status stimuli */
    DAMAGE, DEATH,

    /** positive status stimuli */
    REWARD, AFFECTION, RESPECT,

    /** neutral stimuli generated by an entity */
    ANNOYANCE,

    /** environmental auditory stimuli */
    FOOTSTEP, EXPLOSION, RUMBLE,

    /** environmental visual stimuli */
    FLASH,

    UNKNOWN;

    @Override
    public void writeToDataStream(DataOutput out) throws IOException {

    }
}
