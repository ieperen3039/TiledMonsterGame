package NG.MonsterSoul.Commands;

import NG.DataStructures.Direction;
import NG.Engine.Game;
import NG.Entities.Actions.EntityAction;
import NG.Entities.MonsterEntity;
import NG.MonsterSoul.EnvironmentalStimulus;
import NG.MonsterSoul.Living;
import NG.MonsterSoul.Stimulus;
import NG.MonsterSoul.Type;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

/**
 * a command issued to a MonsterSoul
 */
public abstract class Command implements Stimulus {
    private final Living source;

    protected Command(Living source) {
        this.source = source;
    }

    @Override
    public Type getType() {
        return new CType();
    }

    @Override
    public float getMagnitude(Vector3fc position) {
        return 0;
    }

    /**
     * @return the identity that issued the command.
     */
    public Living getSource() {
        return source;
    }

    /**
     * transforms the command into an action that is executed by the given entity and starts on the given moment in
     * time.
     * @param game      the current game instance
     * @param preceding
     * @return an action such that it corresponds exactly to this command
     */
    public abstract List<EntityAction> toActions(Game game, EntityAction preceding);

    public static class CType implements Type {
        @Override
        public void writeToFile(DataOutput out) throws IOException {

        }

        public Command generateNew(MonsterEntity entity, Stimulus cause) {
            if (cause instanceof Command && cause.getType().equals(this)) {
                return (Command) cause;

            } else if (cause instanceof EnvironmentalStimulus) {
                EnvironmentalStimulus positional = (EnvironmentalStimulus) cause;
                // run away
                Vector3f direction = new Vector3f(entity.getPosition());
                direction.sub(positional.getPosition());
                Vector2ic target = Direction.get(direction.x, direction.y).toVector();

                return new CommandWalk(entity.getController(), target);
            }

            return null;
        }

        @Override
        public boolean equals(Object obj) {
            return obj.getClass().equals(getClass()); // class equality
        }

        @Override
        public int hashCode() {
            return getClass().hashCode();
        }

        public static CType valueOf(String string) {
            return new CType();
        }
    }
}
