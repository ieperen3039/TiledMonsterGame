package NG.Actions.Commands;

import NG.Actions.EntityAction;
import NG.Core.Game;
import NG.Entities.MonsterEntity;
import org.joml.Vector3fc;

import java.io.Serializable;

/**
 * a command issued to a MonsterSoul
 */
public abstract class Command implements Serializable {
    /**
     * transforms the command into an action that is executed by the given entity and starts on the given moment in
     * time.
     * @param game          the current game instance
     * @param entity
     * @param startPosition the last action executed
     * @param gameTime
     * @return a list {@code l} of actions, sorted on first action first, such that they corresponds exactly to this
     * command, and it holds that {@code l.get(0).}{@link EntityAction#follows(EntityAction) follows}{@code
     * (preceding)}
     */
    public abstract EntityAction getAction(
            Game game, MonsterEntity entity, Vector3fc startPosition, float gameTime
    );

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
