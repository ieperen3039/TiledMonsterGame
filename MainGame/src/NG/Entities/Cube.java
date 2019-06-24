package NG.Entities;

import NG.CollisionDetection.BoundingBox;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shapes.GenericShapes;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen created on 9-1-2019.
 */
public class Cube implements Entity {
    private static final float SIZE = 1;
    private static int nr = 0;
    private final int id;

    private boolean isDisposed = false;
    protected Vector3f position;
    private BoundingBox boundingBox;

    public Cube(Vector3f position) {
        this.position = position;
        id = nr++;

        float half = SIZE / 2;
        boundingBox = new BoundingBox(half, half, half, half, half, half);
    }

    @Override
    public Vector3f getPositionAt(float currentTime) {
        return position;
    }

    @Override
    public void draw(SGL gl) {
        gl.pushMatrix();
        {
            gl.translate(position);
            gl.scale(SIZE / 2);
            gl.render(GenericShapes.CUBE, this);
        }
        gl.popMatrix();
    }

    @Override
    public void update(float gameTime) {

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
    public String toString() {
        return this.getClass().getSimpleName() + " " + id;
    }

    @Override
    public BoundingBox hitbox() {
        return boundingBox;
    }

    @Override
    public void collideWith(Object other, float collisionTime) {

    }
}