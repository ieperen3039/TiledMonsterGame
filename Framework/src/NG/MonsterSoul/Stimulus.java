package NG.MonsterSoul;

import NG.Entities.Entity;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public interface Stimulus {
    /**
     * @return the entity that caused the stimulus, or null if this is unknown / undefined
     */
    Entity source();
}
