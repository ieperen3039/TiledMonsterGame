package NG.Living;

import NG.Entities.MonsterEntity;

import java.util.function.Consumer;

/**
 * any entity that accepts commands.
 * @author Geert van Ieperen created on 4-2-2019.
 * @see MonsterSoul
 */
public interface Living extends Consumer<Stimulus> {
    /**
     * @return The entity associated with this living being, or null if this Living has currently no entity
     */
    MonsterEntity entity();
}
