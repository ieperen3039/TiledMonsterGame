package NG.Tools;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * calculates a list of coordinates from A (exclusive) to B (inclusive), such that the returned path is the fastest path
 * to B. Must be overridden for a cost function.
 * @author Geert van Ieperen created on 19-2-2019.
 */
public abstract class AStar implements Callable<List<Vector2i>> {
    public final Vector2i target;
    private final int xMax;
    private final int yMax;

    private final PriorityQueue<ANode> found = new PriorityQueue<>();
    private final Map<Integer, Map<Integer, ANode>> openSet = new HashMap<>();
    private final Map<Integer, Set<Integer>> closedSet = new HashMap<>();
    private final ANode srcNode;

    public AStar(Vector2ic source, Vector2ic target, int xMax, int yMax) {
        this.target = new Vector2i(target);
        this.xMax = xMax;
        this.yMax = yMax;
        srcNode = new ANode(source);
        found.add(srcNode);

        HashMap<Integer, ANode> sourceSet = new HashMap<>();
        sourceSet.put(srcNode.y, srcNode);
        openSet.put(srcNode.x, sourceSet);
    }

    @Override
    public List<Vector2i> call() {
        ANode node = srcNode;

        while (!found.isEmpty()) {
            // take one from the found queue and
            node = found.remove();
            if (node.x == target.x && node.y == target.y) break;

            int x = node.x;
            int y = node.y;
            // add to closed set
            closedSet.computeIfAbsent(x, k -> new HashSet<>()).add(y);

            // positive x
            checkNode(node, x + 1, y);
            // positive y
            checkNode(node, x, y + 1);
            // negative x
            checkNode(node, x - 1, y);
            // negative y
            checkNode(node, x, y - 1);
        }

        if (found.isEmpty()) return new ArrayList<>(); // TODO return null;

        List<Vector2i> path = new ArrayList<>();

        while (node != srcNode) {
            path.add(new Vector2i(node.x, node.y));
            node = node.source; // all nodes but srcNode have a source
        }

        // make list start with source
        Collections.reverse(path);

        return path;
    }

    /**
     * analyse the neighbour (x, y) of the given node
     * @param previous the origin node
     * @param x        the x coordinate of the neighbour of this node
     * @param y        the y coordinate of the neighbour of this node
     */
    public void checkNode(ANode previous, int x, int y) {
        if (x < 0 || y < 0 || x > xMax || y > yMax) return;

        if (inClosedSet(x, y)) return;

        float distance = distanceAdjacent(previous.x, previous.y, x, y);
        if (Float.isInfinite(distance)) return;

        if (!inOpenSet(x, y)) {
            ANode newNode = new ANode(x, y, previous, distance);
            openSet.computeIfAbsent(x, k -> new HashMap<>()).put(y, newNode);
            found.add(newNode);
            return;
        }

        // node already exists
        ANode neighbour = getNode(x, y);
        float neighbourNewGScore = previous.gScore + distance;

        if (neighbourNewGScore > neighbour.gScore) return;

        // got a new high-score
        neighbour.gScore = neighbourNewGScore;
        neighbour.source = previous;

        // target not found
    }

    public ANode getNode(int x, int y) {
        return openSet.get(x).get(y);
    }

    /**
     * Calculate the cost of going from (x1, y1) to (x2, y2). These two coordinates are adjacent
     * @param x1 x of the first coordinate
     * @param y1 y of the first coordinate
     * @param x2 x of the second coordinate
     * @param y2 y of the second coordinate
     * @return the real cost of going past this.
     */
    public abstract float distanceAdjacent(int x1, int y1, int x2, int y2);

    public float distanceHeuristic(int x1, int y1, int x2, int y2) {
        int dx = x1 - x2;
        int dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * @param factor the y displacement per x displacement
     * @return the x displacement per 1 unit distance
     */
    public static float lift(float factor) {
        return (float) Math.sqrt(1f / (factor * factor + 1));
    }

    protected boolean inOpenSet(int x, int y) {
        return openSet.getOrDefault(x, Collections.emptyMap()).containsKey(y);
    }

    protected boolean inClosedSet(int x, int y) {
        return closedSet.getOrDefault(x, Collections.emptySet()).contains(y);
    }

    private class ANode implements Comparable<ANode> {
        final int x;
        final int y;
        final float distToTarget;

        ANode source;
        float gScore;

        private ANode(int x, int y, ANode comeFrom, float distance) {
            this.x = x;
            this.y = y;
            this.gScore = comeFrom.gScore + distance;
            this.distToTarget = distanceHeuristic(x, y, target.x, target.y);
            this.source = comeFrom;
        }

        private ANode(Vector2ic sourcePosition) {
            this.x = sourcePosition.x();
            this.y = sourcePosition.y();
            this.gScore = 0;
            this.distToTarget = distanceHeuristic(x, y, target.x, target.y);
            this.source = null;
        }

        @Override
        public int compareTo(ANode other) {
            return Float.compare(getDist(), other.getDist());
        }

        public float getDist() {
            return distToTarget + gScore;
        }

        @Override
        public int hashCode() {
            return Float.hashCode(getDist());
        }
    }
}
