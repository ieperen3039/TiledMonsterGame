package NG.CollisionDetection;

import NG.DataStructures.Generic.AveragingQueue;
import NG.DataStructures.Generic.Pair;
import NG.DataStructures.Generic.PairList;
import NG.Entities.Entity;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.RayAabIntersection;
import org.joml.Vector3fc;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Geert van Ieperen created on 10-3-2018.
 */
public class CollisionDetection {
    private static final int MAX_COLLISION_ITERATIONS = 5;
    private static final int INSERTION_SORT_BOUND = 64;

    private final Collection<Entity> newEntities = new ArrayList<>();

    private CollisionEntity[] xLowerSorted;
    private CollisionEntity[] yLowerSorted;
    private CollisionEntity[] zLowerSorted;

    private AveragingQueue avgCollisions;

    private float previousTime;
    private WorldCollisionObject world;

    /**
     * @see #CollisionDetection(Collection, float)
     */
    public CollisionDetection() {
        this(Collections.emptyList(), 0);
    }

    /**
     * Collects the given entities and allows collision and phisics calculations to influence these entities
     * @param staticEntities a list of fixed entities. Entities in this collection should not move, but if they do,
     *                       dynamic objects might phase through when CollisionEntity in opposite direction. Apart from
     *                       this case, the collision detection still functions.
     * @param gameTime
     */
    public CollisionDetection(Collection<Entity> staticEntities, float gameTime) {
        avgCollisions = new AveragingQueue(5);
        Logger.printOnline(() ->
                String.format("Collision pair count average: %1.01f", avgCollisions.average())
        );

        int nOfEntities = staticEntities.size();
        xLowerSorted = new CollisionEntity[nOfEntities];
        yLowerSorted = new CollisionEntity[nOfEntities];
        zLowerSorted = new CollisionEntity[nOfEntities];

        populate(staticEntities, gameTime, xLowerSorted, yLowerSorted, zLowerSorted);
    }

    /**
     * populates the given arrays, and sorts the arrays on the lower coordinate of the hitboxes (x, y and z
     * respectively)
     */
    private void populate(
            Collection<? extends Entity> entities, float gameTime,
            CollisionEntity[] xLowerSorted, CollisionEntity[] yLowerSorted, CollisionEntity[] zLowerSorted
    ) {
        int i = 0;
        for (Entity entity : entities) {
            assert entity != null;
            CollisionEntity asCollisionEntity = CollisionEntity.getInstance(entity, gameTime);
            xLowerSorted[i] = asCollisionEntity;
            yLowerSorted[i] = asCollisionEntity;
            zLowerSorted[i] = asCollisionEntity;
            i++;
        }

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

    /**
     * @param gameTime the time of the next game-tick
     */
    public void processCollisions(float gameTime) {

        /* -- clean and restore invariants -- */

        // remove despawned entities
        List<Entity> removals = new ArrayList<>();

        for (CollisionEntity e : entityArray()) {
            Entity entity = e.entity();
            if (entity.isDespawnedAt(gameTime)) {
                removals.add(entity);
            }
        }

        if (!removals.isEmpty()) {
            deleteEntities(removals);
            removals.clear();
        }

        // add new entities
        synchronized (newEntities) {
            // collect all elements that have spawned
            List<Entity> additions = new ArrayList<>();
            for (Entity e : newEntities) {
                if (e.getSpawnTime() < gameTime) {
                    additions.add(e);
                }
            }

            mergeNewEntities(additions, gameTime);
            newEntities.removeAll(additions);
        }

        // update representation
        for (CollisionEntity entity : entityArray()) {
            entity.update(gameTime);
        }

        // update sorted lists
        Toolbox.insertionSort(xLowerSorted, CollisionEntity::xLower);
        Toolbox.insertionSort(yLowerSorted, CollisionEntity::yLower);
        Toolbox.insertionSort(zLowerSorted, CollisionEntity::zLower);

        /* -- analyse the collisions -- */

        // world collisions
        for (CollisionEntity e : entityArray()) {
            if (world.checkCollision(e.entity(), previousTime, gameTime)) {
                e.refresh(gameTime);
            }
        }

        /* As a single collision may result in a previously not-intersecting pair to collide,
         * we shouldn't re-use the getIntersectingPairs method nor reduce by non-collisions.
         * On the other hand, we may assume collisions of that magnitude appear seldom
         */
        PairList<CollisionEntity, CollisionEntity> pairs = getIntersectingPairs();

        int i;
        // for each pair, run checkCollisionPair(a, b, gameTime) until it returns false
        for (i = 0; i < MAX_COLLISION_ITERATIONS && !pairs.isEmpty(); i++) {

            PairList<CollisionEntity, CollisionEntity> newPairs = new PairList<>();
            for (Pair<CollisionEntity, CollisionEntity> p : pairs) {
                if (checkCollisionPair(p.left, p.right, gameTime)) {
                    newPairs.add(p);
                }
            }

            pairs = newPairs;
        }
        Logger.INFO.printSpamless("CollDet" + i, gameTime, "Collision iterations", i);

        previousTime = gameTime;
    }

    public void setWorld(WorldCollisionObject world) {
        this.world = world;
    }

    /**
     * checks and processes collisions between alpha and beta
     * @param alpha    one entity
     * @param beta     another entity
     * @param gameTime the time of the next game-tick
     * @return true iff these pairs indeed collided before endTime and a collision has been processed
     */
    private boolean checkCollisionPair(CollisionEntity alpha, CollisionEntity beta, float gameTime) {
        Entity aEty = alpha.entity();
        Entity bEty = beta.entity();

        assert aEty.canCollideWith(bEty) && bEty.canCollideWith(aEty);

        float bFrac = alpha.checkAtoB(bEty, gameTime);
        float aFrac = beta.checkAtoB(aEty, gameTime);

        float hitFrac = Math.min(aFrac, bFrac);
        if (hitFrac == 1) return false;

        float collisionTime = (float) (previousTime + (double) hitFrac * (previousTime - gameTime));

        /*
         Note: if en entity collides with many entities in one tick, it will affect and be affected by all the
         entities it would collide with, even if the first deflects it. A solution is complex and expensive.
         */
        aEty.collideWith(bEty, collisionTime);
        alpha.refresh(gameTime);
        if (world.checkCollision(aEty, previousTime, gameTime)) {
            alpha.refresh(gameTime);
        }

        bEty.collideWith(aEty, collisionTime);
        beta.refresh(gameTime);
        if (world.checkCollision(bEty, previousTime, gameTime)) {
            beta.refresh(gameTime);
        }

        return true;
    }

    /**
     * generate a list (possibly empty) of all pairs of objects that may have collided. This can include (parts of) the
     * ground, but not an object with itself. One pair does not occur the other way around.
     * @return a collection of pairs of objects that are close to each other
     */
    private PairList<CollisionEntity, CollisionEntity> getIntersectingPairs() {
        assert testInvariants();

        CollisionEntity[] entityArray = entityArray();
        int nrOfEntities = entityArray.length;

        // initialize id values to correspond to the array
        for (int i = 0; i < nrOfEntities; i++) {
            entityArray[i].setID(i);
        }

        AdjacencyMatrix adjacencies = new AdjacencyMatrix(nrOfEntities, 3);

        checkOverlap(adjacencies, xLowerSorted, CollisionEntity::xLower, CollisionEntity::xUpper);
        checkOverlap(adjacencies, yLowerSorted, CollisionEntity::yLower, CollisionEntity::yUpper);
        checkOverlap(adjacencies, zLowerSorted, CollisionEntity::zLower, CollisionEntity::zUpper);

        int nrOfElts = adjacencies.nrOfFoundElements();

        PairList<CollisionEntity, CollisionEntity> allEntityPairs = new PairList<>(nrOfElts);
        adjacencies.forEach((i, j) -> allEntityPairs.add(
                entityArray[i], entityArray[j]
        ));

        avgCollisions.add(nrOfElts);
        return allEntityPairs;
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
                Entity a = subject.entity();
                Entity b = target.entity();
                if (a.canCollideWith(b) && b.canCollideWith(a)) {
                    adjacencyMatrix.add(target.getID(), subject.getID());
                }

                if (j == nOfItems) break;
                target = sortedArray[j++];
            }
        }
    }

    public void addEntities(Collection<Entity> entities) {
        synchronized (newEntities) {
            assert entities.stream().noneMatch(this::contains);

            newEntities.addAll(entities);
        }
    }

    public void addEntity(Entity entity) {
        synchronized (newEntities) {
            assert !contains(entity);

            newEntities.add(entity);
        }
    }

    /**
     * calculates the first entity hit by the given ray
     * @param origin   the origin of the ray
     * @param dir      the direction of the ray
     * @param gameTime
     * @return Left: the first entity hit by the ray, or null if no entity is hit.
     * <p>
     * Right: the fraction t such that {@code origin + t * dir} gives the point of collision with this entity. Undefined
     * if {@code left == null}
     */
    public Pair<Entity, Float> rayTrace(Vector3fc origin, Vector3fc dir, float gameTime) {
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

            Entity entity = elt.entity();

            float f = entity.getHitbox(gameTime).intersectRay(origin, dir);
            if (f < fraction) {
                fraction = f;
                suspect = entity;
            }
        }

        return new Pair<>(suspect, fraction);
    }

    private void mergeNewEntities(Collection<? extends Entity> newEntities, float gameTime) {
        int nOfNewEntities = newEntities.size();
        if (nOfNewEntities <= 0) return;

        CollisionEntity[] newXSort = new CollisionEntity[nOfNewEntities];
        CollisionEntity[] newYSort = new CollisionEntity[nOfNewEntities];
        CollisionEntity[] newZSort = new CollisionEntity[nOfNewEntities];

        populate(newEntities, gameTime, newXSort, newYSort, newZSort);

        xLowerSorted = Toolbox.mergeArrays(xLowerSorted, newXSort, CollisionEntity::xLower);
        yLowerSorted = Toolbox.mergeArrays(yLowerSorted, newYSort, CollisionEntity::yLower);
        zLowerSorted = Toolbox.mergeArrays(zLowerSorted, newZSort, CollisionEntity::zLower);
    }

    /**
     * Remove the selected entities off the entity lists in a robust way. Entities that did not exist are ignored, and
     * doubles are accepted.
     * @param targets a collection of entities to be removed
     */
    private void deleteEntities(Collection<Entity> targets) {
        xLowerSorted = deleteAll(targets, xLowerSorted);
        yLowerSorted = deleteAll(targets, yLowerSorted);
        zLowerSorted = deleteAll(targets, zLowerSorted);
    }

    private CollisionEntity[] deleteAll(Collection<Entity> targets, CollisionEntity[] array) {
        int xi = 0;
        for (int i = 0; i < array.length; i++) {
            Entity entity = array[i].entity();
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
        CollisionEntity[] eties = entityArray();
        ArrayList<Entity> elts = new ArrayList<>(eties.length);

        for (CollisionEntity ety : eties) {
            elts.add(ety.entity());
        }

        return elts;
    }

    public boolean contains(Entity entity) {
        if (newEntities.contains(entity)) return true;

        for (CollisionEntity ety : entityArray()) {
            if (ety.entity().equals(entity)) return true;
        }

        return false;
    }

    public void forEach(Consumer<Entity> action) {
        for (CollisionEntity ety : entityArray()) {
            action.accept(ety.entity());
        }
    }

    public synchronized void cleanup() {
        xLowerSorted = new CollisionEntity[0];
        yLowerSorted = new CollisionEntity[0];
        zLowerSorted = new CollisionEntity[0];

        synchronized (newEntities) {
            newEntities.clear();
        }
    }

    /**
     * tracks how often pairs of integers are added, and allows querying whether a given pair has been added at least a
     * given number of times.
     */
    private static class AdjacencyMatrix {
        // this map is indexed using the entity id values, with i > j
        // if (adjacencyMatrix[i][j] == n) then entityArray[i] and entityArray[j] have n coordinates with coinciding intervals
        Map<Integer, Map<Integer, Integer>> relations;
        Map<Integer, Set<Integer>> found;
        private int depth;

        /**
         * @param nrOfElements maximum value that can be added
         * @param depth        how many times a number must be added to trigger {@link #has(int, int)}. For 3-coordinate
         *                     matching, use 3
         */
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

    /**
     * an interface for checking masses of entities against.
     */
    public interface WorldCollisionObject {
        /**
         * Calculates at what moment of the given interval there is a collision between the entity and this object.
         * @param e         the entity to check
         * @param startTime the begin of the interval, in seconds game time
         * @param endTime   the end of the interval, in seconds game time
         * @return true iff there was a collision
         */
        boolean checkCollision(Entity e, float startTime, float endTime);
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
            allEntities.add(colEty.entity());
        }

        for (CollisionEntity collEty : xLowerSorted) {
            if (!allEntities.contains(collEty.entity())) {
                throw new IllegalStateException("Array x does not contain entity " + collEty.entity());
            }
        }
        for (CollisionEntity collEty : yLowerSorted) {
            if (!allEntities.contains(collEty.entity())) {
                throw new IllegalStateException("Array y does not contain entity " + collEty.entity());
            }
        }
        for (CollisionEntity collEty : zLowerSorted) {
            if (!allEntities.contains(collEty.entity())) {
                throw new IllegalStateException("Array z does not contain entity " + collEty.entity());
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
        float init = Float.NEGATIVE_INFINITY;
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
        init = Float.NEGATIVE_INFINITY;
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
        init = Float.NEGATIVE_INFINITY;
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
}
