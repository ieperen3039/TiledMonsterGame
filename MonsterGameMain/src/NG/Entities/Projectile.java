package NG.Entities;

import NG.Actions.EntityAction;
import NG.Engine.Game;
import NG.Rendering.MatrixStack.SGL;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen created on 2-4-2019.
 */
public abstract class Projectile implements Entity {
    protected Game game;

    private boolean isDisposed = false;
    private float scaling;

    public Projectile(Game game, float scaling) {
        this.game = game;
        this.scaling = scaling;
    }

    @Override
    public void draw(SGL gl) {
        float now = game.timer().getRendertime();

        gl.pushMatrix();
        {
            gl.translate(getPosition(now));
            gl.rotate(getMovement().getRotationAt(now));
            gl.scale(scaling);

            drawProjectile(gl);
        }
        gl.popMatrix();
    }

    protected abstract void drawProjectile(SGL gl);

    protected abstract EntityAction getMovement();

    @Override
    public Vector3f getPosition(float currentTime) {
        return getMovement().getPositionAt(currentTime);
    }

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
}
