package NG.Actions.ActionMarkers;

import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Resources.Resource;
import NG.Tools.Directory;

/**
 * @author Geert van Ieperen created on 28-2-2020.
 */
public interface ActionMarker {
    float SIZE_SCALAR = 0.6f;
    Resource<Mesh> arrowHead = Mesh.createResource(Directory.meshes, "markers", "arrow_head.obj");
    ActionMarker EMPTY_MARKER = gl -> {};

    void draw(SGL gl);
}
