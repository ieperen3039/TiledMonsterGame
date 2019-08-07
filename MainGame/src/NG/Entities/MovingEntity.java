package NG.Entities;

/**
 * An entity is anything that is in the world, excluding the ground itself. Particles and other purely visual elements.
 * @author Geert van Ieperen. Created on 14-9-2018.
 */
public interface MovingEntity extends Entity {

    @Override
    default boolean canCollideWith(Entity other) {
        return other != this;
    }
}
