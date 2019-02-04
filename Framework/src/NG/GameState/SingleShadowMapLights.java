package NG.GameState;

import NG.Camera.Camera;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.Game;
import NG.Rendering.DirectionalLight;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.PointLight;
import NG.Rendering.Shaders.DepthShader;
import NG.Rendering.Shaders.LightShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Tools.Toolbox;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Geert van Ieperen created on 3-2-2019.
 */
public class SingleShadowMapLights implements GameLights {
    private static final int UPDATE_MARGIN = 10;

    private final Lock pointLightEditLock;
    private final Lock pointLightReadLock;

    private final List<PointLight> lights;
    private DirectionalLight sunLight;

    private Game game;
    private DepthShader shadowShader;

    private float lightDist = 1;
    private boolean shadowMapIsDirty = false;

    public SingleShadowMapLights() {
        ReadWriteLock rwl = new ReentrantReadWriteLock(false);
        this.pointLightEditLock = rwl.writeLock();
        this.pointLightReadLock = rwl.readLock();

        this.lights = new ArrayList<>();
        this.sunLight = new DirectionalLight(Color4f.WHITE, new Vector3f(1, -1, 1), 0.5f);
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
        this.sunLight.init(game);
        this.shadowShader = new DepthShader();
        game.map().addChangeListener(() -> shadowMapIsDirty = true);
    }

    @Override
    public void addPointLight(PointLight light) {
        pointLightEditLock.lock();
        try {
            lights.add(light);
        } finally {
            pointLightEditLock.unlock();
        }
    }

    @Override
    public void addDirectionalLight(Vector3fc origin, Color4f color, float intensity) {
        sunLight.setDirection(origin);
        sunLight.setColor(color);
        sunLight.setIntensity(intensity);
    }

    @Override
    public void renderShadowMaps() {
        Camera camera = game.camera();
        Vector3fc playerFocus = camera.getFocus();
        float viewDist = camera.vectorToFocus().length();
        Vector3fc lightFocus = sunLight.getLightFocus();

        if (playerFocus.distanceSquared(lightFocus) > UPDATE_MARGIN * UPDATE_MARGIN) {
            sunLight.setLightFocus(playerFocus);

            shadowMapIsDirty = true;

        } else if (Math.abs(viewDist - lightDist) > UPDATE_MARGIN) {
            float lightCubeSize = 10 + 2 * viewDist + UPDATE_MARGIN;
            sunLight.setLightSize(lightCubeSize);

            shadowMapIsDirty = true;
            lightDist = viewDist;
        }

        if (sunLight.getDynamicShadowMap().getResolution() > 0) {
            // shadow render
            shadowShader.bind();
            {
                shadowShader.initialize(game);

                if (shadowMapIsDirty) {
                    DepthShader.DepthGL gl = shadowShader.getGL(false);
                    shadowShader.setDirectionalLight(sunLight);
                    game.map().draw(gl);

                    gl.cleanup();
                }

                glCullFace(GL_FRONT);
                DepthShader.DepthGL gl = shadowShader.getGL(true);
                shadowShader.setDirectionalLight(sunLight);
                game.state().drawEntities(gl);
                glCullFace(GL_BACK);

                gl.cleanup();
            }
            shadowShader.unbind();

            Toolbox.checkGLError();
        }
    }

    @Override
    public void drawLights(SGL gl) {
        ShaderProgram shader = gl.getShader();
        if (shader instanceof LightShader) {
            LightShader lightShader = (LightShader) shader;

            pointLightReadLock.lock();
            try {
                for (PointLight light : lights) {
                    Vector3fc mPosition = gl.getPosition(light.position);
                    lightShader.setPointLight(mPosition, light.color, light.intensity);
                }
            } finally {
                pointLightReadLock.unlock();
            }

            lightShader.setDirectionalLight(sunLight);
        }
    }

    @Override
    public void cleanup() {

    }
}
