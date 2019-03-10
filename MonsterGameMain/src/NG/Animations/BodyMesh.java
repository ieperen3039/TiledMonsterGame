package NG.Animations;

import NG.Rendering.MeshLoading.Mesh;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen created on 6-3-2019.
 */
public interface BodyMesh extends Mesh { // TODO extends MeshShape
    /**
     * @return the size of the bounding box of this mesh in (x, y, z) direction.
     */
    Vector3f getSize();
}
