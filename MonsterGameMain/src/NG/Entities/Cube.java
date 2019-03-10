package NG.Entities;

import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shapes.GenericShapes;
import org.joml.AABBf;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen created on 9-1-2019.
 */
public class Cube implements Entity {
    private static int nr = 0;
    private final int id;

    private boolean isDisposed = false;
    protected Vector3f position;

    public Cube(Vector3f position) {
        this.position = position;
        id = nr++;
    }

    @Override
    public Vector3f getPosition(float currentTime) {
        return position;
    }

    @Override
    public void draw(SGL gl) {
        gl.pushMatrix();
        {
            gl.translate(position);
            gl.render(GenericShapes.CUBE, this);
        }
        gl.popMatrix();
    }

    @Override
    public void onClick(int button) {
        dispose();
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
        return new AABBf(-1, -1, -1, 1, 1, 1);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " " + id;
    }
}
