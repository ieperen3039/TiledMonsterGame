package NG.Living.MonsterMind;

import NG.Actions.ActionJump;
import NG.Actions.ActionWalk;
import NG.Actions.BrokenMovementException;
import NG.Actions.Commands.Command;
import NG.Actions.EntityAction;
import NG.Core.AbstractGameObject;
import NG.Core.Game;
import NG.DataStructures.Generic.Pair;
import NG.Entities.Entity;
import NG.Entities.MonsterEntity;
import NG.InputHandling.MouseTools.CommandProvider;
import NG.Living.MonsterSoul;
import NG.Living.Stimulus;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An AI for monsters, which selects what moves are executed. The base implementation just follows exactly and only the
 * commands given.
 * @author Geert van Ieperen created on 5-2-2020.
 */
public abstract class MonsterMind extends AbstractGameObject {
    protected Command executionTarget;

    protected final MonsterSoul owner;
    protected MonsterEntity entity;
    protected List<CommandProvider> knownMoves = new ArrayList<>();

    protected MonsterMind(MonsterSoul owner) {
        super(null);
        this.owner = owner;

        knownMoves.add(ActionWalk.WALK_COMMAND);
        knownMoves.add(ActionJump.JUMP_COMMAND);
        knownMoves.addAll(owner.props.moves);
    }

    /**
     * Prepares this AI to control a new entity
     * @param entityToControl the entity now controlled by this AI
     * @param game
     */
    public void setEntity(MonsterEntity entityToControl, Game game) {
        this.game = game;
        this.entity = entityToControl;
    }

    @Override
    public void restoreFields(Game game) {
        entity.restore(game);
        owner.restore(game);
    }

    /**
     * returns the next action to execute at the given game time.
     * @param gameTime planned time of execution of the action.
     * @return the next action that the given entity should execute, or null if no action is required.
     */
    public EntityAction getActionAt(float gameTime) {
        if (executionTarget == null) {
            return null;
        }

        // this is executing a command
        Pair<EntityAction, Float> action = entity.getActionAt(gameTime);
        EntityAction previous = action.left;
        Vector3f position = previous.getPositionAt(action.right);

        EntityAction next = executionTarget.getAction(game, entity, position, gameTime);

        if (next != null && next != previous && !next.getStartPosition().equals(position)) {
            throw new BrokenMovementException(previous, next, action.right);
        }

        return next;
    }

    public abstract void update(float gametime);

    /**
     * pass a signal to this AI.
     * @param stimulus the signal to process
     */
    public abstract void accept(Stimulus stimulus);

    /**
     * notify this mind that a physical collision has overridden the current action queue.
     * @param other         the entity causing the collision
     * @param collisionTime the time of collision
     */
    public abstract void reactEntityCollision(Entity other, float collisionTime);

    /**
     * Execute the given command at the end of the current plan
     * @param c    the command to be executed.
     * @param gameTime
     */
    public void executeCommand(Command c, float gameTime) {
        executionTarget = c;
        entity.processActions(gameTime);
    }

    public List<CommandProvider> getAcceptedCommands() {
        return Collections.unmodifiableList(knownMoves);
    }
}
