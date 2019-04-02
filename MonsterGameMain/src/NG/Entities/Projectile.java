package NG.Entities;

import NG.Engine.Game;
import NG.GameEvent.Actions.EntityAction;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen created on 2-4-2019.
 */
public abstract class Projectile implements Entity {
    protected Game game;

    private EntityAction movement;
    private boolean isDisposed = false;

    public Projectile(EntityAction movement, Game game) {
        this.movement = movement;
        this.game = game;
    }

    @Override
    public void draw(SGL gl) {
        float now = game.timer().getRendertime();

        gl.pushMatrix();
        {
            gl.translate(movement.getPositionAt(now));
            gl.rotate(movement.getRotationAt(now));

            gl.render(getModel(), this);
        }
        gl.popMatrix();
    }

    abstract Mesh getModel();

    @Override
    public Vector3f getPosition(float currentTime) {
        return movement.getPositionAt(currentTime);
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
