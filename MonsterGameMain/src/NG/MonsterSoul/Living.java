package NG.MonsterSoul;

import NG.Entities.Actions.Command;

import java.util.function.Consumer;

/**
 * any entity that accepts commands.
 * @author Geert van Ieperen created on 4-2-2019.
 * @see MonsterSoul
 */
public interface Living extends Consumer<Stimulus> {

    /**
     * issue a command to this unit. The unit may ignore or decide to do other things.
     * @param c the command considered by this unit
     */
    void command(Command c);

    /**
     * update the state of this living entity
     */
    void update();

}
