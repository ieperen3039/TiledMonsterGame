package NG.Actions.Commands;

import NG.Actions.EntityAction;
import NG.Core.Game;
import NG.Living.Living;
import org.joml.Vector3fc;

/**
 * a command issued to a MonsterSoul
 */
public abstract class Command {
    private final Living target;

    protected Command(Living target) {
        this.target = target;
    }

    /**
     * transforms the command into an action that is executed by the given entity and starts on the given moment in
     * time.
     * @param game          the current game instance
     * @param startPosition the last action executed
     * @param gameTime
     * @return a list {@code l} of actions, sorted on first action first, such that they corresponds exactly to this
     * command, and it holds that {@code l.get(0).}{@link EntityAction#follows(EntityAction) follows}{@code
     * (preceding)}
     */
    public abstract EntityAction getAction(Game game, Vector3fc startPosition, float gameTime);

    /**
     * @return the entity that is supposed to execute the command
     */
    public Living getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
