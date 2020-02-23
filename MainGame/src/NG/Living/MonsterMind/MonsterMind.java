package NG.Living.MonsterMind;

import NG.Actions.*;
import NG.Actions.Commands.Command;
import NG.Actions.Commands.CommandWalk;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Generic.Pair;
import NG.Entities.Entity;
import NG.Entities.MonsterEntity;
import NG.GameMap.GameMap;
import NG.InputHandling.MouseTools.CommandProvider;
import NG.Living.MonsterSoul;
import NG.Living.Stimulus;
import NG.Tools.Logger;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An AI for monsters, which selects what moves are executed. The base implementation just follows exactly and only the
 * commands given.
 * @author Geert van Ieperen created on 5-2-2020.
 */
public abstract class MonsterMind {
    protected ArrayDeque<Command> plan = new ArrayDeque<>(16);
    protected Command executionTarget;

    protected final MonsterSoul owner;
    protected MonsterEntity entity;
    protected Game game;
    protected List<CommandProvider> knownMoves = new ArrayList<>();

    protected MonsterMind(MonsterSoul owner) {
        this.owner = owner;

        knownMoves.add(CommandWalk.walkCommand());
        knownMoves.add(ActionJump.JUMP_COMMAND);
    }

    /**
     * Prepares this AI to control a new entity
     * @param entityToControl the entity now controlled by this AI
     * @param game            the game instance of the entity
     */
    public void init(MonsterEntity entityToControl, Game game) {
        this.game = game;
        this.entity = entityToControl;

    }

    /**
     * returns the next action to execute at the given game time.
     * @param gameTime planned time of execution of the action.
     * @return the next action that the given entity should execute
     */
    public EntityAction getNextAction(float gameTime) {
        Pair<EntityAction, Float> action = entity.currentActions.getActionAt(gameTime);
        EntityAction previous = action.left;
        Vector3f position = previous.getPositionAt(action.right);

        if (executionTarget == null) {
            executionTarget = plan.poll();
        }

        while (executionTarget != null) {
            EntityAction next = executionTarget.getAction(game, position, gameTime, entity);

            if (next != null) {
                if (next != previous && !next.getStartPosition().equals(position)) {
                    throw new BrokenMovementException(previous, next, action.right);
                }

                return next;
            }

            executionTarget = plan.poll();
        }

        GameMap map = game.get(GameMap.class);
        Vector2i coordinate = map.getCoordinate(position);
        if (!map.isOnFloor(position)) {
            return new ActionJump(position, map.getPosition(coordinate), 4);

        } else if (map.getPosition(coordinate).distanceSquared(position) > 0.01f) {
            return new ActionWalk(game, position, coordinate);

        } else {
            return new ActionIdle(position);
        }
    }

    public abstract void update(float gametime);

    /**
     * pass a signal to this AI.
     * @param stimulus the signal to process
     */
    public abstract void accept(Stimulus stimulus);

    /**
     * Execute the given command at the end of the current plan
     * @param game the game instance
     * @param c    the command to be executed.
     */
    public void queueCommand(Game game, Command c) {
        plan.offer(c);

        // if currently nothing is executed, trigger an action update
        if (executionTarget == null) {
            float now = game.get(GameTimer.class).getGametime();
            EntityAction nextAction = getNextAction(now);
            entity.currentActions.insert(nextAction, now);
        } else {
            Logger.DEBUG.printf("Queued %s, Currently doing %s", c, executionTarget);
        }
    }

    public EntityAction reactEntityCollision(Entity other, float collisionTime) {
        GameMap map = game.get(GameMap.class);

        Vector3f thisPos = entity.getPositionAt(collisionTime);
        Vector3f otherPos = other.getPositionAt(collisionTime);

        if (map.isOnFloor(thisPos) && !map.isOnFloor(otherPos)) {
            return new ActionIdle(thisPos, 0.1f);
        }

        Vector3f otherToThis = new Vector3f(thisPos).sub(otherPos);
        executionTarget = null;

        if (map.isOnFloor(thisPos)) {
            Vector2i coordinate = map.getCoordinate(thisPos);
            Vector3f direction = map.getPosition(coordinate).sub(thisPos);
            // if current coordinate is in direction of collision
            if (direction.dot(otherToThis) < 0) {
                expandCoord(coordinate, otherToThis);
                return new ActionWalk(game, thisPos, coordinate);
            }

            return new ActionIdle(thisPos, 0.1f);
        }

        return new ActionFall(thisPos, otherToThis, 2);
    }

    /** increases the x or y coordinate in the given direction */
    private void expandCoord(Vector2i coordinate, Vector3f direction) {
        if (Math.abs(direction.x) > Math.abs(direction.y)) {
            if (direction.x > 0) {
                coordinate.x++;
            } else {
                coordinate.x--;
            }
        } else {
            if (direction.y > 0) {
                coordinate.y++;
            } else {
                coordinate.y--;
            }
        }
    }

    public List<CommandProvider> getAcceptedCommands() {
        return Collections.unmodifiableList(knownMoves);
    }
}
