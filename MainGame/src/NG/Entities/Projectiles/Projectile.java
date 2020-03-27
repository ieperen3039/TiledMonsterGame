package NG.Entities.Projectiles;

import NG.Actions.EntityAction;
import NG.Core.AbstractGameObject;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Generic.Pair;
import NG.Entities.Entity;
import NG.Entities.MovingEntity;
import NG.Rendering.MatrixStack.SGL;

import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 2-4-2019.
 */
public abstract class Projectile extends AbstractGameObject implements MovingEntity {
    protected Entity source;
    protected EntityAction movement;
    protected float spawnTime;
    protected float despawnTime;
    private boolean isVerified = false;
    private Supplier<Boolean> validator;

    public Projectile(
            Game game, MovingEntity source, float spawnTime, float despawnTime, EntityAction movement,
            EntityAction sourceAction
    ) {
        this(game, source, spawnTime, despawnTime, movement, () -> sourceAction.equals(source.getActionAt(spawnTime).left));
    }

    public Projectile(
            Game game, Entity source, float spawnTime, float despawnTime, EntityAction movement,
            Supplier<Boolean> validator
    ) {
        super(game);
        assert despawnTime > spawnTime;
        this.source = source;
        this.spawnTime = spawnTime;
        this.despawnTime = despawnTime;
        this.movement = movement;
        this.validator = validator;
    }

    @Override
    public void update(float gameTime) {
        if (!isVerified && gameTime >= spawnTime) {
            if (validator.get()) {
                isVerified = true;
            }
        }
    }

    @Override
    public void draw(SGL gl) {
        float now = game.get(GameTimer.class).getRendertime();
        if (isVerified && now >= spawnTime && now <= despawnTime) return;

        gl.pushMatrix();
        {
            gl.translate(movement.getPositionAt(now));
            gl.rotate(movement.getRotationAt(now));
            drawProjectile(gl, now);
        }
        gl.popMatrix();

        movement.getMarker().draw(gl);
    }

    @Override
    public Pair<EntityAction, Float> getActionAt(float gameTime) {
        return new Pair<>(movement, gameTime - spawnTime);
    }

    @Override
    protected void restoreFields(Game game) {
        source.restore(game);
        movement.restore(game);
    }

    /**
     * @param gl         draw the projectile, without additional positioning
     * @param renderTime the current rendering time
     */
    protected abstract void drawProjectile(SGL gl, float renderTime);

    @Override
    public boolean canCollideWith(Entity other) {
        return other != this && other != source;
    }

    @Override
    public float getSpawnTime() {
        return spawnTime;
    }

    @Override
    public float getDespawnTime() {
        return despawnTime;
    }
}
