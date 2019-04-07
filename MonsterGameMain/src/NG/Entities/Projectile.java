package NG.Entities;

import NG.Engine.Game;
import NG.Engine.GameTimer;
import NG.Rendering.MatrixStack.SGL;

/**
 * @author Geert van Ieperen created on 2-4-2019.
 */
public abstract class Projectile implements Entity {
    protected Game game;

    private boolean isDisposed = false;
    private final float scaling;

    public Projectile(Game game, float scaling) {
        this.game = game;
        this.scaling = scaling;
    }

    @Override
    public void draw(SGL gl) {
        float now = game.get(GameTimer.class).getRendertime();
        if (isDisposed) return;
        if (now < getSpawnTime()) return;

        gl.pushMatrix();
        {
            gl.translate(getPositionAt(now));
            gl.scale(scaling);

            drawProjectile(gl, now);
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

    /**
     * @return the timestamp at which this projectile comes into existence
     */
    public abstract float getSpawnTime();
}
