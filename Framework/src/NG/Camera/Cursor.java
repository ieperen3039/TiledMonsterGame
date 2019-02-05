package NG.Camera;

import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import NG.Tools.Toolbox;
import org.joml.AABBf;
import org.joml.Vector3fc;

import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 5-2-2019.
 */
public class Cursor implements Entity {
    private final Supplier<Vector3fc> positionSupplier;
    private boolean isDisposed = false;

    public Cursor(Supplier<Vector3fc> positionSupplier) {
        this.positionSupplier = positionSupplier;
    }

    @Override
    public void update() {

    }

    @Override
    public void draw(SGL gl) {
        gl.pushMatrix();
        gl.translate(positionSupplier.get());
        Toolbox.draw3DPointer(gl);
        gl.popMatrix();
    }

    @Override
    public Vector3fc getPosition() {
        return positionSupplier.get();
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

    @Override
    public AABBf hitbox() {
        return new AABBf();
    }
}
