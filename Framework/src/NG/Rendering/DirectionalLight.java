/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NG.Rendering;


import NG.DataStructures.Generic.Color4f;
import NG.Engine.Game;
import NG.Rendering.Shaders.ShaderException;
import NG.Rendering.Shaders.ShadowMap;
import NG.Tools.Vectors;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * A light source that is infinitely far away. Manages shadow mappings and light properties.
 * @author Dungeons-and-Drawings group
 * @author Geert van Ieperen
 */
public class DirectionalLight {
    private static final float LIGHT_Z_NEAR = 0.5f;
    private Color4f color;
    private Vector3fc direction;
    private float intensity;

    // Shadows related
    private ShadowMap staticShadowMap, dynamicShadowMap;
    private Matrix4f ortho = new Matrix4f();
    private Matrix4f lightSpaceMatrix = new Matrix4f();

    private static final float LIGHT_SIZE = 100.0f;
    private float lightCubeSize;
    private Vector3fc lightFocus = new Vector3f();

    public DirectionalLight(Color4f color, Vector3fc direction, float intensity) {
        this.color = color;
        this.direction = new Vector3f(direction);
        this.intensity = intensity;
    }

    public void init(Game game) throws ShaderException {
        int stRes = game.settings().STATIC_SHADOW_RESOLUTION;
        int dyRes = game.settings().DYNAMIC_SHADOW_RESOLUTION;

        if (stRes > 0) {
            staticShadowMap = new ShadowMap(stRes);
            staticShadowMap.init();
        }
        if (dyRes > 0) {
            dynamicShadowMap = new ShadowMap(dyRes);
            dynamicShadowMap.init();
        }

        setLightSize(LIGHT_SIZE);
    }

    public void setLightSize(float lightSize) {
        lightCubeSize = lightSize;

        float zFar = LIGHT_Z_NEAR + lightSize * 2;
        ortho.setOrtho(-lightSize, lightSize, -lightSize, lightSize, LIGHT_Z_NEAR, zFar);

        lightSpaceMatrix = recalculateLightSpace();
    }

    public Vector3fc getDirection() {
        return direction;
    }

    public void setDirection(Vector3fc direction) {
        this.direction = direction;

        lightSpaceMatrix = recalculateLightSpace();
    }

    private Matrix4f recalculateLightSpace() {
        if (staticShadowMap == null && dynamicShadowMap == null) {
            return lightSpaceMatrix;
        }

        Vector3f vecToLight = new Vector3f(direction);
        vecToLight.normalize(lightCubeSize + LIGHT_Z_NEAR);

        vecToLight.add(lightFocus);

        Matrix4f lightView = new Matrix4f().setLookAt(vecToLight, lightFocus, Vectors.zVector());

        return new Matrix4f(ortho).mul(lightView);
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public Matrix4f getLightSpaceMatrix() {
        return lightSpaceMatrix;
    }

    public ShadowMap getStaticShadowMap() {
        return staticShadowMap;
    }

    public ShadowMap getDynamicShadowMap() {
        return dynamicShadowMap;
    }

    public Color4f getColor() {
        return color;
    }

    public void setColor(Color4f color) {
        this.color = color;
    }

    /**
     * Cleanup memory
     */
    public void cleanup() {
        staticShadowMap.cleanup();
        dynamicShadowMap.cleanup();
    }

    public boolean doShadowMapping(boolean dynamic) {
        return dynamic ? dynamicShadowMap != null : staticShadowMap != null;
    }

    public Vector3fc getLightFocus() {
        return lightFocus;
    }

    public void setLightFocus(Vector3fc lightFocus) {
        this.lightFocus = lightFocus;
        lightSpaceMatrix = recalculateLightSpace();
    }
}
