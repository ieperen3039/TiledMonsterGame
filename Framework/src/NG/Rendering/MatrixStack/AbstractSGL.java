package NG.Rendering.MatrixStack;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Stack;

/**
 * An implementation of the Shader GL object, which handles a model matrix and stack operations. No shader-specific
 * operations are covered.
 * @author Geert van Ieperen created on 30-1-2019.
 */
public abstract class AbstractSGL implements SGL {
    protected static final Painter LOCK = new Painter();
    private Stack<Matrix4f> matrixStack;
    private Matrix4f modelMatrix;

    public AbstractSGL() {
        matrixStack = new Stack<>();
        modelMatrix = new Matrix4f();
    }

    @Override
    public void rotate(float angle, float x, float y, float z) {
        modelMatrix.rotate(angle, x, y, z);
    }

    @Override
    public void rotate(Vector3f axis, float angle) {
        modelMatrix.rotate(angle, axis);
    }

    @Override
    public void translate(float x, float y, float z) {
        modelMatrix.translate(x, y, z);
    }

    @Override
    public void scale(float x, float y, float z) {
        modelMatrix.scale(x, y, z);
    }

    @Override
    public void scale(Vector3fc s) {
        modelMatrix.scale(s);
    }

    @Override
    public Vector3f getPosition(Vector3fc position) {
        Vector3f result = new Vector3f();
        position.mulPosition(modelMatrix, result);
        return result;
    }

    @Override
    public Vector3f getDirection(Vector3fc direction) {
        Vector3f result = new Vector3f();
        direction.mulDirection(modelMatrix, result);
        return result;
    }

    @Override
    public void pushMatrix() {
        matrixStack.push(new Matrix4f(modelMatrix));
    }

    @Override
    public void popMatrix() {
        modelMatrix = matrixStack.pop();
    }

    @Override
    public void rotate(Quaternionf rotation) {
        modelMatrix.rotate(rotation);
    }

    @Override
    public void translate(Vector3fc v) {
        modelMatrix.translate(v);
    }

    @Override
    public void multiplyAffine(Matrix4f postTransformation) {
        modelMatrix.mulAffine(postTransformation);
    }

    protected Matrix4f getModelMatrix() {
        return modelMatrix;
    }
}
