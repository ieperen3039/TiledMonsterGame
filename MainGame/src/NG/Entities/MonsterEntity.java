package NG.Entities;

import NG.Actions.*;
import NG.CollisionDetection.BoundingBox;
import NG.Core.AbstractGameObject;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Generic.Color4f;
import NG.DataStructures.Generic.Pair;
import NG.GameMap.GameMap;
import NG.Living.MonsterSoul;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Rendering.Shapes.GenericShapes;
import NG.Tools.Logger;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static NG.Actions.EntityAction.ACCEPTABLE_DIFFERENCE_SQ;

/**
 * All monsters of the game are based on this class. This is only the physical representation of the entity, NOT the
 * {@link MonsterSoul} or identity of the monster.
 * @author Geert van Ieperen created on 4-2-2019.
 */
public class MonsterEntity extends AbstractGameObject implements MovingEntity {
    /** the current actions that are executed */
    private ActionQueue currentActions;

    private MonsterSoul controller;

    private boolean isDisposed;
    private Mark marking = Mark.NONE;

    public MonsterEntity() {
    }

    public MonsterEntity(
            Game game, Vector2i initialPosition, MonsterSoul controller
    ) {
        init(game);
        this.controller = controller;
        this.currentActions = new ActionQueue(game, initialPosition);
    }

    @Override
    public void restoreFields(Game game) {
        controller.restore(game);
    }

    @Override
    public void draw(SGL gl) {
        if (isDisposed) return;

        float now = game.get(GameTimer.class).getRendertime();

        ShaderProgram shader = gl.getShader();
        MaterialShader materials = null;
        if (shader instanceof MaterialShader) {
            materials = (MaterialShader) shader;
        }

        if (materials != null) {
            for (EntityAction action : currentActions.actionsBetween(now, Float.POSITIVE_INFINITY)) {
                action.getMarker().draw(gl);
            }
        }

        Pair<EntityAction, Float> actionPair = currentActions.getActionAt(now);
        EntityAction action = actionPair.left;
        Float timeSinceStart = actionPair.right;

        gl.pushMatrix();
        {
            gl.translate(action.getPositionAt(timeSinceStart));

            if (materials != null && marking == Mark.SELECTED) {
                materials.setMaterial(Material.SILVER, Color4f.YELLOW);
                gl.translate(0, 0, 0.5f);
                gl.render(GenericShapes.SELECTION, this);
                gl.translate(0, 0, -0.5f);
            }

            gl.rotate(action.getRotationAt(timeSinceStart));

            controller.props.bodyModel.draw(gl, this, controller.props.boneMapping, timeSinceStart, action);

            if (materials != null) {
                materials.setMaterial(Material.ROUGH, Color4f.WHITE);

                switch (marking) {
                    case SELECTED:
                        materials.setMaterial(Material.SILVER, Color4f.YELLOW);
                    case OWNED:
                        gl.translate(0, 0, 4);
                        gl.scale(2, 2, -1);
                        gl.render(GenericShapes.ARROW, this);
                        break;

                    case NONE:
                    default:
                        break;
                }
            }
        }
        gl.popMatrix();

        // TODO future serialisation may want to take this responsibility
        currentActions.removeUntil(now - 1f);
    }

    @Override
    public void update(float gameTime) {
        controller.update(gameTime);
    }

    /**
     * @return the {@link NG.Living.Living} that controls this entity.
     */
    public MonsterSoul getController() {
        return controller;
    }

    @Override
    public void dispose() {
        isDisposed = true;
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

    @Override
    public Pair<EntityAction, Float> getActionAt(float gameTime) {
        return currentActions.getActionAt(gameTime);
    }

    @Override
    public void collideWith(Entity other, float collisionTime) {
        GameMap map = game.get(GameMap.class);

        Pair<EntityAction, Float> action = getActionAt(collisionTime);
        Vector3fc thisPos = action.left.getPositionAt(action.right);
        Vector3fc otherPos = other.getPositionAt(collisionTime);
        Vector3f movement = action.left.getDerivative(action.right);
        Vector3f otherToThis = new Vector3f(thisPos).sub(otherPos);

        final EntityAction nextAction;

        if (map.isOnFloor(thisPos)) {
            if (map.isOnFloor(otherPos)) { // other bumps this
                Vector2i coordinate = map.getCoordinate(thisPos);
                Vector3f coordPos = map.getPosition(coordinate);
                Vector3f thisToCoord = new Vector3f(coordPos).sub(thisPos);

                if (thisToCoord.lengthSquared() < ACCEPTABLE_DIFFERENCE_SQ) {
                    nextAction = new ActionIdle(coordPos, 0.1f);

                } else {
                    if (thisToCoord.dot(otherToThis) < 0) { // current coordinate is in direction of collision
                        GameMap.expandCoord(coordinate, otherToThis);
                    }
                    nextAction = new ActionJump(thisPos, coordPos, controller.props.jumpSpeed);
                }

            } else { // other lands on this
                nextAction = new ActionIdle(thisPos, 0.1f);
            }

        } else { // collide while in air
            nextAction = new ActionFall(thisPos, otherToThis, movement.length());
        }

        Logger.DEBUG.printf("%8.04f: %s : %s", collisionTime, this, nextAction);
        currentActions.insert(nextAction, collisionTime);
        controller.mind().reactEntityCollision(other, collisionTime);
        processActions(collisionTime);
    }

    /**
     * process a collision with the map, happening at collisionTime.
     * @param map           the map
     * @param collisionTime the moment of collision
     */
    @Override
    public void collideWith(GameMap map, float collisionTime) {
        EntityAction nextAction = controller.mind().getActionAt(collisionTime);

        if (nextAction == null) {
            Vector3f position = getPositionAt(collisionTime);
            Vector2i coordinate = map.getCoordinate(position);
            Vector3f targetPosition = map.getPosition(coordinate);

            if (!map.isOnFloor(position)) {
                nextAction = new ActionJump(position, targetPosition, 5);

            } else if (targetPosition.distanceSquared(position) > ACCEPTABLE_DIFFERENCE_SQ) {
                nextAction = new ActionWalk(game, position, coordinate);

            } else {
                nextAction = new ActionIdle(targetPosition);
            }
        }

        Logger.DEBUG.printf("%8.04f: %s : %s", collisionTime, this, nextAction);
        currentActions.insert(nextAction, collisionTime);
        processActions(collisionTime);
    }

    @Override
    public String toString() {
        return controller.toString();
    }

    public void markAs(Mark type) {
        marking = type;
    }

    public enum Mark {
        NONE, OWNED, SELECTED
    }

    @Override
    public BoundingBox getHitbox(float gameTime) {
        Pair<EntityAction, Float> action = getActionAt(gameTime);
        Quaternionf rotation = action.left.getRotationAt(action.right);
        BoundingBox hitbox = getLocalHitbox();
//        rotate hitbox
        //...
        return hitbox.getMoved(getPositionAt(gameTime));
    }

    /**
     * @return the relative (local-space) bounding box of this entity
     */
    public BoundingBox getLocalHitbox() {
        return controller.props.hitbox;
    }

    /**
     * queries the mind for actions to execute starting at gameTime until an action with infinite duration is found. If
     * the current action has infinite duration, it is cancelled.
     * @param gameTime the time to start analyzing
     */
    public void processActions(float gameTime) {
        GameMap map = game.get(GameMap.class);

        Pair<EntityAction, Float> actionPair = currentActions.getActionAt(gameTime);
        assert actionPair != null;

        float duration = actionPair.left.duration();
        if (Float.isFinite(duration) && actionPair.right < duration) {
            float timeLeft = duration - actionPair.right;
            gameTime += timeLeft;
        }

        EntityAction newAction = controller.mind().getActionAt(gameTime);
        while (newAction != null) {
            Logger.DEBUG.printf("    plan: %s : %s", this, newAction);
            currentActions.insert(newAction, gameTime);

            float actionDuration = newAction.duration();
            if (Float.isInfinite(actionDuration)) break;

            if (newAction.hasWorldCollision()) {
                Float collision = map.getActionCollision(newAction, 0, newAction.duration());
                if (collision != null) {
                    actionDuration = collision;
                }
            }

            if (actionDuration == 0) break;

            gameTime += actionDuration;
            newAction = controller.mind().getActionAt(gameTime);
        }
    }
}
