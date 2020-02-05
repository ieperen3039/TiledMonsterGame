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
import NG.GUIMenu.Components.SFrame;
import NG.GUIMenu.Components.SPanel;
import NG.GUIMenu.Frames.FrameGUIManager;
import NG.GameMap.GameMap;
import NG.Living.MonsterSoul;
import NG.Particles.GameParticles;
import NG.Particles.Particles;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Rendering.Shapes.GenericShapes;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Map;

import static NG.GUIMenu.Components.SButton.BUTTON_MIN_HEIGHT;

/**
 * the generic class of all {@code MonsterEntity} entities. This is only the physical representation of the entity, NOT
 * including the {@link MonsterSoul} or identity of the monster.
 * @author Geert van Ieperen created on 4-2-2019.
 */
public abstract class MonsterEntity implements MovingEntity {
    protected final Game game;
    /** the current actions that are executed */
    public final ActionQueue currentActions;
    public final MonsterSoul controller;

    private final BodyModel bodyModel;
    private final Map<SkeletonBone, BoneElement> boneMapping;

    private boolean isDisposed;
    private SFrame frame;
    private EntityAction previousAction; // only in rendering

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

        Pair<EntityAction, Float> actionPair = currentActions.getActionAt(now);
        EntityAction action = actionPair.left;
        Float timeSinceStart = actionPair.right;

        gl.pushMatrix();
        {
            gl.translate(action.getPositionAt(timeSinceStart));
            gl.rotate(action.getRotationAt(timeSinceStart));

            gl.pushMatrix();
            {
                bodyModel.draw(gl, this, boneMapping, timeSinceStart, action, previousAction);
            }
            gl.popMatrix();

            ShaderProgram shader = gl.getShader();
            if (shader instanceof MaterialShader) {
                gl.translate(0, 0, getHitbox().maxZ + 1);
                gl.scale(1, 1, -1);
                gl.render(GenericShapes.ARROW, this);
            }
        }
        gl.popMatrix();


        if (action != previousAction) previousAction = action;
    }

    @Override
    public void update(float gameTime) {
        float lastActionEnd = currentActions.lastActionEnd();
        if (gameTime >= lastActionEnd) {
            EntityAction next = controller.getNextAction(lastActionEnd);
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
    public Vector3f getPositionAt(float currentTime) {
        return currentActions.getPositionAt(currentTime);
    }

    public void onClick(int button) {
        if (frame != null) frame.dispose();

        frame = new SFrame("Entity " + this);
        frame.setMainPanel(SPanel.column(
                controller.getStatisticsPanel(BUTTON_MIN_HEIGHT)
        ));
        frame.pack();
        game.get(FrameGUIManager.class).addFrame(frame);
    }

    @Override
    public float getIntersection(Vector3fc origin, Vector3fc direction, float gameTime) {
        return new BoundingBox(getHitbox(), getPositionAt(gameTime)).intersectRay(origin, direction);
    }

    @Override
    public void dispose() {
        isDisposed = true;
        if (frame != null) frame.dispose();
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

    public EntityAction getLastAction() {
        return currentActions.lastAction();
    }

    public EntityAction getActionAt(float gameTime) {
        return currentActions.getActionAt(gameTime).left;
    }

    @Override
    public void collideWith(Entity other, float collisionTime) {
        EntityAction nextAction = controller.getNextAction(collisionTime);
        currentActions.insert(nextAction, collisionTime);
    }

    @Override
    public void collideWith(GameMap map, float collisionTime) {
        EntityAction action = currentActions.getActionAt(collisionTime).left;
        if (!action.hasWorldCollision()) return;

        EntityAction nextAction = controller.getNextAction(collisionTime);
        currentActions.insert(nextAction, collisionTime);
    }

    public void eventDeath(float time) {
        this.dispose();
        game.get(GameParticles.class).add(
                Particles.explosion(getPositionAt(time), Color4f.RED, 10)
        );
    }
}
