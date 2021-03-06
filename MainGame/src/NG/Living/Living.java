package NG.Living;

import NG.Core.GameObject;
import NG.Entities.MonsterEntity;

/**
 * Monstersouls and the player
 * @author Geert van Ieperen created on 4-2-2019.
 * @see MonsterSoul
 */
public interface Living extends GameObject {
    /**
     * @return The entity associated with this living being, or null if this Living has currently no entity
     */
    MonsterEntity entity();
}
