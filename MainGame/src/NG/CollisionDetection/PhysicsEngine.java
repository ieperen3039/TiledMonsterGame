package NG.CollisionDetection;

import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Generic.Pair;
import NG.Entities.Entity;
import NG.Entities.MovingEntity;
import NG.GameMap.GameMap;
import NG.InputHandling.ClickShader;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Rendering.Lights.GameState;
import NG.Rendering.MatrixStack.SGL;
import NG.Settings.Settings;
import NG.Storable;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Geert van Ieperen created on 10-2-2019.
 */
public class PhysicsEngine implements GameState {
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

    private void entityWorldCollision(Entity entity, float startTime, float endTime) {
        GameMap map = game.get(GameMap.class);
        Vector3fc startPos = entity.getPositionAt(startTime);
        Vector3fc endPos = entity.getPositionAt(endTime);

        float intersect = map.gridMapIntersection(startPos, new Vector3f(endPos).sub(startPos));
        if (intersect == 1) return;

        // collision found
        float collisionTime = startTime + intersect * (endTime - startTime);
        Vector3fc midPos = entity.getPositionAt(collisionTime);

        // only accept if the found position is sufficiently close to a checked point
        while (Math.min(startPos.distanceSquared(midPos), endPos.distanceSquared(midPos)) > Settings.MIN_COLLISION_CHECK_SQ) {
            intersect = map.gridMapIntersection(startPos, new Vector3f(midPos).sub(startPos));

            if (intersect < 1) {
                collisionTime = startTime + intersect * (collisionTime - startTime);
                endPos = midPos;

            } else { // wrong half, repeat with other half
                intersect = map.gridMapIntersection(midPos, new Vector3f(endPos).sub(midPos));
                collisionTime = collisionTime + intersect * (endTime - collisionTime);
                startPos = midPos;
            }
        }

        entity.collideWith(map, collisionTime);
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
    public void writeToDataStream(DataOutputStream out) throws IOException {
        Collection<Entity> entities = entityList.getEntityList();
        List<Storable> box = new ArrayList<>(entities.size());

        for (Entity e : entities) {
            if (e instanceof Storable) {
                box.add((Storable) e);
            }
        }

        out.writeInt(box.size());
        for (Storable s : box) {
            Storable.writeSafe(out, s);
        }
    }

    public PhysicsEngine(DataInputStream in) throws IOException {
        int size = in.readInt();
        List<Entity> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Entity entity = Storable.readSafe(in, MovingEntity.class);
            if (entity == null) continue;
            list.add(entity);
        }
        entityList = new CollisionDetection(list, 0);
    }

    @Override
    public void cleanup() {
        entityList.cleanup();
    }

}
