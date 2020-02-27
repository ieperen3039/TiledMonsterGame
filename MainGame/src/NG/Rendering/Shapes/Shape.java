package NG.Rendering.Shapes;

import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Rendering.Shapes.Primitives.Plane;
import NG.Resources.Resource;
import NG.Tools.Directory;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.AABBf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * @author Geert van Ieperen created on 30-10-2017.
 */
public interface Shape {

    /** returns all planes of this object in no specific order */
    Collection<? extends Plane> getPlanes();

    /** @return the points of this plane in no specific order */
    Collection<Vector3fc> getPoints();

    static Resource<Shape> createResource(String... path) {
        return Resource.derive(MeshFile.createResource(Directory.meshes, path), MeshFile::getShape);
    }

    /**
     * given a point on position {@code origin} and a direction of {@code direction}, calculates the fraction t such
     * that (origin + direction * t) lies on this shape, or Float.POSITIVE_INFINITY if it does not hit.
     * @param origin    the begin of a line segment
     * @param direction the direction of the line segment
     * @return the scalar t
     */
    default float getIntersectionScalar(Vector3fc origin, Vector3fc direction) {
        float least = Float.POSITIVE_INFINITY;

        for (Plane plane : getPlanes()) {
            float intersectionScalar = plane.getIntersectionScalar(origin, direction);

            if (intersectionScalar < least) {
                least = intersectionScalar;
            }
        }

        return least;
    }

    AABBf getBoundingBox();

    /**
     * splits the mesh into sections of size containersize.
     * @param file          the file to split
     * @param containerSize size of splitted container, which is applied in 3 dimensions
     * @return a list of shapes, each being roughly containersize in size
     */
    static List<Shape> split(MeshFile file, float containerSize) {
        HashMap<Vector3i, CustomShape> world = new HashMap<>();

        for (Mesh.Face f : file.getFaces()) {
            Vector3fc[] edges = new Vector3fc[f.size()];
            Vector3f minimum = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
            for (int i = 0; i < f.size(); i++) {
                Vector3fc p = file.getVertices().get(f.vert[i]);
                minimum.min(p);
                edges[i] = p;
            }

            int x = (int) (minimum.x / containerSize);
            int y = (int) (minimum.y / containerSize);
            int z = (int) (minimum.z / containerSize);

            Vector3i key = new Vector3i(x, y, z);
            CustomShape container = world.computeIfAbsent(key, k ->
                    new CustomShape(new Vector3f(x + 0.5f, y + 0.5f, -Float.MAX_VALUE))
            );

            Vector3f normal = new Vector3f();
            for (int ind : f.norm) {
                if (ind < 0) continue;
                normal.add(file.getNormals().get(ind));
            }
            if (Vectors.isScalable(normal)) {
                normal.normalize();
            } else {
                normal = null;
                Logger.DEBUG.printSpamless(file.toString(), file + " has at least one not-computed normal");
            }

            container.addPlane(normal, edges);
        }

        Collection<CustomShape> containers = world.values();
        Logger.DEBUG.print("Loaded model " + file + " in " + containers.size() + " parts");

        List<Shape> shapes = new ArrayList<>();
        for (CustomShape frame : containers) {
            shapes.add(frame.toShape());
        }
        return shapes;
    }
}
