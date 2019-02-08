package NG.GameState;

import NG.ActionHandling.ClickShader;
import NG.ActionHandling.MouseTools.MouseTool;
import NG.DataStructures.Storable;
import NG.Engine.AbstractGameLoop;
import NG.Engine.Game;
import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shapes.Primitives.Collision;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.Vector3fc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A collection of entities, which manages synchronous updating and drawing.
 * @author Geert van Ieperen. Created on 14-9-2018.
 */
public class GameLoop extends AbstractGameLoop implements GameState {
    private final List<Entity> entities;
    private final Lock entityWriteLock;
    private final Lock entityReadLock;
    private Game game;

    public GameLoop(String gameName, int targetTps) {
        super("Gameloop " + gameName, targetTps);

        this.entities = new ArrayList<>();
        ReadWriteLock rwl = new ReentrantReadWriteLock(false);
        this.entityWriteLock = rwl.writeLock();
        this.entityReadLock = rwl.readLock();
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
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

    /**
     * updates the server state of all objects
     * @param deltaTime real time difference
     */
    public void update(float deltaTime) {
        runCleaning();

        game.timer().updateGameTime();
        float gameDT = game.timer().getGametimeDifference();
        if (gameDT == 0) return;

        entities.forEach(Entity::update);
    }

    @Override
    public void draw(SGL gl) {
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
        FutureTask<Entity> identifier = new FutureTask<>(() -> ClickShader.getEntity(game, xSc, ySc));
        game.executeOnRenderThread(identifier);

        try {
            Entity entity = identifier.get();
            if (entity == null) return false;

            tool.apply(entity, xSc, ySc);
            return true;

        } catch (InterruptedException | ExecutionException ex) {
            Logger.ERROR.print(ex);
            return false;
        }
    }

    @Override
    public void cleanup() {
        entityWriteLock.lock();
        entities.clear();
        entityWriteLock.unlock();
    }

    @Override
    public void writeToFile(DataOutput out) throws IOException {
        entityReadLock.lock();
        try {
            // properties of the list
            out.writeInt(entities.size());
            for (Entity e : entities) {
                if (e instanceof Storable) {
                    // class
                    Storable item = (Storable) e;
                    out.writeUTF(item.getClass().getCanonicalName());
                    // entity
                    item.writeToFile(out);
                }
            }

        } finally {
            entityReadLock.unlock();
        }
    }

    @Override
    public void readFromFile(DataInput in) throws IOException {
        entityWriteLock.lock();
        try {
            int size = in.readInt();
            entities.clear();
            for (int i = 0; i < size; i++) {
                String className = in.readUTF();
                Entity entity = getEntityInstance(className, game);
                ((Storable) entity).readFromFile(in);
            }
        } finally {
            entityWriteLock.unlock();
        }
    }

    private static Entity getEntityInstance(String className, Game game) throws IOException {
        try {
            Class<?> entityClass = Class.forName(className);

            if (!entityClass.isAssignableFrom(Entity.class)) {
                throw new IOException("Class " + className + " is not of type Entity");

            } else if (!entityClass.isAssignableFrom(Storable.class)) {
                throw new IOException("Class " + className + " is not of type Storable");
            }

            Constructor<?> constructor = entityClass.getConstructor(Game.class);
            return (Entity) constructor.newInstance(game);

        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            throw new IOException("Could not find class " + className, ex);

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new IOException("Could not initialize instance of type " + className, ex);

        }
    }
}
