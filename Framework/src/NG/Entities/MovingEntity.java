package NG.Entities;

import org.joml.Vector3fc;

/**
 * An entity of which its position is defined, and may change.
 * @author Geert van Ieperen. Created on 14-9-2018.
 */
public interface MovingEntity extends Entity {
    Vector3fc getPosition();

}
