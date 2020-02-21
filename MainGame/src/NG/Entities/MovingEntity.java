package NG.Entities;

import NG.Actions.EntityAction;
import NG.DataStructures.Generic.Pair;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * An entity is anything that is in the world, excluding the ground itself. Particles and other purely visual elements.
 * @author Geert van Ieperen. Created on 14-9-2018.
 */
public interface MovingEntity extends Entity {

    @Override
    default boolean canCollideWith(Entity other) {
        return other != this;
    }

    @Override
    default Vector3f getPositionAt(float gameTime) {
        Pair<EntityAction, Float> action = getActionAt(gameTime);
        return action.left.getPositionAt(action.right);
    }

    Pair<EntityAction, Float> getActionAt(float gameTime);

    /**
     * returns the points of the shape of this entity at the given moment in time
     * @param gameTime the moment when to retrieve this entity's points
     * @return a list of the exact wolrd-positions of the vertices of the shape of this object. Changes in the list are
     * not reflected in this object.
     */
    default List<Vector3f> getShapePoints(float gameTime) {
        return getShapePoints(new ArrayList<>(), gameTime);
    }

    /**
     * returns the points of the shape of this entity at the given moment in time, and store the result in the vectors
     * of dest.
     * @param dest     a list of vectors. If the result requires more or less elements, this parameter may be ignored.
     * @param gameTime the moment when to retrieve this entity's points
     * @return a list of the exact world-positions of the vertices of the shape of this object. Changes in the list are
     * not reflected in this object.
     */
    default List<Vector3f> getShapePoints(List<Vector3f> dest, float gameTime) {
        if (dest.size() > 8) {
            dest.clear();
        }
        if (dest.size() < 8) {
            for (int i = dest.size(); i < 8; i++) {
                dest.add(new Vector3f());
            }
        }

        Iterable<Vector3f> corners = getHitbox(gameTime).corners();

        int i = 0;
        for (Vector3f corner : corners) {
            dest.get(i).set(corner);
            i++;
        }

        return dest;
    }
}
