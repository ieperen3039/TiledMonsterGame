package NG.CollisionDetection;

import NG.DataStructures.Generic.AveragingQueue;
import NG.DataStructures.Generic.ConcurrentArrayList;
import NG.DataStructures.Generic.PairList;
import NG.Entities.Entity;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.RayAabIntersection;
import org.joml.Vector3fc;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * @author Geert van Ieperen created on 10-3-2018.
 */
public class CollisionDetection {
    private static final int MAX_COLLISION_ITERATIONS = 5;
    private CollisionEntity[] xLowerSorted;
    private CollisionEntity[] yLowerSorted;
    private CollisionEntity[] zLowerSorted;

    private AveragingQueue avgCollision;
    private float previousTime = 0f;

    private Collection<Entity> entityList;
    private Collection<Entity> newEntities;

    /**
     * @see #CollisionDetection(Collection)
     */
    public CollisionDetection(Entity... entities) {
        this(Arrays.asList(entities));
    }

    /**
     * Collects the given entities and allows collision and phisics calculations to influence these entities
     * @param entities a list of fixed entities. Entities in this collection should not move, but if they do, dynamic
     *                 objects might phase through when moving in opposite direction. Apart from this case, the
     *                 collision detection still functions.
     */
    public CollisionDetection(Collection<Entity> entities) {
        this.entityList = new CopyOnWriteArrayList<>();
        this.newEntities = new ConcurrentArrayList<>();

        Logger.printOnline(() ->
                String.format("Collision pair count average: %1.01f", avgCollision.average())
        );

        int nOfEntities = entities.size();
        xLowerSorted = new CollisionEntity[nOfEntities];
        yLowerSorted = new CollisionEntity[nOfEntities];
        zLowerSorted = new CollisionEntity[nOfEntities];

        populate(entities, xLowerSorted, yLowerSorted, zLowerSorted);

        avgCollision = new AveragingQueue(5);
    }

    /**
     * populates the given arrays, and sorts the arrays on the lower coordinate of the hitboxes (x, y and z
     * respectively)
     */
    private void populate(
            Collection<Entity> entities,
            CollisionEntity[] xLowerSorted, CollisionEntity[] yLowerSorted, CollisionEntity[] zLowerSorted
    ) {
        int i = 0;
        for (Entity entity : entities) {
            CollisionEntity asCollisionEntity = new CollisionEntity(entity);
            asCollisionEntity.update(previousTime);
            xLowerSorted[i] = asCollisionEntity;
            yLowerSorted[i] = asCollisionEntity;
            zLowerSorted[i] = asCollisionEntity;
            i++;
        }

        final int INSERTION_SORT_BOUND = 64;
        if (entities.size() < INSERTION_SORT_BOUND) {
            Toolbox.insertionSort(xLowerSorted, CollisionEntity::xLower);
            Toolbox.insertionSort(yLowerSorted, CollisionEntity::yLower);
            Toolbox.insertionSort(zLowerSorted, CollisionEntity::zLower);

        } else {
            Arrays.sort(xLowerSorted, (a, b) -> Float.compare(a.xLower(), b.xLower()));
            Arrays.sort(yLowerSorted, (a, b) -> Float.compare(a.yLower(), b.yLower()));
            Arrays.sort(zLowerSorted, (a, b) -> Float.compare(a.zLower(), b.zLower()));
        }
    }

    public void processCollisions(float currentTime) {
        if (currentTime == previousTime) return;

        /** -- clean and restore invariants -- */

        // remove disposed entities
        List<Entity> removeEntities = new ArrayList<>();

        for (Entity e : entityList) {
            if (e.isDisposed()) {
                removeEntities.add(e);
            }
        }

        if (!removeEntities.isEmpty()) {
            deleteEntities(removeEntities);
            removeEntities.clear();
        }

        // add new entities
        newEntities.removeIf(Entity::isDisposed);
        if (!newEntities.isEmpty()) {
            entityList.addAll(newEntities);
            mergeNewEntities(newEntities);
            newEntities.clear();
        }

        // update representation
        for (CollisionEntity entity : entityArray()) {
            entity.update(currentTime);
        }

        /** -- analyse the collisions -- */

        /* As a single collision may result in a previously not-intersecting pair to collide,
         * we shouldn't re-use the getIntersectingPairs method nor reduce by non-collisions.
         * On the other hand, we may assume collisions of that magnitude appear seldom
         */
        PairList<Entity, Entity> pairs = getIntersectingPairs();

        int remainingLoops = MAX_COLLISION_ITERATIONS / 2;
        int unresolved;
        do {
            unresolved = analyseCollisions(previousTime, currentTime, pairs);
        } while ((unresolved > 0) && (--remainingLoops > 0) && !Thread.currentThread().isInterrupted());

        previousTime = currentTime;
    }

    /**
     * @param startTime first moment to consider
     * @param endTime   last moment to consider
     * @param pairs
     * @return the number of unresolved collisions after this
     */
    private int analyseCollisions(float startTime, float endTime, PairList<Entity, Entity> pairs) {
        return (int) IntStream.range(0, pairs.size())
                .parallel()
                // resolve collisions twice, to keep only the problems
                .filter(n -> checkCollisionPair(pairs.left(n), pairs.right(n), startTime, endTime))
                .filter(n -> checkCollisionPair(pairs.left(n), pairs.right(n), startTime, endTime))
                .count();
    }


    /**
     * @param alpha     one entity
     * @param beta      another entity
     * @param startTime first moment in time to consider (inclusive)
     * @param endTime   last moment in time to consider (exclusive)
     * @return true iff these pairs indeed collided before endTime
     */
    private boolean checkCollisionPair(Entity alpha, Entity beta, float startTime, float endTime) {
        // this may change with previous collisions
        if (alpha.isDisposed() || beta.isDisposed() || alpha == beta) return false;

        BoundingBox aBox = alpha.hitbox();
        Vector3fc aPos = alpha.getPositionAt(startTime);
        Vector3fc aMove = alpha.getPositionAt(endTime).sub(aPos);

        BoundingBox bBox = beta.hitbox();
        Vector3fc bPos = beta.getPositionAt(startTime);
        Vector3fc bMove = beta.getPositionAt(endTime).sub(bPos);

        float aFrac = aBox.relativeCollisionFraction(aPos, aMove, bBox, bPos, bMove);
        float bFrac = bBox.relativeCollisionFraction(bPos, bMove, aBox, aPos, aMove);

        float hitFrac = Math.min(aFrac, bFrac);
        if (hitFrac == 1) return false;

        float collisionTime = startTime + hitFrac * (endTime - startTime);

        alpha.collideWith(beta, collisionTime);
        beta.collideWith(alpha, collisionTime);

        return true;
    }

    /**
     * generate a list (possibly empty) of all pairs of objects that may have collided. This can include (parts of) the
     * ground, but not an object with itself. One pair does not occur the other way around.
     * @return a collection of pairs of objects that are close to each other
     */
    private PairList<Entity, Entity> getIntersectingPairs() {
        Toolbox.insertionSort(xLowerSorted, CollisionEntity::xLower);
        Toolbox.insertionSort(yLowerSorted, CollisionEntity::yLower);
        Toolbox.insertionSort(zLowerSorted, CollisionEntity::zLower);

        assert testInvariants();

        CollisionEntity[] entityArray = entityArray();
        int nrOfEntities = entityArray.length;

        // initialize id values to correspond to the array
        for (int i = 0; i < nrOfEntities; i++) {
            entityArray[i].setId(i);
        }

        AdjacencyMatrix adjacencies = new AdjacencyMatrix(nrOfEntities, 3);

        checkOverlap(adjacencies, xLowerSorted, CollisionEntity::xLower, CollisionEntity::xUpper);
        checkOverlap(adjacencies, yLowerSorted, CollisionEntity::yLower, CollisionEntity::yUpper);
        checkOverlap(adjacencies, zLowerSorted, CollisionEntity::zLower, CollisionEntity::zUpper);

        int nrOfElts = adjacencies.nrOfFoundElements();

        PairList<Entity, Entity> allEntityPairs = new PairList<>(nrOfElts);
        adjacencies.forEach((i, j) -> allEntityPairs.add(
                entityArray[i].entity, entityArray[j].entity
        ));

        avgCollision.add(nrOfElts);
        return allEntityPairs;
    }

    /**
     * tests whether the invariants holds. Throws an error if any of the arrays is not correctly sorted or any other
     * assumption no longer holds
     */
    boolean testInvariants() {
        String source = Logger.getCallingMethod(1);
        Logger.DEBUG.printSpamless(source, "\n    " + source + " Checking collision detection invariants");

        // all arrays contain all entities
        Set<Entity> allEntities = new HashSet<>();
        for (CollisionEntity colEty : entityArray()) {
            allEntities.add(colEty.entity);
        }

        for (CollisionEntity collEty : xLowerSorted) {
            if (!allEntities.contains(collEty.entity)) {
                throw new IllegalStateException("Array x does not contain entity " + collEty.entity);
            }
        }
        for (CollisionEntity collEty : yLowerSorted) {
            if (!allEntities.contains(collEty.entity)) {
                throw new IllegalStateException("Array y does not contain entity " + collEty.entity);
            }
        }
        for (CollisionEntity collEty : zLowerSorted) {
            if (!allEntities.contains(collEty.entity)) {
                throw new IllegalStateException("Array z does not contain entity " + collEty.entity);
            }
        }

        // all arrays are of equal length
        if ((xLowerSorted.length != yLowerSorted.length) || (xLowerSorted.length != zLowerSorted.length)) {
            Logger.ERROR.print(Arrays.toString(entityArray()));
            throw new IllegalStateException("Entity arrays have different lengths: "
                    + xLowerSorted.length + ", " + yLowerSorted.length + ", " + zLowerSorted.length
            );
        }

        // x is sorted
        float init = -Float.MAX_VALUE;
        for (int i = 0; i < xLowerSorted.length; i++) {
            CollisionEntity collisionEntity = xLowerSorted[i];
            if (collisionEntity.xLower() < init) {
                Logger.ERROR.print("Sorting error on x = " + i);
                Logger.ERROR.print(Arrays.toString(xLowerSorted));
                throw new IllegalStateException("Sorting error on x = " + i);
            }
            init = collisionEntity.xLower();
        }

        // y is sorted
        init = -Float.MAX_VALUE;
        for (int i = 0; i < yLowerSorted.length; i++) {
            CollisionEntity collisionEntity = yLowerSorted[i];
            if (collisionEntity.yLower() < init) {
                Logger.ERROR.print("Sorting error on y = " + i);
                Logger.ERROR.print(Arrays.toString(yLowerSorted));
                throw new IllegalStateException("Sorting error on y = " + i);
            }
            init = collisionEntity.yLower();
        }

        // z is sorted
        init = -Float.MAX_VALUE;
        for (int i = 0; i < zLowerSorted.length; i++) {
            CollisionEntity collisionEntity = zLowerSorted[i];
            if (collisionEntity.zLower() < init) {
                Logger.ERROR.print("Sorting error on z = " + i);
                Logger.ERROR.print(Arrays.toString(zLowerSorted));
                throw new IllegalStateException("Sorting error on z = " + i);
            }
            init = collisionEntity.zLower();
        }

        return true;
    }

    /**
     * iterating over the sorted array, increase the value of all pairs that have coinciding intervals
     * @param adjacencyMatrix the matrix where the pairs are marked using entity id's
     * @param sortedArray     an array sorted increasingly on the lower mapping
     * @param lower           a function that maps to the lower value of the interval of the entity
     * @param upper           a function that maps an entity to its upper interval
     */
    protected void checkOverlap(
            AdjacencyMatrix adjacencyMatrix, CollisionEntity[] sortedArray, Function<CollisionEntity, Float> lower,
            Function<CollisionEntity, Float> upper
    ) {
        // INVARIANT:
        // all items i where i.lower < source.lower, are already added to the matrix

        int nOfItems = sortedArray.length;
        for (int i = 0; i < (nOfItems - 1); i++) {
            CollisionEntity subject = sortedArray[i];

            // increases the checks count of every source with index less than i, with position less than the given minimum
            int j = i + 1;
            CollisionEntity target = sortedArray[j++];

            // while the lowerbound of target is less than the upperbound of our subject
            while (lower.apply(target) <= upper.apply(subject)) {
                adjacencyMatrix.add(target.id, subject.id);

                if (j == nOfItems) break;
                target = sortedArray[j++];
            }
        }
    }

    public void addEntities(Collection<? extends Entity> entities) {
        newEntities.addAll(entities);
    }

    public void addEntity(Entity entity) {
        assert (!entityList.contains(entity)) : entity;
        assert (!newEntities.contains(entity)) : entity;

        newEntities.add(entity);
    }

    /**
     * calculates the entity hit by the given ray
     * @param origin the origin of the ray
     * @param dir    the direction of the ray
     * @return the first entity hit by the ray, or null if no entity is hit.
     */
    public Entity rayTrace(Vector3fc origin, Vector3fc dir) {
        if (entityList.isEmpty()) return null;
        assert testInvariants();

        RayAabIntersection sect = new RayAabIntersection(origin.x(), origin.y(), origin.z(), dir.x(), dir.y(), dir.z());

        float fraction = Float.MAX_VALUE;
        Entity suspect = null;

        for (CollisionEntity elt : entityArray()) {
            boolean collide = sect.test(
                    elt.xLower(), elt.yLower(), elt.zLower(),
                    elt.xUpper(), elt.yUpper(), elt.zUpper()
            );
            if (!collide) continue;

            Entity entity = elt.entity;

            float f = entity.hitbox().intersectMovement(origin, dir);
            if (f < fraction) {
                fraction = f;
                suspect = entity;
            }
        }

        return suspect;
    }

    private void mergeNewEntities(Collection<Entity> newEntities) {
        int nOfNewEntities = newEntities.size();
        if (nOfNewEntities <= 0) return;

        CollisionEntity[] newXSort = new CollisionEntity[nOfNewEntities];
        CollisionEntity[] newYSort = new CollisionEntity[nOfNewEntities];
        CollisionEntity[] newZSort = new CollisionEntity[nOfNewEntities];

        populate(newEntities, newXSort, newYSort, newZSort);

        xLowerSorted = Toolbox.mergeArrays(xLowerSorted, newXSort, CollisionEntity::xLower);
        yLowerSorted = Toolbox.mergeArrays(yLowerSorted, newYSort, CollisionEntity::yLower);
        zLowerSorted = Toolbox.mergeArrays(zLowerSorted, newZSort, CollisionEntity::zLower);
    }

    /**
     * Remove the selected entities off the entity lists in a robust way. Entities that did not exist are ignored, and
     * doubles are also accepted.
     * @param targets a collection of entities to be removed
     */
    private void deleteEntities(Collection<Entity> targets) {
        xLowerSorted = deleteAll(targets, xLowerSorted);
        yLowerSorted = deleteAll(targets, yLowerSorted);
        zLowerSorted = deleteAll(targets, zLowerSorted);

        entityList.removeAll(targets);
    }

    private CollisionEntity[] deleteAll(Collection<Entity> targets, CollisionEntity[] array) {
        int xi = 0;
        for (int i = 0; i < array.length; i++) {
            Entity entity = array[i].entity;
            if ((entity != null) && targets.contains(entity)) {
                continue;
            }
            array[xi++] = array[i];
        }
        return Arrays.copyOf(array, xi);
    }

    /**
     * @return an array of the entities, backed by any local representation. Should only be used for querying, otherwise
     * it must be cloned
     */
    private CollisionEntity[] entityArray() {
        return xLowerSorted;
    }

    public Collection<Entity> getEntityList() {
        Collection<Entity> list = new ArrayList<>(entityList);
        list.addAll(newEntities);
        return list;
    }

    public boolean contains(Entity entity) {
        return entityList.contains(entity) || newEntities.contains(entity);
    }

    public void forEach(Consumer<Entity> action) {
        for (Entity e : entityList) {
            action.accept(e);
        }
        for (Entity e : newEntities) {
            action.accept(e);
        }
    }

    public void cleanup() {
        xLowerSorted = new CollisionEntity[0];
        yLowerSorted = new CollisionEntity[0];
        zLowerSorted = new CollisionEntity[0];

        for (Entity e : entityList) {
            e.dispose();
        }
        for (Entity e : newEntities) {
            e.dispose();
        }

        entityList.clear();
        newEntities.clear();
    }


    protected class CollisionEntity {
        public final Entity entity;
        public int id;

        private BoundingBox range;
        private float x;
        private float y;
        private float z;

        public CollisionEntity(Entity source) {
            this.entity = source;
            this.range = entity.hitbox();
        }

        public void update(float time) {
            Vector3fc middle = entity.getPositionAt(time);
            x = middle.x();
            y = middle.y();
            z = middle.z();
        }

        public void setId(int id) {
            this.id = id;
        }

        public float xUpper() {
            return x + range.maxX;
        }

        public float yUpper() {
            return y + range.maxY;
        }

        public float zUpper() {
            return z + range.maxZ;
        }

        public float xLower() {
            return x - range.minX;
        }

        public float yLower() {
            return y - range.minY;
        }

        public float zLower() {
            return z - range.minZ;
        }

        @Override
        public String toString() {
            return entity.toString();
        }
    }

    /**
     * tracks how often pairs of integers are added, and allows querying whether a given pair has been added at least a
     * given number of times.
     */
    private class AdjacencyMatrix {
        // this matrix is indexed using the entity id values, with i > j
        // if (adjacencyMatrix[i][j] == n) then entityArray[i] and entityArray[j] have n coordinates with coinciding intervals
        Map<Integer, Map<Integer, Integer>> relations;
        Map<Integer, Set<Integer>> found;
        private int depth;

        public AdjacencyMatrix(int nrOfElements, int depth) {
            relations = new HashMap<>(nrOfElements);
            found = new HashMap<>();
            this.depth = depth;
        }

        public void add(int i, int j) {
            if (j > i) {
                int t = i;
                i = j;
                j = t;
            }

            Map<Integer, Integer> firstSide = relations.computeIfAbsent(i, HashMap::new);
            int newValue = firstSide.getOrDefault(j, 0) + 1;
            if (newValue == depth) {
                found.computeIfAbsent(i, HashSet::new).add(j);
            }
            firstSide.put(j, newValue);
        }

        public boolean has(int i, int j) {
            return found.containsKey(i) && found.get(i).contains(j);
        }

        public void forEach(BiConsumer<Integer, Integer> action) {
            for (Integer i : found.keySet()) {
                for (Integer j : found.get(i)) {
                    action.accept(i, j);
                }
            }
        }

        public int nrOfFoundElements() {
            int count = 0;
            for (Set<Integer> integers : found.values()) {
                count += integers.size();
            }
            return count;
        }
    }
}
