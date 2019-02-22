package NG.Entities;

import NG.DataStructures.Generic.Pair;
import NG.Engine.Game;
import NG.Entities.Actions.ActionQueue;
import NG.Entities.Actions.EntityAction;
import NG.MonsterSoul.MonsterSoul;
import NG.Rendering.MatrixStack.SGL;
import NG.ScreenOverlay.Frames.Components.SFrame;
import NG.ScreenOverlay.Frames.Components.SPanel;
import NG.ScreenOverlay.Frames.Components.SToggleButton;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * the generic class of all {@code MonsterEntity} entities. This is only the physical representation of the entity, NOT
 * including the {@link MonsterSoul} or identity of the monster.
 * @author Geert van Ieperen created on 4-2-2019.
 */
public abstract class MonsterEntity implements Entity {
    private final Game game;
    private final MonsterSoul controller;
    /** the current action that is executed */
    public ActionQueue currentActions;

    /** the direction this entity is facing relative to the world */
    private Pair<Vector3fc, Float> currentFace;
    /** the direction this entity is rotating to relative to the world */
    private Pair<Vector3fc, Float> targetFace;

    private boolean isDisposed;
    private SFrame frame;
    private final WalkCommandTool walkTool;

    public MonsterEntity(
            Game game, Vector2i initialPosition, Vector3fc faceDirection, MonsterSoul controller
    ) {
        this.game = game;
        this.controller = controller;
        this.currentActions = new ActionQueue(game, initialPosition);

        float gametime = game.timer().getGametime();

        Vector3f eyeDir = new Vector3f(faceDirection);
        this.currentFace = new Pair<>(eyeDir, gametime);
        this.targetFace = currentFace;

        boolean hasClaim = game.claims().createClaim(initialPosition, this);

        if (!hasClaim) {
            throw new IllegalPositionException("given coordinate " + Vectors.toString(initialPosition) + " is not free");
        }
        walkTool = new WalkCommandTool(this.game, this);
    }

    @Override
    public void draw(SGL gl) {
        if (isDisposed) return;

        float now = game.timer().getRendertime();
        currentActions.removeUntil(now);
        Pair<EntityAction, Float> action = currentActions.getActionAt(now);

        gl.pushMatrix();
        {
            Vector3fc pos = action.left.getPositionAfter(action.right);
            gl.translate(pos);

            gl.translate(0, 0, 2);
            Toolbox.draw3DPointer(gl); // sims
            gl.translate(0, 0, -2);

            drawDetail(gl, action.left, action.right / action.left.duration());
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

    /**
     * Returns the rotation of this entity on the given moment. This is influenced by {@link
     * #setTargetRotation(Vector3fc)}
     * @param currentTime the time of measurement.
     * @return the rotation at the given moment
     */
    private Vector3f getFaceRotation(float currentTime) {
        if (currentTime > targetFace.right) {
            return new Vector3f(targetFace.left);
        }

        float base = currentFace.right;
        float range = targetFace.right - base;
        float fraction = (currentTime - base) / range;

        Vector3fc current = currentFace.left;
        Vector3fc target = targetFace.left;

        return new Vector3f(current).lerp(target, fraction);
    }

    public MonsterSoul getController() {
        return controller;
    }

    @Override
    public Vector3fc getPosition() {
        float currentTime = game.timer().getGametime();
        return currentActions.getPositionAt(currentTime);
    }

    protected void setTargetRotation(Vector3fc direction) {
        float gametime = game.timer().getGametime();
        Vector3fc curDir = getFaceRotation(gametime);
        float angle = curDir.angle(direction);

        currentFace = new Pair<>(getFaceRotation(gametime), gametime);
        float rotationSpeedRS = 0.1f;
        targetFace = new Pair<>(direction, gametime + angle / rotationSpeedRS);
    }

    @Override
    public void onClick(int button) {
        if (frame != null) frame.dispose();

        frame = new SFrame("Entity " + this);
        frame.setMainPanel(SPanel.column(
                new SToggleButton("Walk to...", 300, 80, (s) -> game.inputHandling().setMouseTool(s ? walkTool : null))
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

    private class IllegalPositionException extends IllegalArgumentException {
        public IllegalPositionException(String s) {
            super(s);
        }
    }
}
