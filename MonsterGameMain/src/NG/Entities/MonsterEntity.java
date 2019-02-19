package NG.Entities;

import NG.DataStructures.Generic.Pair;
import NG.Engine.Game;
import NG.Entities.Actions.ActionQueue;
import NG.Entities.Actions.Command;
import NG.Entities.Actions.EntityAction;
import NG.MonsterSoul.MonsterSoul;
import NG.Rendering.MatrixStack.SGL;
import NG.ScreenOverlay.Frames.Components.SButton;
import NG.ScreenOverlay.Frames.Components.SFrame;
import NG.ScreenOverlay.Frames.Components.SPanel;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Quaternionf;
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
    private ActionQueue currentActions;

    /** the direction this entity is facing relative to the world */
    private Pair<Vector3fc, Float> currentFace;
    /** the direction this entity is rotating to relative to the world */
    private Pair<Vector3fc, Float> targetFace;

    private final float rotationSpeedRS = 0.1f;

    private boolean isDisposed;
    private float lastActionQueryTime = -Float.MAX_VALUE;

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
    }

    @Override
    public void draw(SGL gl) {
        float now = game.timer().getRendertime();
        currentActions.removeUntil(now);

        gl.pushMatrix();
        {
            Vector3f pos = currentActions.getPositionAt(now);
            gl.translate(pos);

            gl.translate(0, 0, 2);
            Toolbox.draw3DPointer(gl); // sims
            gl.translate(0, 0, -2);

            Quaternionf rot = Vectors.getPitchYawRotation(getFaceRotation(now));
            gl.rotate(rot);

            drawDetail(gl);
        }
        gl.popMatrix();
    }

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

    /**
     * draw the entity,
     * @param gl the sgl object to render with
     */
    protected abstract void drawDetail(SGL gl);

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
        targetFace = new Pair<>(direction, gametime + angle / rotationSpeedRS);
    }

    /**
     * retrieves and removes the next action of this entity.
     * @param currentTime the current time. Must be more than any previous invocation
     * @return the action that has the most recently been started at the given time.
     */
    public EntityAction getAction(float currentTime) {
        if (lastActionQueryTime > currentTime) {
            throw new IllegalArgumentException(
                    "getAction argument time must be more than any previous invocation. " +
                            "(was " + lastActionQueryTime + " but got " + currentTime + ")"
            );
        }

        lastActionQueryTime = currentTime;
        currentActions.removeUntil(currentTime);
        return currentActions.pollFirst();
    }

    @Override
    public void onClick(int button) {
        SFrame frame = new SFrame("Entity " + this);
        WalkCommandTool walkTool = new WalkCommandTool(game, this);

        SPanel mainPanel = SPanel.column(
                new SButton("Walk to...", () -> game.inputHandling().setMouseTool(walkTool), 300, 100)
        );

        frame.setMainPanel(mainPanel);
        game.gui().addFrame(frame);
    }

    protected abstract void lookAt(Vector3fc position);

    public void execute(Command command, MonsterSoul source) {
        assert source.equals(controller);

        EntityAction action = command.toAction(game, this);
        currentActions.offer(action);
    }

    @Override
    public void dispose() {
        isDisposed = true;
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

    public EntityAction getLastQueuedAction() {
        return currentActions.peek();
    }

    private class IllegalPositionException extends IllegalArgumentException {
        public IllegalPositionException(String s) {
            super(s);
        }
    }
}
