package NG.Entities.Actions;

import NG.Engine.Game;
import NG.Entities.MonsterEntity;
import NG.MonsterSoul.Living;
import org.joml.Vector2ic;

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
     * @param beginTime     the game time at which the action should start
     * @param beginPosition the position at which the action should start
     * @param entity        the entity that is going to execute the action.
     * @return an action such that it corresponds exactly to this command, and {@link EntityAction#getStartTime()}
     * {@code == beginTime} and {@link EntityAction#getPositionAt(float)} for {@code beginTime} gives {@code
     * beginPosition}
     */
    public abstract EntityAction toAction(
            Game game, float beginTime, Vector2ic beginPosition, MonsterEntity entity
    );
}
