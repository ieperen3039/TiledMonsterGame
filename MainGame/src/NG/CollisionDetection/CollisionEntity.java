package NG.CollisionDetection;

import NG.Entities.Entity;
import NG.Entities.MovingEntity;
import org.joml.AABBf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geert van Ieperen created on 21-2-2020.
 */
public interface CollisionEntity {

    static CollisionEntity getInstance(Entity target, float gameTime) {
        if (target instanceof MovingEntity) {
            return new Moving((MovingEntity) target, gameTime);
        } else {
            return new Static(target, gameTime);
        }
    }

    int getID();

    void setID(int id);

    Entity entity();

    void refresh(float gameTime);

    void update(float gameTime);

    float xUpper();

    float yUpper();

    float zUpper();

    float xLower();

    float yLower();

    float zLower();

    /**
     * checks whether this collides with {@code receiving} before gameTime
     * @param receiver another entity
     * @param gameTime
     * @return 1 if CollisionEntity does not hit the receiver, otherwise a value t [0 ... 1) such that {@code origin + t
     * * direction} lies on this
     */
    float checkAtoB(Entity receiver, float gameTime);

    /**
     * @author Geert van Ieperen created on 21-2-2020.
     */
    class Moving implements CollisionEntity {
        private final MovingEntity entity;
        private int id;

        private List<Vector3f> nextPoints;
        private List<Vector3f> prevPoints;

        private BoundingBox nextBoundingBox;
        private BoundingBox prevBoundingBox;
        private AABBf hitbox; // combined of both states

        public Moving(MovingEntity source, float gameTime) {
            this.entity = source;
            prevPoints = new ArrayList<>();
            update(gameTime);
        }

        @Override
        public void refresh(float gameTime) {
            nextPoints = entity.getShapePoints(nextPoints, gameTime);
            nextBoundingBox = entity.getHitbox(gameTime);
            hitbox = prevBoundingBox.union(nextBoundingBox);
        }

        @Override
        public void update(float gameTime) {
            entity.update(gameTime);

            if (nextBoundingBox == null) {
                nextPoints = entity.getShapePoints(gameTime);
                prevPoints = entity.getShapePoints(gameTime);

                nextBoundingBox = entity.getHitbox(gameTime);
                hitbox = nextBoundingBox;

            } else {
                List<Vector3f> buffer = prevPoints;

                prevPoints = nextPoints;
                nextPoints = entity.getShapePoints(buffer, gameTime);

                prevBoundingBox = nextBoundingBox;
                nextBoundingBox = entity.getHitbox(gameTime);

                hitbox = prevBoundingBox.union(nextBoundingBox);
            }
        }

        @Override
        public float xUpper() {
            return hitbox.maxX;
        }

        @Override
        public float yUpper() {
            return hitbox.maxY;
        }

        @Override
        public float zUpper() {
            return hitbox.maxZ;
        }

        @Override
        public float xLower() {
            return hitbox.minX;
        }

        @Override
        public float yLower() {
            return hitbox.minY;
        }

        @Override
        public float zLower() {
            return hitbox.minZ;
        }

        @Override
        public Entity entity() {
            return entity;
        }

        @Override
        public int getID() {
            return id;
        }

        @Override
        public void setID(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return entity.toString();
        }

        @Override
        public float checkAtoB(Entity receiver, float gameTime) {
            float bFrac = 1;

            for (int i = 0; i < prevPoints.size(); i++) {
                Vector3fc origin = nextPoints.get(i);
                Vector3fc target = prevPoints.get(i);
                Vector3f direction = new Vector3f(target).sub(origin);

                float intersection = receiver.getIntersection(origin, direction, gameTime);

                if (intersection < bFrac) {
                    bFrac = intersection;
                }
            }

            return bFrac;
        }
    }

    /**
     *
     */
    class Static implements CollisionEntity {
        private final Entity entity;
        private int id;
        private AABBf hitbox; // combined of both states

        public Static(Entity source, float gameTime) {
            this.entity = source;
            update(gameTime);
        }

        @Override
        public void refresh(float gameTime) {
        }

        @Override
        public void update(float gameTime) {
            entity.update(gameTime);
            hitbox = entity.getHitbox(gameTime);
        }

        @Override
        public float xUpper() {
            return hitbox.maxX;
        }

        @Override
        public float yUpper() {
            return hitbox.maxY;
        }

        @Override
        public float zUpper() {
            return hitbox.maxZ;
        }

        @Override
        public float xLower() {
            return hitbox.minX;
        }

        @Override
        public float yLower() {
            return hitbox.minY;
        }

        @Override
        public float zLower() {
            return hitbox.minZ;
        }

        @Override
        public Entity entity() {
            return entity;
        }

        @Override
        public int getID() {
            return id;
        }

        @Override
        public void setID(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return entity.toString();
        }

        /**
         * checks whether this collides with {@code receiving}
         * @param receiver another entity
         * @param gameTime
         * @return 1 if CollisionEntity does not hit the receiver, otherwise a value t [0 ... 1) such that {@code origin
         * + t * direction}
         */
        @Override
        public float checkAtoB(Entity receiver, float gameTime) {
            return 1;
        }
    }
}
