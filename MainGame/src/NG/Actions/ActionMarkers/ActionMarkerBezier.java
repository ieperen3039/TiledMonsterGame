package NG.Actions.ActionMarkers;

import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.Shapes.CustomShape;
import NG.Resources.GeneratorResource;
import NG.Resources.Resource;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 29-2-2020.
 */
public class ActionMarkerBezier implements ActionMarker {
    private static final float RELATIVE_BODY_WIDTH = 0.5f;
    private static final float RELATIVE_BODY_HEIGHT = 0.25f;
    private static final int RESOLUTION = 100;

    private final Resource<Mesh> body;

    public ActionMarkerBezier(Vector3fc start, Vector3fc secondPoint, Vector3fc thirdPoint, Vector3fc end) {
        body = new GeneratorResource<>(() -> generateBezier(start, secondPoint, thirdPoint, end), Mesh::dispose);
    }

    @Override
    public void draw(SGL gl) {
        gl.render(body.get(), null);
    }

    static Mesh generateBezier(
            Vector3fc startPos, Vector3fc secondPoint, Vector3fc thirdPoint, Vector3fc endPos
    ) {
        Vector3fc startDir = new Vector3f(secondPoint).sub(startPos);
        Vector3f pointSide = new Vector3f(startDir).cross(Vectors.Z).normalize(RELATIVE_BODY_WIDTH / 2);
        Vector3f pointUp = new Vector3f(pointSide).cross(startDir).normalize(RELATIVE_BODY_HEIGHT / 2);

        CustomShape frame = new CustomShape();

        Vector3f pp1 = new Vector3f(startPos).add(pointSide).add(pointUp);
        Vector3f pn1 = new Vector3f(startPos).add(pointSide).sub(pointUp);
        Vector3f np1 = new Vector3f(startPos).sub(pointSide).add(pointUp);
        Vector3f nn1 = new Vector3f(startPos).sub(pointSide).sub(pointUp);
        frame.addQuad(pp1, pn1, nn1, np1, new Vector3f(startDir).negate());

        Vector3f pp2 = new Vector3f();
        Vector3f pn2 = new Vector3f();
        Vector3f np2 = new Vector3f();
        Vector3f nn2 = new Vector3f();

        float delta = 1f / ActionMarkerBezier.RESOLUTION;
        for (float u = delta; u < 1 + (delta / 2f); u += delta) {
            pp2.set(pp1);
            pn2.set(pn1);
            np2.set(np1);
            nn2.set(nn1);

            Vector3f point = Vectors.bezierPoint(startPos, secondPoint, thirdPoint, endPos, u);
            Vector3f dir = Vectors.bezierDerivative(startPos, secondPoint, thirdPoint, endPos, u);

            pointSide.set(dir).cross(Vectors.Z).normalize(RELATIVE_BODY_WIDTH / 2);
            pointUp.set(pointSide).cross(dir).normalize(RELATIVE_BODY_HEIGHT / 2);

            pp1.set(point).add(pointSide).add(pointUp);
            pn1.set(point).add(pointSide).sub(pointUp);
            np1.set(point).sub(pointSide).add(pointUp);
            nn1.set(point).sub(pointSide).sub(pointUp);

            frame.addQuad(np1, pp1, pp2, np2, pointUp);
            frame.addQuad(pp1, pn1, pn2, pp2, pointSide);
            frame.addQuad(pn1, nn1, nn2, pn2, pointUp.negate());
            frame.addQuad(nn1, np1, np2, nn2, pointSide.negate());
        }

        Vector3f endDir = Vectors.bezierDerivative(startPos, secondPoint, thirdPoint, endPos, 1);
        frame.addQuad(pp1, pn1, nn1, np1, endDir);

        return frame.toFlatMesh();
    }
}
