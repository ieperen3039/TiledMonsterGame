package NG.Entities;

import NG.DataStructures.Generic.Pair;
import NG.Engine.Game;
import NG.GameEvent.Actions.ActionQueue;
import NG.GameEvent.Actions.EntityAction;
import NG.MonsterSoul.MonsterSoul;
import NG.Rendering.MatrixStack.SGL;
import NG.ScreenOverlay.Frames.Components.SFrame;
import NG.ScreenOverlay.Frames.Components.SPanel;
import NG.ScreenOverlay.Frames.Components.SToggleButton;
import NG.ScreenOverlay.Menu.MainMenu;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * the generic class of all {@code MonsterEntity} entities. This is only the physical representation of the entity, NOT
 * including the {@link MonsterSoul} or identity of the monster.
 * @author Geert van Ieperen created on 4-2-2019.
 */
public abstract class MonsterEntity implements Entity {
    protected final Game game;
    private final MonsterSoul controller;
    /** the current action that is executed */
    public ActionQueue currentActions;

    private boolean isDisposed;
    private SFrame frame;
    private final WalkCommandTool walkTool;

    public MonsterEntity(
            Game game, Vector2i initialPosition, MonsterSoul controller
    ) {
        this.game = game;
        this.controller = controller;
        this.currentActions = new ActionQueue(game, initialPosition);

        boolean hasClaim = game.claims().createClaim(initialPosition, this);

        if (!hasClaim) {
            throw new IllegalPositionException("given coordinate " + Vectors.toString(initialPosition) + " is not free");
        }
        walkTool = new WalkCommandTool(game, this);
    }

    @Override
    public void draw(SGL gl) {
        if (isDisposed) return;

        float now = game.timer().getRendertime();
        currentActions.removeUntil(now);
        Pair<EntityAction, Float> action = currentActions.getActionAt(now);
        EntityAction theAction = action.left;

        gl.pushMatrix();
        {
            Vector3fc pos = theAction.getPositionAfter(action.right);
            gl.translate(pos);

            gl.translate(0, 0, 2);
            Toolbox.draw3DPointer(gl); // sims
            gl.translate(0, 0, -2);

            float progress = action.right / theAction.duration();
            drawDetail(gl, theAction, progress);
        }
        gl.popMatrix();
    }

    /**
     * draw the entity,
     * @param gl             the sgl object to render with
     * @param action         the action being executed
     * @param actionProgress the progress of this action as a fraction in [0, 1]
     */
    protected abstract void drawDetail(SGL gl, EntityAction action, float actionProgress);

    public MonsterSoul getController() {
        return controller;
    }

    @Override
    public Vector3fc getPosition() {
        float currentTime = game.timer().getGametime();
        return currentActions.getPositionAt(currentTime);
    }

    protected abstract void setTargetRotation(Vector3fc direction);

    @Override
    public void onClick(int button) {
        if (frame != null) frame.dispose();
        int buttonHeight = MainMenu.BUTTON_MIN_HEIGHT;

        frame = new SFrame("Entity " + this);
        frame.setMainPanel(SPanel.column(
                controller.getStatisticsPanel(buttonHeight),
                SPanel.column(
                        new SToggleButton("Walk to...", 400, buttonHeight, (s) -> game.inputHandling()
                                .setMouseTool(s ? walkTool : null))
                )
        ));
        game.gui().addFrame(frame);
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
        Vector2ic coord = action.getEndPosition();
        Vector3f position = game.map().getPosition(coord);
        lookAt(position);
    }

    private class IllegalPositionException extends IllegalArgumentException {
        public IllegalPositionException(String s) {
            super(s);
        }
    }
}
