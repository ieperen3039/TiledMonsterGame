package NG.Living;

import NG.Actions.ActionFall;
import NG.Actions.ActionIdle;
import NG.Actions.BrokenMovementException;
import NG.Actions.Commands.Command;
import NG.Actions.EntityAction;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Generic.Pair;
import NG.Entities.Entity;
import NG.Entities.MonsterEntity;
import NG.GameMap.GameMap;
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
    protected MonsterEntity entity;
    protected Game game;

    private float previousGameTime = -Float.MAX_VALUE;

    protected MonsterMind(MonsterSoul owner) {
        this.owner = owner;
    }

    /**
     * Prepares this AI to control a new entity
     * @param entityToControl the entity now controlled by this AI
     * @param game            the game instance of the entity
     */
    void init(MonsterEntity entityToControl, Game game) {
        this.game = game;
        this.entity = entityToControl;
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
     *
     */
    public abstract void accept(Stimulus stimulus);

    /**
     * Execute the given command at the end of the current plan
     * @param game   the game instance
     * @param c      the command to be executed.
     */
    public void queueCommand(Game game, Command c) {
        plan.offer(c);

        // if currently nothing is executed, trigger an action update
        if (executionTarget == null) {
            float now = game.get(GameTimer.class).getGametime();
            EntityAction nextAction = getNextAction(now, game, entity);
            entity.currentActions.insert(nextAction, now);
        }
    }

    public EntityAction reactCollision(Game game, Entity other, float collisionTime) {
        Vector3f thisPos = entity.getPositionAt(collisionTime);
        Vector3f otherPos = other.getPositionAt(collisionTime);
        Vector3f otherToThis = thisPos.sub(otherPos, otherPos);
        executionTarget = null;

        return new ActionFall(thisPos, otherToThis, game.get(GameMap.class));
    }
}
