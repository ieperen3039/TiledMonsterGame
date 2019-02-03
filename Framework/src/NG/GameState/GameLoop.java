package NG.GameState;

import NG.ActionHandling.ClickShader;
import NG.ActionHandling.MouseTools.MouseTool;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.AbstractGameLoop;
import NG.Engine.Game;
import NG.Entities.Entity;
import NG.Rendering.DirectionalLight;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.PointLight;
import NG.Rendering.Shaders.LightShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Rendering.Shapes.Primitives.Collision;
import NG.Rendering.Textures.Texture;
import NG.Tools.Toolbox;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A collection of entities, which manages synchronous updating and drawing.
 * @author Geert van Ieperen. Created on 14-9-2018.
 */
public class GameLoop extends AbstractGameLoop implements GameState {
    private final List<Entity> entities;
    private final List<PointLight> lights;
    private final Lock entityWriteLock;
    private final Lock entityReadLock;

    private DirectionalLight sunLight;
    private Deque<Runnable> postUpdateActionQueue;
    private Game game;

    public GameLoop(String gameName, int targetTps) {
        super("Gameloop " + gameName, targetTps);

        this.entities = new ArrayList<>();
        this.lights = new ArrayList<>();
        this.postUpdateActionQueue = new ConcurrentLinkedDeque<>();
        ReadWriteLock rwl = new ReentrantReadWriteLock(false);
        this.entityWriteLock = rwl.writeLock();
        this.entityReadLock = rwl.readLock();
        sunLight = new DirectionalLight(Color4f.WHITE, new Vector3f(1, -1, 1), 0.5f);
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
        sunLight.init(game);
    }

    /**
     * Adds an entity to the gameloop in a synchronized fashion.
     * @param entity the new entity, with only its constructor called
     * @see #defer(Runnable)
     */
    @Override
    public void addEntity(Entity entity) { // TODO group new entities like has been done in JetFighterGame
        // Thanks to the reentrant mechanism, this may also be executed by a deferred action.
        defer(() -> {
            entityWriteLock.lock();
            try {
                entities.add(entity);
            } finally {
                entityWriteLock.unlock();
            }
        });
    }

    @Override
    public void addLight(PointLight light) {
        defer(() -> {
            entityWriteLock.lock();
            try {
                lights.add(light);
            } finally {
                entityWriteLock.unlock();
            }
        });
    }

    /**
     * updates the server state of all objects
     */
    public void update(float deltaTime) {
        runPostUpdateActions();
        runCleaning();

        entities.forEach(Entity::update);

        runPostUpdateActions();
    }

    @Override
    public void drawEntities(SGL gl) {
        entityReadLock.lock();
        try {
            Toolbox.drawAxisFrame(gl);
            for (Entity entity : entities) {
                entity.draw(gl);
            }

        } finally {
            entityReadLock.unlock();
        }
    }

    @Override
    public Collision getEntityCollision(Vector3fc from, Vector3fc to) {
        return null;
    }

    @Override
    public void setDirectionalLight(Vector3fc origin, Color4f color, float intensity) {
        sunLight.setDirection(origin);
        sunLight.setColor(color);
        sunLight.setIntensity(intensity);
    }

    @Override
    public void drawLights(SGL gl) {
        ShaderProgram shader = gl.getShader();
        if (shader instanceof LightShader) {
            LightShader lightShader = (LightShader) shader;

            for (PointLight light : lights) {
                Vector3fc mPosition = gl.getPosition(light.position);
                lightShader.setPointLight(mPosition, light.color, light.intensity);
            }

            lightShader.setDirectionalLight(sunLight);
        }
    }

    /** executes action after a gameloop completes */
    public void defer(Runnable action) {
        // TODO make a 'modifyingDefer'
        postUpdateActionQueue.offer(action);
    }

    /** execute all actions that have been deferred */
    private void runPostUpdateActions() {
        while (!postUpdateActionQueue.isEmpty()) {
            postUpdateActionQueue.remove().run();
        }
    }

    /** remove all entities from the entity list that have their doRemove flag true */
    private void runCleaning() {
        entityWriteLock.lock();
        try {
            entities.removeIf(Entity::isDisposed);
        } finally {
            entityWriteLock.unlock();
        }
    }

    @Override
    public boolean checkMouseClick(MouseTool tool, int xSc, int ySc) {
        Entity entity = ClickShader.getEntity(game, xSc, ySc);
        if (entity == null) return false;

        tool.apply(entity, xSc, ySc);
        return true;
    }

    @Override
    public Texture getStaticShadowMap() {
        return sunLight.getStaticShadowMap();
    }

    @Override
    public void cleanup() {
        entityWriteLock.lock();
        entities.clear();
        lights.clear();
        postUpdateActionQueue.clear();
        entityWriteLock.unlock();
    }

    @Override
    public void writeToFile(DataOutput out) {

    }

    @Override
    public void readFromFile(DataInput in) {

    }
}
