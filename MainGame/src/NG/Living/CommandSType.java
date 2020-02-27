package NG.Living;

import NG.Actions.Commands.Command;
import NG.Actions.Commands.CommandWalk;
import NG.DataStructures.Direction;
import NG.Entities.MonsterEntity;
import org.joml.Vector2ic;
import org.joml.Vector3f;

import java.util.Objects;

/**
 * @author Geert van Ieperen created on 7-2-2020.
 */
public class CommandSType implements StimulusType {
    private final boolean isTarget;

    public CommandSType(boolean isTarget) {
        this.isTarget = isTarget;
    }

    public boolean isTarget() {
        return isTarget;
    }

    public CommandSType invert() {
        return new CommandSType(!isTarget);
    }

    public Command generateNew(MonsterEntity entity, Stimulus cause, float gametime) {
        if (cause instanceof Command && cause.getType().equals(this)) {
            return (Command) cause;

        } else if (cause instanceof EnvironmentalStimulus) {
            EnvironmentalStimulus positional = (EnvironmentalStimulus) cause;
            // run away
            Vector3f direction = entity.getPositionAt(gametime);
            direction.sub(positional.getPosition());
            Vector2ic target = Direction.get(direction.x, direction.y).toVector();

            MonsterSoul self = entity.getController();
            return new CommandWalk(self, target);
        }

        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CommandSType) {
            CommandSType other = (CommandSType) obj;
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

    public static CommandSType valueOf(String string) {
        return new CommandSType(false);
    }

}
