package NG.Rendering;

import NG.DataStructures.Generic.Color4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * a light object, which can be either a point-light or an infinitely far light
 * @author Geert van Ieperen. Created on 18-11-2018.
 */
public class PointLight {
    public final Vector3fc position;
    public final Color4f color;
    public final float intensity;

    public PointLight(float x, float y, float z, Color4f color, float intensity) {
        this(new Vector3f(x, y, z), color, intensity);
    }

    public PointLight(Vector3fc position, Color4f color, float intensity) {
        this.position = new Vector3f(position);
        this.color = color; // TODO fix brightness
        this.intensity = intensity;
    }
}
