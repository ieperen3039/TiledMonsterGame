package NG.Entities;

import NG.Actions.ActionIdle;
import NG.Actions.ActionQueue;
import NG.Actions.EntityAction;
import NG.Animations.BodyModel;
import NG.Animations.BoneElement;
import NG.Animations.SkeletonBone;
import NG.DataStructures.Generic.Pair;
import NG.Engine.Game;
import NG.Engine.GameTimer;
import NG.GUIMenu.Components.SFrame;
import NG.GUIMenu.Components.SPanel;
import NG.GUIMenu.Frames.FrameGUIManager;
import NG.GUIMenu.Menu.MainMenu;
import NG.GameMap.GameMap;
import NG.Living.MonsterSoul;
import NG.Rendering.MatrixStack.SGL;
import NG.Tools.Logger;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.Map;

/**
 * the generic class of all {@code MonsterEntity} entities. This is only the physical representation of the entity, NOT
 * including the {@link MonsterSoul} or identity of the monster.
 * @author Geert van Ieperen created on 4-2-2019.
 */
public abstract class MonsterEntity implements Entity {
    protected final Game game;
    private final MonsterSoul controller;
    /** the current actions that are executed */
    public final ActionQueue currentActions;

    private boolean isDisposed;
    private SFrame frame;
    private EntityAction previousAction; // only in rendering

    public MonsterEntity(Game game, Vector2i initialPosition, MonsterSoul controller) {
        this.game = game;
        this.controller = controller;
        this.currentActions = new ActionQueue(game, initialPosition);
        previousAction = new ActionIdle(game, initialPosition);
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

            bodyModel().draw(gl, this, getBoneMapping(), timeSinceStart, action, previousAction);
        }
        gl.popMatrix();

        if (action != previousAction) previousAction = action;
    }

    @Override
    public void update(float gameTime) {
        float lastActionEnd = currentActions.lastActionEnd();
        if (gameTime >= lastActionEnd) {
            EntityAction next = controller.getNextAction(lastActionEnd);
            currentActions.insert(next, gameTime);
        }

        controller.update(gameTime);
    }

    /**
     * @return the root bone of this entity's skeleton
     */
    protected abstract BodyModel bodyModel();

    /**
     * @return a mapping of bones from the bones of {@link #bodyModel()} to implementations.
     */
    protected abstract Map<SkeletonBone, BoneElement> getBoneMapping();

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

    @Override
    public void onClick(int button) {
        if (frame != null) frame.dispose();
        int buttonHeight = MainMenu.BUTTON_MIN_HEIGHT;

        frame = new SFrame("Entity " + this);
        frame.setMainPanel(SPanel.column(
                controller.getStatisticsPanel(buttonHeight)
        ));
        frame.pack();
        game.get(FrameGUIManager.class).addFrame(frame);
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
    public void collideWith(Object other, float collisionTime) {
        if (other instanceof GameMap) {
            Pair<EntityAction, Float> actionAt = currentActions.getActionAt(collisionTime);
            EntityAction action = actionAt.left;
            Logger.WARN.print(actionAt, action.getPositionAt(actionAt.right));

            if (!action.hasWorldCollision()) return;
        }

        EntityAction nextAction = controller.getNextAction(collisionTime);
        currentActions.insert(nextAction, collisionTime);
    }
}
