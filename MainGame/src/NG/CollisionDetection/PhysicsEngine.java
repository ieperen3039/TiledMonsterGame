package NG.CollisionDetection;

import NG.Actions.EntityAction;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Generic.Pair;
import NG.Entities.Entity;
import NG.Entities.MovingEntity;
import NG.GameMap.GameMap;
import NG.InputHandling.ClickShader;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Rendering.MatrixStack.SGL;
import NG.Tools.SerializationTools;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;

/**
 * Doesn't actually calculate results of collisions. See {@link Entity#collideWith(Entity, float)}
 * @author Geert van Ieperen created on 10-2-2019.
 */
public class PhysicsEngine implements GameState, Externalizable {
    private final CollisionDetection entityList;
    private Game game;

    public PhysicsEngine() {
        entityList = new CollisionDetection();
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
        entityList.setWorld(this::entityWorldCollision);
    }

    private boolean entityWorldCollision(Entity entity, float startTime, float endTime) {
        if (!(entity instanceof MovingEntity)) return false;
        MovingEntity movingEntity = (MovingEntity) entity;

        float spawnTime = entity.getSpawnTime();
        float despawnTime = entity.getDespawnTime();
        if (spawnTime > endTime || despawnTime < startTime) return false;
        startTime = Math.max(startTime, spawnTime);
        endTime = Math.min(endTime, despawnTime);

        // this is the case when the entity does not exist between start and end
        if (startTime >= endTime) return false;

        // first check whether and where we consider collision
        Pair<EntityAction, Float> firstAction = movingEntity.getActionAt(startTime);
        Pair<EntityAction, Float> lastAction = movingEntity.getActionAt(endTime);
        EntityAction action;
        float actionStart;
        float actionEnd;

        GameMap map = game.get(GameMap.class);

        if (firstAction.left == lastAction.left) {
            if (!firstAction.left.hasWorldCollision()) return false;
            action = firstAction.left;
            actionStart = firstAction.right;
            actionEnd = firstAction.right + (endTime - startTime);

        } else {
            // we assume there is no action inbetween
            boolean firstHasColl = firstAction.left.hasWorldCollision();
            boolean lastHasColl = lastAction.left.hasWorldCollision();

            if (!firstHasColl && !lastHasColl) {
                return false;

            } else if (!firstHasColl) { // lastHasColl
                action = lastAction.left;
                actionStart = 0;
                actionEnd = lastAction.right;

            } else if (!lastHasColl) { // firstHasColl
                action = firstAction.left;
                actionStart = firstAction.right;
                actionEnd = (endTime - startTime) - lastAction.right;

            } else { // firstHasColl && lastHasColl
                float relativeEnd = (endTime - startTime) - lastAction.right;
                Float collisionTime = map.getActionCollision(firstAction.left, firstAction.right, relativeEnd);
                if (collisionTime != null) {
                    entity.collideWith(map, startTime - firstAction.right + collisionTime);
                    return true;

                } else {
                    action = lastAction.left;
                    actionStart = 0;
                    actionEnd = lastAction.right;
                }
            }
        }

        if (!(actionStart < actionEnd)) {
            throw new AssertionError();
        }

        Float collisionTime = map.getActionCollision(action, actionStart, actionEnd);
        if (collisionTime != null) {
            entity.collideWith(map, startTime - actionStart + collisionTime);
            return true;
        }

        return false;
    }

    @Override
    public void update(float gameTime) {
        entityList.processCollisions(gameTime);
        entityList.forEach(entity -> entity.update(gameTime));
    }

    @Override
    public void addEntity(MovingEntity entity) {
        assert !entityList.contains(entity) : "duplicate entity";
        entityList.addEntity(entity);
    }

    @Override
    public void draw(SGL gl) {
        entityList.forEach(e -> e.draw(gl));
    }

    @Override
    public Pair<Entity, Float> getEntityByRay(Vector3fc origin, Vector3fc dir, float gameTime) {
        return entityList.rayTrace(origin, dir, gameTime);
    }

    @Override
    public Collection<Entity> entities() {
        return entityList.getEntityList();
    }

    @Override
    public boolean checkMouseClick(MouseTool tool, int xSc, int ySc) {
        Entity entity;

        if (game.has(ClickShader.class)) {
            entity = game.get(ClickShader.class).getEntity(game, xSc, ySc);

        } else {
            Vector3f origin = new Vector3f();
            Vector3f direction = new Vector3f();
            Vectors.windowCoordToRay(game, xSc, ySc, origin, direction);

            float gameTime = game.get(GameTimer.class).getGametime();
            entity = entityList.rayTrace(origin, direction, gameTime).left;
        }

        if (entity == null) return false;

        tool.apply(entity, xSc, ySc);
        return true;
    }

    @Override
    public void cleanup() {
        entityList.cleanup();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Collection<Entity> box = entityList.getEntityList();

        out.writeInt(box.size());
        for (Object s : box) {
            SerializationTools.writeSafe(out, s);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            Entity entity = SerializationTools.readSafe(in, Entity.class);
            if (entity == null) continue;
            if (entity instanceof MovingEntity) {
                entityList.addEntity(entity);
            }
        }
    }
}
