package NG.Living;

import NG.Actions.ActionIdle;
import NG.Actions.BrokenMovementException;
import NG.Actions.Commands.Command;
import NG.Actions.EntityAction;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Generic.Pair;
import NG.Entities.MonsterEntity;
import org.joml.Vector3f;

import java.util.ArrayDeque;

/**
 * An AI for monsters. The base implementation just follows exactly and only the commands given
 * @author Geert van Ieperen created on 5-2-2020.
 */
public abstract class MonsterMind {
    protected ArrayDeque<Command> plan = new ArrayDeque<>(16);
    protected Command executionTarget;
    protected final MonsterSoul owner;

    private float previousGameTime = -Float.MAX_VALUE;

    protected MonsterMind(MonsterSoul owner) {
        this.owner = owner;
    }

    /**
     * returns the next action to execute at the given game time.
     * @param gameTime planned time of execution of the action.
     * @param game     the game instance
     * @param entity   the entity that is controlled.
     * @return the next action that the given entity should execute
     */
    public EntityAction getNextAction(float gameTime, Game game, MonsterEntity entity) {
        assert (gameTime >= previousGameTime) : "getNextAction calls in non-chronological order";
        previousGameTime = gameTime;

        Pair<EntityAction, Float> action = entity.currentActions.getActionAt(gameTime);
        EntityAction previous = action.left;
        Vector3f position = previous.getPositionAt(action.right);

        if (executionTarget == null) {
            executionTarget = plan.poll();
        }

        while (executionTarget != null) {
            EntityAction next = executionTarget.getAction(game, position, gameTime);

            if (next != null) {
                if (next != previous && !next.getStartPosition().equals(position)) {
                    throw new BrokenMovementException(previous, next, action.right);
                }

                return next;
            }

            executionTarget = plan.poll();
        }

        return new ActionIdle(position);
    }

    protected abstract void update(float gametime);

    /**
     * pass a signal to this AI.
     * @param stimulus the signal to process
     * @param game     the game instance
     */
    public abstract void accept(Stimulus stimulus, Game game);

    /**
     * Execute the given command at the end of the current plan
     * @param game   the game instance
     * @param c      the command to be executed.
     * @param entity the entity being controlled
     */
    protected void queueCommand(Game game, Command c, MonsterEntity entity) {
        plan.offer(c);

        // if currently nothing is executed, trigger an action update
        if (executionTarget == null) {
            float now = game.get(GameTimer.class).getGametime();
            EntityAction nextAction = getNextAction(now, game, entity);
            entity.currentActions.insert(nextAction, now);
        }
    }
}
