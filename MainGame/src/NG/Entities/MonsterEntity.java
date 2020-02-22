package NG.Entities;

import NG.Actions.ActionIdle;
import NG.Actions.ActionQueue;
import NG.Actions.EntityAction;
import NG.CollisionDetection.BoundingBox;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Generic.Color4f;
import NG.DataStructures.Generic.Pair;
import NG.GameMap.GameMap;
import NG.Living.MonsterMind.MonsterMind;
import NG.Living.MonsterSoul;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Rendering.Shapes.GenericShapes;
import org.joml.Vector2i;
import org.joml.Vector3f;

/**
 * the generic class of all {@code MonsterEntity} entities. This is only the physical representation of the entity, NOT
 * including the {@link MonsterSoul} or identity of the monster.
 * @author Geert van Ieperen created on 4-2-2019.
 */
public class MonsterEntity implements MovingEntity {
    protected final Game game;
    /** the current actions that are executed */
    public final ActionQueue currentActions;

    private final MonsterSoul controller;

    private boolean isDisposed;
    private EntityAction previousAction; // only in rendering
    private Mark marking = Mark.NONE;

    public MonsterEntity(
            Game game, Vector2i initialPosition, MonsterSoul controller
    ) {
        this.game = game;
        this.controller = controller;
        this.currentActions = new ActionQueue(game, initialPosition);
        this.previousAction = new ActionIdle(game, initialPosition);
    }

    @Override
    public void draw(SGL gl) {
        if (isDisposed) return;

        float now = game.get(GameTimer.class).getRendertime();
        currentActions.removeUntil(now);

        ShaderProgram shader = gl.getShader();
        Pair<EntityAction, Float> actionPair = currentActions.getActionAt(now);
        EntityAction action = actionPair.left;
        Float timeSinceStart = actionPair.right;

        gl.pushMatrix();
        {
            gl.translate(action.getPositionAt(timeSinceStart));

            if (shader instanceof MaterialShader && marking == Mark.SELECTED) {
                MaterialShader mat = (MaterialShader) shader;
                mat.setMaterial(Material.SILVER, Color4f.YELLOW);
                gl.translate(0, 0, 0.5f);
                gl.render(GenericShapes.SELECTION, this);
                gl.translate(0, 0, -0.5f);
            }

            gl.rotate(action.getRotationAt(timeSinceStart));

            controller.props.bodyModel.draw(gl, this, controller.props.boneMapping, timeSinceStart, action, previousAction);

            if (shader instanceof MaterialShader) {
                MaterialShader mat = (MaterialShader) shader;
                mat.setMaterial(Material.ROUGH, Color4f.WHITE);

                switch (marking) {
                    case SELECTED:
                        mat.setMaterial(Material.SILVER, Color4f.YELLOW);
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


        if (action != previousAction) previousAction = action;
    }

    @Override
    public void update(float gameTime) {
        float lastActionEnd = currentActions.lastActionEnd();
        if (gameTime >= lastActionEnd) {
            EntityAction next = controller.mind().getNextAction(lastActionEnd);
            currentActions.insert(next, lastActionEnd);
        }

        controller.update(gameTime);
    }

    /**
     * @return the {@link NG.Living.Living} that controls this entity.
     */
    public MonsterSoul getController() {
        return controller;
    }

    @Override
    public Vector3f getPositionAt(float gameTime) {
        return currentActions.getPositionAt(gameTime);
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
        MonsterMind mind = controller.mind();
        EntityAction nextAction = mind.reactEntityCollision(other, collisionTime);
        currentActions.insert(nextAction, collisionTime);
    }

    @Override
    public void collideWith(GameMap map, float collisionTime) {
        EntityAction nextAction = controller.mind().getNextAction(collisionTime);
        currentActions.insert(nextAction, collisionTime);
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
//        Pair<EntityAction, Float> action = getActionAt(gameTime);
//        Quaternionf rotation = action.left.getRotationAt(action.right);
        BoundingBox hitbox = getLocalHitbox();
//        rotate hitbox
        return hitbox.getMoved(getPositionAt(gameTime));
    }

    /**
     * @return the relative (local-space) bounding box of this entity
     */
    public BoundingBox getLocalHitbox() {
        return controller.props.hitbox;
    }
}
