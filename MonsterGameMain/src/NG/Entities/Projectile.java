package NG.Entities;

import NG.CollisionDetection.BoundingBox;
import NG.Engine.Game;
import NG.Engine.GameTimer;
import NG.Rendering.MatrixStack.SGL;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 2-4-2019.
 */
public abstract class Projectile implements Entity {
    protected final Game game;
    private final float scaling;
    private float spawnTime = Float.MAX_VALUE;

    private boolean isDisposed = false;
    private boolean isLaunched = false;

    public Projectile(Game game, float scaling) {
        this.game = game;
        this.scaling = scaling;
    }

    /**
     * Sets the position of this projectile to startPosition and make it move to endPosition
     * @param startPosition a position in the world, where this projectile spawns at the current game time
     * @param spawnTime     moment of launching, should be equal to the current game time
     */
    public void launch(Vector3fc startPosition, float spawnTime) {
        this.spawnTime = spawnTime;
        setSpawnPosition(startPosition);
        isLaunched = true;
    }

    /**
     * sets the start position of the projectile. No call to {@link #getPositionAt(float)} may happen before this method
     * is called
     * @param spawnPosition the position of spawning
     */
    protected abstract void setSpawnPosition(Vector3fc spawnPosition);

    public boolean isLaunched() {
        return isLaunched;
    }

    @Override
    public void draw(SGL gl) {
        if (isDisposed) return;
        float now = game.get(GameTimer.class).getRendertime();
        if (now < spawnTime) return;
        float timeSinceStart = now - spawnTime;

        gl.pushMatrix();
        {
            gl.translate(getPositionAt(timeSinceStart));
            gl.scale(scaling);

            drawProjectile(gl, timeSinceStart);
        }
        gl.popMatrix();
    }

    /**
     * @param gl         draw the projectile, without additional positioning
     * @param renderTime the current rendering time
     */
    protected abstract void drawProjectile(SGL gl, float renderTime);

    @Override
    public void onClick(int button) {

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
    public BoundingBox hitbox() {
        return new BoundingBox(0, 0, 0, 0, 0, 0);
    }
}
