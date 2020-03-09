package NG.Entities;

import NG.Actions.ActionIdle;
import NG.Actions.EntityAction;
import NG.CollisionDetection.BoundingBox;
import NG.Core.Game;
import NG.DataStructures.Generic.Pair;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shapes.GenericShapes;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 9-1-2019.
 */
public class Cube implements MovingEntity {
    private static final float SIZE = 1;
    private static int nr = 0;
    private final int id;

    private boolean isDisposed = false;
    protected Vector3fc position;
    private Pair<EntityAction, Float> action;
    private BoundingBox boundingBox;

    public Cube(Vector3f position) {
        this.position = position;
        id = nr++;

        float half = SIZE / 2;
        boundingBox = new BoundingBox(half, half, half, half, half, half);
        action = new Pair<>(new ActionIdle(position), 0f);
    }

    @Override
    public Pair<EntityAction, Float> getActionAt(float gameTime) {
        return action;
    }

    @Override
    public Vector3f getPositionAt(float gameTime) {
        return new Vector3f(position);
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
    public float getIntersection(Vector3fc origin, Vector3fc direction, float gameTime) {
        return 0;
    }

    @Override
    public BoundingBox getHitbox(float gameTime) {
        return boundingBox.getMoved(getPositionAt(gameTime));
    }

    @Override
    public void restore(Game game) {

    }
}
