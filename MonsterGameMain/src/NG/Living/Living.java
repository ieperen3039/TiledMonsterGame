package NG.Living;

import java.util.function.Consumer;

/**
 * any entity that accepts commands.
 * @author Geert van Ieperen created on 4-2-2019.
 * @see MonsterSoul
 */
public interface Living extends Consumer<Stimulus> {

}
