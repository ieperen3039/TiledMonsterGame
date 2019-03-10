package NG.Rendering.Shapes;

import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Rendering.Shapes.Primitives.Collision;
import NG.Rendering.Shapes.Primitives.Plane;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Geert van Ieperen created on 30-10-2017.
 */
public interface Shape {

    /** returns all planes of this object in no specific order */
    Iterable<? extends Plane> getPlanes();

    /** @return the points of this plane in no specific order */
    Iterable<Vector3fc> getPoints();

    /** @see #getPlanes() */
    default Stream<? extends Plane> getPlaneStream() {
        return StreamSupport.stream(getPlanes().spliterator(), false);
    }

    /** @see #getPoints() */
    default Stream<? extends Vector3fc> getPointStream() {
        return StreamSupport.stream(getPoints().spliterator(), false);
    }

    /**
     * given a point on position {@code linePosition} moving in the direction of {@code direction}, calculates the
     * movement it is allowed to do before hitting this shape
     * @param linePosition a position vector on the line in local space
     * @param direction    the direction vector of the line in local space
     * @param endPoint     the endpoint of this vector, defined as {@code linePosition.add(direction)}
     * @return {@code null} if it does not hit with direction scalar < 1 otherwise, it provides a collision object about
     * the first collision with this shape
     */
    default Collision getCollision(Vector3fc linePosition, Vector3fc direction, Vector3fc endPoint) {
        return getPlaneStream()
                .parallel()
                // find the vector that hits the planes
                .map((plane) -> plane.getCollisionWith(linePosition, direction, endPoint))
                // exclude the vectors that did not hit
                .filter(Objects::nonNull)
                // return the shortest vector
                .min(Collision::compareTo)
                .orElse(null);
    }

    /**
     * loads a mesh, splitting it into sections of size containersize.
     * @param containerSize size of splitted container, which is applied in 3 dimensions
     * @param scale         possible scaling factor upon loading
     * @param path          path to the .obj file without extension
     * @return a list of shapes, each being roughly containersize in size
     */
    static List<Shape> loadSplit(float containerSize, Vector3fc scale, Path path)
            throws IOException {
        MeshFile file = MeshFile.loadFile(path, Vectors.O, scale);
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
            shapes.add(frame.wrapToShape());
        }
        return shapes;
    }
}
