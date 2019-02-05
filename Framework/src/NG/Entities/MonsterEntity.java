package NG.Entities;

import NG.DataStructures.Direction;
import NG.DataStructures.Interpolation.VectorInterpolator;
import NG.Engine.Game;
import NG.GameMap.GameMap;
import NG.MonsterSoul.Living;
import NG.MonsterSoul.MonsterSoul;
import NG.MonsterSoul.Stimulus;
import NG.Rendering.MatrixStack.SGL;
import NG.Settings.Settings;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static NG.Settings.Settings.TILE_SIZE;

/**
 * the generic class of all {@code MonsterEntity} entities. This is only the physical representation of the entity, NOT
 * including the {@link MonsterSoul} or identity of the monster.
 * @author Geert van Ieperen created on 4-2-2019.
 */
public abstract class MonsterEntity implements Entity, Living {
    private final Game game;
    private final MonsterSoul controller;

    /** current real position of this entity */
    private final Vector3f position = new Vector3f();

    /** the coordinate this entity is moving to */
    private final Vector2i targetCoordinate = new Vector2i();
    /** the coordinate this entity is leaving, or equal to {@code targetCoordinate} */
    private final Vector2i claimedCoordinate = new Vector2i();

    /** the direction this entity is facing relative to the world */
    private final Vector3f faceDirection = new Vector3f();
    /** the direction this entity is rotating to relative to the world */
    private final Vector3f targetFace = new Vector3f();

    // todo wrap this into an Stats object
    private final float movementSpeedMS = TILE_SIZE / 1f; // = s/t
    private final float rotationSpeedRS = 0.1f;
    private final VectorInterpolator positionInterpolator;
    private final VectorInterpolator rotationInterpolator;

    private boolean isDisposed;

    public MonsterEntity(
            Game game, Vector2i initialPosition, Vector3fc faceDirection, MonsterSoul controller
    ) {
        this.game = game;
        this.controller = controller;
        Settings s = game.settings();

        float gametime = game.timer().getGametime();
        Vector3f realPosition = game.map().getPosition(initialPosition);
        int capacity = (s.TARGET_FPS / s.TARGET_TPS) + 1;

        this.position.set(realPosition);
        this.positionInterpolator = new VectorInterpolator(capacity, realPosition, gametime);
        this.rotationInterpolator = new VectorInterpolator(capacity, faceDirection, gametime);
        this.targetCoordinate.set(initialPosition);

        boolean hasClaim = game.map().createClaim(initialPosition, this);
        if (!hasClaim) {
            throw new IllegalPositionException("given coordinate " + Vectors.toString(initialPosition) + " is not free");
        }
        this.claimedCoordinate.set(initialPosition);
    }

    @Override
    public void draw(SGL gl) {
        float now = game.timer().getRendertime();
        positionInterpolator.updateTime(now);
        rotationInterpolator.updateTime(now);

        gl.pushMatrix();
        {
            Vector3fc pos = positionInterpolator.getInterpolated(now);
            gl.translate(pos);

            gl.translate(0, 0, 2);
            Toolbox.draw3DPointer(gl); // sims
            gl.translate(0, 0, -2);

            Quaternionf rot = Vectors.getPitchYawRotation(targetFace);
            gl.rotate(rot);

            drawDetail(gl);
        }
        gl.popMatrix();
    }

    @Override
    public void update() {
        if (isDisposed()) return;

        float gameTime = game.timer().getGametime();
        float deltaTime = game.timer().getGametimeDifference();
        GameMap map = game.map();
        assert deltaTime > 0;

        controller.update();

        // the position this entity wants to go to
        Vector3f targetPosition = map.getPosition(targetCoordinate);
        boolean onPosition = targetPosition.equals(position);

        // if on the right place, update target with target direction
        if (onPosition) {
            Direction moveDir = controller.targetDirection();
            if (moveDir != Direction.NONE) {

                Vector2i move = moveDir.toVector();
                targetFace.set(move.x, move.y, targetFace.z); // TODO this should be different
                targetCoordinate.add(move);

                // update target
                boolean hasClaim = map.createClaim(targetCoordinate, this);
                if (hasClaim) {
                    Vector3f newPosition = map.getPosition(targetCoordinate);
                    targetPosition.set(newPosition);
                    onPosition = false;
                }
            }
        }

        // if not on the right place, update position
        if (!onPosition) {
            Vector3f vecToTarget = new Vector3f(targetPosition).sub(position);
            assert vecToTarget.lengthSquared() > 0;

            // todo energy usage and activity
            float travelDistance = movementSpeedMS * deltaTime;
            boolean isOneStep = vecToTarget.length() <= travelDistance;

            if (isOneStep) {
                position.set(targetPosition);
                boolean success = game.map().dropClaim(claimedCoordinate, this);
                assert success : this + " could not drop claim on " + Vectors.toString(claimedCoordinate);
                claimedCoordinate.set(targetCoordinate);

            } else {
                vecToTarget = vecToTarget.normalize(travelDistance);
                Vector3f result = vecToTarget.add(position);
                position.set(result.x, result.y, map.getHeightAt(result.x, result.y));
            }
        }

        // if not in the right orientation, update rotation.
        if (!faceDirection.equals(targetFace)) {
            float angle = faceDirection.angle(targetFace);
            float travelRotation = rotationSpeedRS * deltaTime;

            if (angle < travelRotation) {
                faceDirection.set(targetFace);

            } else {
                Vector3f cross = faceDirection.cross(targetFace);
                if (!Vectors.isScalable(cross)) cross = Vectors.newZVector();

                faceDirection.rotateAxis(travelRotation, cross.x, cross.y, cross.z);
            }
        }
//        Logger.ASSERT.print(gameTime, position);

        positionInterpolator.add(new Vector3f(position), gameTime);
        rotationInterpolator.add(new Vector3f(faceDirection), gameTime);
    }

    @Override
    public void command(Command command) {
        controller.command(command);
    }

    @Override
    public void accept(Stimulus stimulus) {
        controller.accept(stimulus);
    }

    /**
     * draw the entity,
     * @param gl the sgl object to render with
     */
    protected abstract void drawDetail(SGL gl);

    @Override
    public Vector3fc getPosition() {
        return position;
    }

    protected void setTargetRotation(Vector3fc direction) {
        targetFace.set(direction);
    }

    @Override
    public void onClick(int button) {
        // open entity interface
    }

    public abstract void lookAt(Vector3fc position);

    @Override
    public void dispose() {
        isDisposed = true;
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

    private class IllegalPositionException extends IllegalArgumentException {
        public IllegalPositionException(String s) {
            super(s);
        }
    }
}
