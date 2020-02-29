package NG.Actions.ActionMarkers;

import NG.Actions.EntityAction;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.Shapes.CustomShape;
import NG.Resources.GeneratorResource;
import NG.Resources.Resource;
import NG.Tools.Vectors;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 29-2-2020.
 */
public class ActionMarkerGenerated implements ActionMarker {
    private static final float RELATIVE_BODY_WIDTH = 0.5f;
    private static final float RELATIVE_BODY_HEIGHT = 0.25f;
    private static final int RESOLUTION = 10;
    private static final float ARROW_SIZE = 1.0f; // real size of the arrow

    private final Resource<Mesh> body;
    private final Vector3fc arrowMiddle;
    private final Quaternionf arrowRotation;
    private final float arrowSize;

    public ActionMarkerGenerated(EntityAction action) {
        Vector3fc end = action.getEndPosition();

        float t = action.duration();
        if (Float.isInfinite(t)) {
            t = 1;
        } else {
            while (t > 0 && action.getPositionAt(t).distance(end) < ARROW_SIZE * SIZE_SCALAR) {
                t -= 1f / RESOLUTION;
            }
        }
        float tBodyEnd = t;

        // calculate arrow head
        Vector3f arrowStart = action.getPositionAt(tBodyEnd);
        Vector3f dir = new Vector3f(end).sub(arrowStart);
        float hzDistance = Math.sqrt(dir.x * dir.x + dir.y * dir.y);
        arrowRotation = new Quaternionf()
                .rotateZ(Math.atan2(dir.y, dir.x))
                .rotateY(-Math.atan2(dir.z, hzDistance));
        arrowSize = dir.length();
        arrowMiddle = arrowStart.add(dir.div(2));

        // set body mesh
        this.body = new GeneratorResource<>(() -> generate(action, tBodyEnd), Mesh::dispose);
    }

    @Override
    public void draw(SGL gl) {
        gl.pushMatrix();
        {
            gl.translate(arrowMiddle);
            gl.rotate(arrowRotation);
            gl.scale(arrowSize / 2, SIZE_SCALAR * (arrowSize / 2), SIZE_SCALAR);
            gl.render(arrowHead.get(), null);
        }
        gl.popMatrix();
        gl.render(body.get(), null);
    }

    static Mesh generate(EntityAction function, float tEnd) {
        float delta = 1f / RESOLUTION;
        Vector3fc startPos = function.getStartPosition();
        Vector3fc startDir = function.getPositionAt(delta).sub(startPos);
        Vector3f pointSide = new Vector3f(startDir).cross(Vectors.Z).normalize(SIZE_SCALAR * RELATIVE_BODY_WIDTH / 2);
        Vector3f pointUp = new Vector3f(pointSide).cross(startDir).normalize(SIZE_SCALAR * RELATIVE_BODY_HEIGHT / 2);

        CustomShape frame = new CustomShape();

        Vector3f point1 = new Vector3f(startPos);
        Vector3f pp1 = new Vector3f(startPos).add(pointSide).add(pointUp);
        Vector3f pn1 = new Vector3f(startPos).add(pointSide).sub(pointUp);
        Vector3f np1 = new Vector3f(startPos).sub(pointSide).add(pointUp);
        Vector3f nn1 = new Vector3f(startPos).sub(pointSide).sub(pointUp);
        frame.addQuad(pp1, pn1, nn1, np1, new Vector3f(startDir).negate());

        Vector3f point2;
        Vector3f dir = new Vector3f();
        Vector3f pp2 = new Vector3f();
        Vector3f pn2 = new Vector3f();
        Vector3f np2 = new Vector3f();
        Vector3f nn2 = new Vector3f();

        int nrSections = (int) Math.ceil(tEnd * RESOLUTION);
        for (int i = 1; i <= nrSections; i++) {
            float t = (i == nrSections) ? tEnd : (i * delta);

            pp2.set(pp1);
            pn2.set(pn1);
            np2.set(np1);
            nn2.set(nn1);

            point2 = point1;
            point1 = function.getPositionAt(t);
            dir.set(point1).sub(point2);

            pointSide.set(dir).cross(Vectors.Z).normalize(SIZE_SCALAR * RELATIVE_BODY_WIDTH / 2);
            pointUp.set(pointSide).cross(dir).normalize(SIZE_SCALAR * RELATIVE_BODY_HEIGHT / 2);

            pp1.set(point1).add(pointSide).add(pointUp);
            pn1.set(point1).add(pointSide).sub(pointUp);
            np1.set(point1).sub(pointSide).add(pointUp);
            nn1.set(point1).sub(pointSide).sub(pointUp);

            frame.addQuad(np1, pp1, pp2, np2, pointUp);
            frame.addQuad(pp1, pn1, pn2, pp2, pointSide);
            frame.addQuad(pn1, nn1, nn2, pn2, pointUp.negate());
            frame.addQuad(nn1, np1, np2, nn2, pointSide.negate());
        }

        frame.addQuad(pp1, pn1, nn1, np1, dir);

        return frame.toFlatMesh();
    }
}
