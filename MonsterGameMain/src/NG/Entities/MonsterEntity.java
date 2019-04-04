package NG.Entities;

import NG.Actions.ActionIdle;
import NG.Actions.ActionQueue;
import NG.Actions.EntityAction;
import NG.Animations.AnimationBone;
import NG.Animations.BodyModel;
import NG.Animations.BoneElement;
import NG.DataStructures.Generic.Pair;
import NG.Engine.Game;
import NG.Engine.GameTimer;
import NG.GUIMenu.Frames.Components.SFrame;
import NG.GUIMenu.Frames.Components.SPanel;
import NG.GUIMenu.Frames.Components.SToggleButton;
import NG.GUIMenu.Frames.GUIManager;
import NG.GUIMenu.Menu.MainMenu;
import NG.GameMap.ClaimRegistry;
import NG.GameMap.GameMap;
import NG.InputHandling.KeyMouseCallbacks;
import NG.MonsterSoul.Commands.WalkCommandTool;
import NG.MonsterSoul.MonsterSoul;
import NG.Rendering.MatrixStack.SGL;
import NG.Tools.Vectors;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

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
    public ActionQueue currentActions;

    private boolean isDisposed;
    private SFrame frame;
    private final WalkCommandTool walkTool;
    private EntityAction previousAction; // only in rendering

    public MonsterEntity(
            Game game, Vector2i initialPosition, MonsterSoul controller
    ) {
        this.game = game;
        this.controller = controller;
        this.currentActions = new ActionQueue(game, initialPosition);

        boolean hasClaim = game.get(ClaimRegistry.class).createClaim(initialPosition, this);

        if (!hasClaim) {
            throw new IllegalPositionException("given coordinate " + Vectors.toString(initialPosition) + " is not free");
        }
        walkTool = new WalkCommandTool(game, this);
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

    /**
     * @return the root bone of this entity's skeleton
     */
    protected abstract BodyModel bodyModel();

    /**
     * @return a mapping of bones from the bones of {@link #bodyModel()} to implementations.
     */
    protected abstract Map<AnimationBone, BoneElement> getBoneMapping();

    /**
     * @return the {@link NG.MonsterSoul.Living} that controls this entity.
     */
    public MonsterSoul getController() {
        return controller;
    }

    @Override
    public Vector3f getPosition(float currentTime) {
        return currentActions.getPositionAt(currentTime);
    }

    @Override
    public void onClick(int button) {
        if (frame != null) frame.dispose();
        int buttonHeight = MainMenu.BUTTON_MIN_HEIGHT;

        frame = new SFrame("Entity " + this);
        frame.setMainPanel(SPanel.column(
                controller.getStatisticsPanel(buttonHeight),
                SPanel.column(
                        new SToggleButton("Walk to...", 400, buttonHeight, (s) -> game.get(KeyMouseCallbacks.class)
                                .setMouseTool(s ? walkTool : null))
                )
        ));
        game.get(GUIManager.class).addFrame(frame);
    }

    protected abstract void lookAt(Vector3fc position);

    @Override
    public void dispose() {
        isDisposed = true;
        frame.dispose();
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

    public EntityAction getLastAction() {
        return currentActions.peekLast();
    }

    public void addAction(EntityAction action, float gameTime) {
        currentActions.addAfter(gameTime, action);
        Vector2ic coord = action.getEndCoordinate();
        Vector3f position = game.get(GameMap.class).getPosition(coord);
        lookAt(position);
    }

    private class IllegalPositionException extends IllegalArgumentException {
        public IllegalPositionException(String s) {
            super(s);
        }
    }
}
