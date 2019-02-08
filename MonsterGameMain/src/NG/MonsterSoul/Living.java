package NG.MonsterSoul;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.function.Consumer;

/**
 * any entity that accepts commands.
 * @author Geert van Ieperen created on 4-2-2019.
 * @see MonsterSoul
 */
public interface Living extends Consumer<Stimulus> {

    /**
     * issue a command to this living entity
     * @param c the command considered by this entity
     */
    void command(Command c);

    /**
     * update the state of this living entity, including giving commands to the entity
     */
    void update();

    /**
     * a command issued to a MonsterSoul
     */
    abstract class Command {
        private Living source;

        protected Command(Living source) {
            this.source = source;
        }

        /**
         * @return the identity that issued the command.
         */
        Living getSource() {
            return source;
        }
    }

    /**
     * a command to move to a specified position on the map.
     */
    class MoveCommand extends Command {
        private final Vector2ic target;

        public MoveCommand(Vector2ic target, Living source) {
            super(source);
            this.target = new Vector2i(target);
        }

        public Vector2ic getTargetPosition() {
            return target;
        }
    }
}
