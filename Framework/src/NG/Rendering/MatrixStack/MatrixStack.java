package NG.Rendering.MatrixStack;


import NG.Tools.Vectors;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 19-11-2017.
 */
public interface MatrixStack {
    /**
     * rotates the axis frame such that the z-axis points from source to target vector, and translates the system to
     * source if (target == source) the axis will not turn
     * @param source the vector where the axis will have its orgin upon returning
     * @param target the vector in which direction the z-axis will point upon returning
     */
    default void pointFromTo(Vector3f source, Vector3f target) {
        if (target.equals(source)) return;
        translate(source);

        Vector3f sourceToTarget = new Vector3f();
        target.sub(source, sourceToTarget).normalize();

        rotate(new Quaternionf().rotateTo(Vectors.Z, sourceToTarget));
    }

    /**
     * rotates the coordinate system along the axis defined by (x, y, z)
     * @param angle in radians
     */
    void rotate(float angle, float x, float y, float z);

    /**
     * moves the system x to the x-axis, y to the y-asis and z toward the z-axis
     */
    void translate(float x, float y, float z);

    /**
     * scale in the direction of the appropriate axes
     */
    void scale(float x, float y, float z);

    /**
     * calculate the position of a vector in reference to the space in which this is initialized
     * @param position a local vector
     * @return that vector in world-space
     */
    Vector3f getPosition(Vector3fc position);

    /**
     * calculate a direction vector in reference to the space in which this is initialized
     * @param direction a local direction
     * @return this direction in world-space
     */
    Vector3f getDirection(Vector3fc direction);

    /**
     * stores the current state of the transformations on a stack
     */
    void pushMatrix();

    /**
     * restores the current state of the transformations to the top of the stack
     */
    void popMatrix();

    /**
     * @param axis  the axis to rotate around
     * @param angle the angle to rotate in radians
     * @see #rotate(float, float, float, float)
     */
    default void rotate(Vector3f axis, float angle) {
        rotate(angle, axis.x(), axis.y(), axis.z());
    }

    /**
     * @see #rotate(Vector3f, float)
     */
    void rotate(Quaternionf rotation);

    /**
     * @see #translate(float, float, float)
     */
    default void translate(Vector3fc v) {
        translate(v.x(), v.y(), v.z());
    }

    /**
     * scale uniformly in all directions with the given factor
     */
    default void scale(float s) {
        if (s != 1) scale(s, s, s);
    }

    /**
     * @see #scale(float, float, float)
     */
    default void scale(Vector3fc s) {
        scale(s.x(), s.y(), s.z());
    }

    /**
     * Applies an affine transformation.
     * @param postTransformation some affine matrix
     */
    void multiplyAffine(Matrix4f postTransformation);
}
