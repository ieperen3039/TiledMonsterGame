package NG.Tools;

import NG.Camera.Camera;
import NG.DataStructures.Generic.Pair;
import NG.Engine.Game;
import NG.Rendering.GLFWWindow;
import NG.Rendering.MatrixStack.SGL;
import org.joml.Math;
import org.joml.*;

import java.util.Locale;

import static java.lang.Float.isNaN;

/**
 * A collection of utility functions for vectors, specifically for Vector2f
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class Vectors {
    private static final float PI = (float) Math.PI;

    public static Vector3f zeroVector() {
        return new Vector3f(0, 0, 0);
    }

    public static Vector3f zVector() {
        return new Vector3f(0, 0, 1);
    }

    public static Vector3f xVector() {
        return new Vector3f(1, 0, 0);
    }

    public static String toString(Vector3fc v) {
        return String.format(Locale.US, "(%1.3f, %1.3f, %1.3f)", v.x(), v.y(), v.z());
    }

    public static String toString(Vector2fc v) {
        return String.format(Locale.US, "(%1.3f, %1.3f)", v.x(), v.y());
    }

    public static String toString(Vector3ic v) {
        return String.format("(%d, %d, %d)", v.x(), v.y(), v.z());
    }

    public static String toString(Vector2ic v) {
        return String.format("(%d, %d)", v.x(), v.y());
    }

    public static String toString(Vector4fc v) {
        return String.format(Locale.US, "(%1.3f, %1.3f, %1.3f, %1.3f)", v.x(), v.y(), v.z(), v.w());
    }

    public static String stringAsVector(float x, float y) {
        return String.format("(%1.3f, %1.3f)", x, y);
    }

    /**
     * Rotates a two-dimensional vector on the z-axis in clockwise direction //TODO not counterclock?
     * @param target the vector to rotate
     * @param angle  the angle of rotation
     * @param dest   the vector to store the result
     * @return dest
     */
    public static Vector2f rotate(Vector2fc target, float angle, Vector2f dest) {
        float sin = sin(angle), cos = cos(angle);
        float tx = target.x();
        float ty = target.y();
        dest.x = tx * cos - ty * sin;
        dest.y = tx * sin + ty * cos;
        return dest;
    }

    /**
     * Rotates a two-dimensional vector on the z-axis in clockwise direction //TODO not counterclock?
     * @param target the vector to rotate
     * @param angle  the angle of rotation
     * @return the target vector holding the result
     */
    public static Vector2f rotate(Vector2f target, int angle) {
        float sin = sin(angle), cos = cos(angle);
        float tx = target.x;
        float ty = target.y;
        target.x = tx * cos - ty * sin;
        target.y = tx * sin + ty * cos;
        return target;
    }

    // a few mathematical shortcuts
    public static float cos(float theta) {
        return (float) Math.cos(theta);
    }

    public static float sin(float theta) {
        return (float) Math.sin(theta);
    }

    /**
     * @param vector any vector
     * @return theta such that a vector with {@code x = {@link #cos(float)}} and {@code y = {@link #sin(float)}} gives
     * {@code vector}, normalized.
     */
    public static float arcTan(Vector2fc vector) {
        return (float) Math.atan2(vector.y(), vector.x());
    }

    /**
     * high(er)-precision calculation of the angle between two vectors.
     * @param v1 a vector
     * @param v2 another vector
     * @return the angle in randians between v1 and v2
     */
    public static float angle(Vector2fc v1, Vector2fc v2) {
        float x1 = v1.x();
        float y1 = v1.y();
        float x2 = v2.x();
        float y2 = v2.y();
        double length1Sqared = x1 * x1 + y1 * y1;
        double length2Sqared = x2 * x2 + y2 * y2;
        double dot = x1 * x2 + y1 * y2;
        double cos = dot / (java.lang.Math.sqrt(length1Sqared * length2Sqared));

        // Cull because sometimes cos goes above 1 or below -1 because of lost precision
        cos = cos < 1 ? cos : 1;
        cos = cos > -1 ? cos : -1;
        return (float) java.lang.Math.acos(cos);
    }

    /**
     * returns the point closest to the given position on the line between startCoord and endCoord
     * @param position   a point in space
     * @param startCoord one end of the line
     * @param endCoord   the other end of the line
     * @return the position p on the line from {@code startCoord} to {@code endCoord} such that the distance to the
     * given {@code position} is minimal
     */
    public static Vector2f getIntersectionPointLine(Vector2fc position, Vector2fc startCoord, Vector2fc endCoord) {
        float aX = startCoord.x();
        float aY = startCoord.y();
        float abX = endCoord.x() - aX;
        float abY = endCoord.y() - aY;
        float t = ((position.x() - aX) * abX + (position.y() - aY) * abY) / (abX * abX + abY * abY);
        if (t < 0.0f) t = 0.0f;
        if (t > 1.0f) t = 1.0f;
        return new Vector2f(aX + t * abX, aY + t * abY);
    }

    /**
     * Calculates the smallest orb around the given points
     * @param points a number of points, at least two
     * @return Left, the middle of the found orb. Right, the radius of the found orb
     */
    public static Pair<Vector3fc, Float> getMinimalOrb(Iterable<Vector3fc> points) {
        // determine furthest two point
        float duoMaxSq = 0;
        Vector3f aMax = new Vector3f();
        Vector3f bMax = new Vector3f();
        for (Vector3fc a : points) {
            for (Vector3fc b : points) {
                float dist = a.distanceSquared(b);
                if (dist > duoMaxSq) {
                    duoMaxSq = dist;
                    aMax.set(a);
                    bMax.set(b);
                }
            }
        }

        // determine point furthest from the middle
        Vector3f mid = aMax.lerp(bMax, 0.5f);
        Vector3f outer = new Vector3f();
        float tripleMaxSq = 0;
        for (Vector3fc vector : points) {
            float dist = mid.distanceSquared(vector);
            if (dist > tripleMaxSq) {
                outer.set(vector);
                tripleMaxSq = dist;
            }
        }

        // if the furthest point is none of the two previous points, determine the circumscribed circle
        // https://en.wikipedia.org/wiki/Circumscribed_circle
        if ((tripleMaxSq > (duoMaxSq / 4)) && !(outer.equals(aMax) || outer.equals(bMax))) {
            Vector3f temp2 = new Vector3f();
            Vector3f temp3 = new Vector3f();

            Vector3fc a = aMax.sub(outer, new Vector3f());
            Vector3fc b = bMax.sub(outer, new Vector3f());

            Vector3f dif = b.mul(a.lengthSquared(), temp2)
                    .sub(a.mul(b.lengthSquared(), temp3), temp2);
            float scalar = 2 * a.cross(b, temp3).lengthSquared();

            mid.set(
                    dif.cross(a.cross(b, new Vector3f())).div(scalar).add(outer)
            );
        }

        return new Pair<>(mid, mid.distance(aMax));
    }

    /**
     * evaluates a beziér curve defined by vectors. The given ABCD vectors are kept intact.
     * @param A starting point
     * @param B first control point
     * @param C second control point
     * @param D ending point
     * @param u fraction of the curve to be requested
     * @return vector to the point on the curve on fraction u
     */
    public static Vector3f bezierPoint(Vector3fc A, Vector3fc B, Vector3fc C, Vector3fc D, double u) {
        Vector3f temp = new Vector3f();
        Vector3f point = new Vector3f();
        //A*(1−u)^3 + B*3u(1−u)^2 + C*3u^2(1−u) + D*u^3
        A.mul((float) ((1 - u) * (1 - u) * (1 - u)), point)
                .add(B.mul((float) (3 * u * (1 - u) * (1 - u)), temp), point)
                .add(C.mul((float) (3 * u * u * (1 - u)), temp), point)
                .add(D.mul((float) (u * u * u), temp), point);
        return point;
    }

    /**
     * evaluates the derivative of a beziér curve on a point defined by u
     * @see #bezierPoint(Vector3fc, Vector3fc, Vector3fc, Vector3fc, double)
     */
    public static Vector3f bezierDerivative(Vector3fc A, Vector3fc B, Vector3fc C, Vector3fc D, double u) {
        Vector3f direction = new Vector3f();
        Vector3f temp = new Vector3f();
        final Vector3f point = new Vector3f();
        //(B-A)*3*(1-u)^2 + (C-B)*6*(1-u)*u + (D-C)*3*u^2
        (B.sub(A, point))
                .mul((float) (3 * (1 - u) * (1 - u)), point)
                .add(C.sub(B, temp).mul((float) (6 * (1 - u) * u), temp), direction)
                .add(D.sub(C, temp).mul((float) (3 * u * u), temp), direction);
        return direction;
    }

    /**
     * @param vec any vector
     * @return true iff none of the scalars of the given vector is nan, and the length of the vector is not zero
     */
    public static boolean isScalable(Vector3fc vec) {
        return !isNaN(vec.x()) && !isNaN(vec.y()) && !isNaN(vec.z()) && !((vec.x() == 0) && (vec.y() == 0) && (vec.z() == 0));
    }

    /**
     * transforms a click on a screen of the given size on the given coordinate to a ray. Modifies origin and
     * direction.
     * @param camera       the view on the game world
     * @param windowWidth  the number of pixels of the viewport on the x axis
     * @param windowHeight the number of pixels of the viewport on the y axis
     * @param clickCoords  the coordinates of where was clicked on the screen
     * @param origin       the destination vector of the origin of the ray. The exact result is ({@link Camera#getEye()}
     *                     + Z_NEAR * direction)
     * @param direction    the direction of the ray, not normalized.
     * @param isometric
     */
    public static void windowCoordToRay(
            Camera camera, int windowWidth, int windowHeight, Vector2f clickCoords, Vector3f origin, Vector3f direction,
            boolean isometric
    ) {
        Matrix4f projection = SGL.getViewProjection(windowWidth, windowHeight, camera, isometric);
        int[] viewport = {0, 0, windowWidth, windowHeight};
        projection.unprojectRay(clickCoords, viewport, origin, direction);
    }

    public static Vector3f getNormalVector(Vector3fc A, Vector3fc B, Vector3fc C) {
        Vector3f BC = new Vector3f(C).sub(B);
        Vector3f BA = new Vector3f(A).sub(B);
        return BA.cross(BC).normalize();
    }

    /**
     * transforms a click on the given x and y screen coordinates in the given game instance to a ray, which is returned
     * in the origin and direction vectors. Modifies origin and direction.
     * @param game      the game instance
     * @param xSc       the x screen coordinate of the click
     * @param ySc       the y screen coordinate of the click
     * @param origin    the destination vector of the origin of the ray
     * @param direction the direction of the ray, not normalized.
     */
    public static void windowCoordToRay(Game game, int xSc, int ySc, Vector3f origin, Vector3f direction) {
        GLFWWindow window = game.window();
        Camera camera = game.camera();
        Vector2f winCoords = new Vector2f(xSc, ySc);
        windowCoordToRay(camera, window.getWidth(), window.getHeight(), winCoords, origin, direction, game.settings().ISOMETRIC_VIEW);
    }
}
