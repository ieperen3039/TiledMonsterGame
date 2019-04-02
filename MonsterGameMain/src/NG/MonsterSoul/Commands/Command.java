package NG.MonsterSoul.Commands;

import NG.DataStructures.Direction;
import NG.Engine.Game;
import NG.Entities.MonsterEntity;
import NG.Actions.EntityAction;
import NG.MonsterSoul.*;
import org.joml.Vector2ic;
import org.joml.Vector3f;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * a command issued to a MonsterSoul
 */
public abstract class Command implements Stimulus {
    private final Living source; // may be equal
    private final Living target;

    protected Command(Living source, Living target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public CType getType() {
        // the type of an arbitrary command is not directed to the callee
        return new CType(false);
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
     * @param preceding the last action executed
     * @return a list {@code l} of actions, sorted on first action first, such that they corresponds exactly to this command, and it holds that {@code
     * l.get(0).}{@link EntityAction#follows(EntityAction) follows}{@code (preceding)}
     */
    public abstract List<EntityAction> toActions(Game game, EntityAction preceding);

    public Living getTarget() {
        return target;
    }

    /**
     * a type of command, which can be translated back into a command of this type
     */
    public static class CType implements Type {
        private final boolean isTarget;

        public CType(boolean isTarget) {
            this.isTarget = isTarget;
        }

        // compatibility to Storable
        private CType(DataInput in) throws IOException {
            isTarget = in.readBoolean();
        }

        public boolean isTarget() {
            return isTarget;
        }

        public CType invert() {
            return new CType(!isTarget);
        }

        @Override
        public void writeToDataStream(DataOutput out) throws IOException {
            out.writeBoolean(isTarget);
        }

        public Command generateNew(MonsterEntity entity, Stimulus cause, float gametime) {
            if (cause instanceof Command && cause.getType().equals(this)) {
                return (Command) cause;

            } else if (cause instanceof EnvironmentalStimulus) {
                EnvironmentalStimulus positional = (EnvironmentalStimulus) cause;
                // run away
                Vector3f direction = entity.getPosition(gametime);
                direction.sub(positional.getPosition());
                Vector2ic target = Direction.get(direction.x, direction.y).toVector();

                MonsterSoul self = entity.getController();
                return new CommandWalk(self, self, target);
            }

            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CType) {
                CType other = (CType) obj;
                return Objects.equals(this.getClass(), other.getClass()) &&
                        this.isTarget == other.isTarget;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int h = getClass().hashCode();
            return isTarget ? h : ~h;
        }

        public static CType valueOf(String string) {
            return new CType(false);
        }
    }
}
