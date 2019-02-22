package NG.Entities.Actions;

import NG.Engine.Game;
import NG.MonsterSoul.Living;

import java.util.List;

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
     * @param preceding
     * @return an action such that it corresponds exactly to this command
     */
    public abstract List<EntityAction> toActions(Game game, EntityAction preceding);
}
