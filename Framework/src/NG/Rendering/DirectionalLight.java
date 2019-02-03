/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NG.Rendering;


import NG.Camera.Camera;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.Game;
import NG.Engine.GameAspect;
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
public class DirectionalLight implements GameAspect {
    private static final int UPDATE_MARGIN = 10;
    private static final float LIGHT_Z_NEAR = 0.5f;
    private Color4f color;
    private Vector3fc direction;
    private float intensity;

    // Shadows related
    private ShadowMap staticShadowMap, dynamicShadowMap;
    private Matrix4f ortho = new Matrix4f();
    private Matrix4f lightSpaceMatrix;

    private boolean doShadowMapping;
    private static final float LIGHT_SIZE = 100.0f;
    private float lightCubeSize;
    private Game game;
    private Vector3fc lightFocus = new Vector3f();
    private float lightDist = 1;

    public DirectionalLight(
            Color4f color, Vector3fc direction, float intensity
    ) {
        this.color = color;
        this.direction = new Vector3f(direction);
        this.intensity = intensity;
    }

    @Override
    public void init(Game game) throws Exception {
        int resolution = game.settings().SHADOW_RESOLUTION;
        doShadowMapping = resolution > 0;
        this.game = game;

        if (doShadowMapping) {
            staticShadowMap = new ShadowMap(resolution);
            dynamicShadowMap = new ShadowMap(resolution);
            staticShadowMap.init();
            dynamicShadowMap.init();
        }

        setLightSize(LIGHT_SIZE);
    }

    private void setLightSize(float lightSize) {
        lightCubeSize = lightSize;

        float zFar = LIGHT_Z_NEAR + lightSize * 2;
        ortho.setOrtho(-lightSize, lightSize, -lightSize, lightSize, LIGHT_Z_NEAR, zFar);

        lightSpaceMatrix = getLightSpace();
    }

    public Vector3fc getDirection() {
        return direction;
    }

    public void setDirection(Vector3fc direction) {
        this.direction = direction;

        lightSpaceMatrix = getLightSpace();
    }

    private Matrix4f getLightSpace() {
        Vector3f vecToLight = new Vector3f(direction);
        vecToLight.normalize(lightCubeSize + LIGHT_Z_NEAR);

        Vector3fc playerFocus = game.camera().getFocus();
        vecToLight.add(playerFocus);

        Matrix4f lightView = new Matrix4f().setLookAt(vecToLight, playerFocus, Vectors.zVector());

        return new Matrix4f(ortho).mul(lightView);
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public Matrix4f getLightSpaceMatrix() {
        Camera camera = game.camera();
        Vector3fc playerFocus = camera.getFocus();
        float viewDist = camera.vectorToFocus().length();

        if (playerFocus.distanceSquared(lightFocus) > UPDATE_MARGIN * UPDATE_MARGIN) {
            lightFocus = new Vector3f(playerFocus);
            lightDist = viewDist;
            lightSpaceMatrix = getLightSpace();

        } else if (Math.abs(viewDist - lightDist) > UPDATE_MARGIN) {
            lightFocus = new Vector3f(playerFocus);
            lightDist = viewDist;

            float lightCubeSize = 10 + 2 * viewDist + UPDATE_MARGIN;
            setLightSize(lightCubeSize);
        }

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

    public boolean doShadowMapping() {
        return doShadowMapping;
    }
}
