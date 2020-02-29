package NG.Actions.ActionMarkers;

import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Resources.Resource;
import NG.Tools.Directory;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 28-2-2020.
 */
public class ActionMarkerArrow implements ActionMarker {
    private static final Resource<Mesh> arrowBody = Mesh.createResource(Directory.meshes, "markers", "arrow_body.obj");

    private final Vector3fc end;
    private final Quaternionfc rotation;
    private final float bodyLength;

    public ActionMarkerArrow(Vector3fc start, Vector3fc end) {
        this.end = end;
        this.bodyLength = (start.distance(end) - 2) / SIZE_SCALAR;

        Vector3f dir = new Vector3f(end).sub(start);
        double hzDistance = Math.sqrt(dir.x * dir.x + dir.y * dir.y);
        this.rotation = new Quaternionf()
                .rotateZ((float) Math.atan2(dir.y, dir.x))
                .rotateY((float) -Math.atan2(dir.z, hzDistance));
    }

    @Override
    public void draw(SGL gl) {
        gl.pushMatrix();
        {
            gl.translate(end);
            gl.rotate(rotation);
            gl.scale(SIZE_SCALAR);
            // move back for half the arrow head length
            gl.translate(-1, 0, 0);
            gl.render(arrowHead.get(), null);

            if (bodyLength > 0) {
                gl.translate(-bodyLength / 2f - 1, 0, 0);
                gl.scale(bodyLength / 2f, 1, 1);
                gl.render(arrowBody.get(), null);
            }
        }
        gl.popMatrix();
    }
}
