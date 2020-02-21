package NG.Entities;

import NG.Actions.ActionIdle;
import NG.Actions.ActionQueue;
import NG.Actions.EntityAction;
import NG.Animations.BodyModel;
import NG.Animations.BoneElement;
import NG.Animations.SkeletonBone;
import NG.CollisionDetection.BoundingBox;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Generic.Color4f;
import NG.DataStructures.Generic.Pair;
import NG.GameMap.GameMap;
import NG.Living.MonsterMind.MonsterMind;
import NG.Living.MonsterSoul;
import NG.Particles.GameParticles;
import NG.Particles.Particles;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Rendering.Shapes.GenericShapes;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.Map;

/**
 * the generic class of all {@code MonsterEntity} entities. This is only the physical representation of the entity, NOT
 * including the {@link MonsterSoul} or identity of the monster.
 * @author Geert van Ieperen created on 4-2-2019.
 */
public abstract class MonsterEntity implements MovingEntity {
    protected final Game game;
    /** the current actions that are executed */
    public final ActionQueue currentActions;

    private final MonsterSoul controller;
    private final BodyModel bodyModel;
    private final Map<SkeletonBone, BoneElement> boneMapping;

    private boolean isDisposed;
    private EntityAction previousAction; // only in rendering
    private Mark marking = Mark.NONE;

    public MonsterEntity(
            Game game, Vector2i initialPosition, MonsterSoul controller,
            BodyModel bodyModel, Map<SkeletonBone, BoneElement> boneMapping
    ) {
        this.game = game;
        this.controller = controller;
        this.currentActions = new ActionQueue(game, initialPosition);
        this.previousAction = new ActionIdle(game, initialPosition);
        this.bodyModel = bodyModel;
        this.boneMapping = boneMapping;
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

            bodyModel.draw(gl, this, boneMapping, timeSinceStart, action, previousAction);

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

    public void eventDeath(float time) {
        this.dispose();
        game.get(GameParticles.class).add(
                Particles.explosion(getPositionAt(time), Color4f.RED, 10)
        );
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
    abstract BoundingBox getLocalHitbox();
}
