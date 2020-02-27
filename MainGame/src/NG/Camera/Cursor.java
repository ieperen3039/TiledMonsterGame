package NG.Camera;

import NG.CollisionDetection.BoundingBox;
import NG.Core.Game;
import NG.Entities.Dummy;
import NG.Rendering.MatrixStack.SGL;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Vector3f;

import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 5-2-2019.
 */
public class Cursor extends Dummy {
    private transient Supplier<Vector3f> positionSupplier;

    public Cursor(Supplier<Vector3f> positionSupplier) {
        this.positionSupplier = positionSupplier;
    }

    @Override
    public void draw(SGL gl) {
        gl.pushMatrix();
        gl.translate(positionSupplier.get());
        Toolbox.draw3DPointer(gl);
        gl.popMatrix();
    }

    @Override
    public Vector3f getPositionAt(float gameTime) {
        return positionSupplier.get();
    }

    @Override
    public BoundingBox getHitbox(float gameTime) {
        return new BoundingBox(0, 0, 0, 0, 0, 0);
    }

    @Override
    public void restore(Game game) {
        positionSupplier = Vectors::newZeroVector;
    }
}
