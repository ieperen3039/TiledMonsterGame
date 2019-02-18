package NG.Entities.Actions;

import NG.Engine.Game;
import NG.Entities.MonsterEntity;
import NG.MonsterSoul.Living;

/**
 * a command issued to a MonsterSoul
 */
public abstract class Command {
    private final Living source;

    protected Command(Living source) {
        this.source = source;
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
     * @param game          the current game instance
     * @param entity        the entity that is going to execute the action.
     * @return an action such that it corresponds exactly to this command
     */
    public abstract EntityAction toAction(Game game, MonsterEntity entity);
}
