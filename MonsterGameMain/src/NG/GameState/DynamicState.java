package NG.GameState;

import NG.CollisionDetection.CollisionDetection;
import NG.Engine.Game;
import NG.Entities.Entity;
import NG.GameMap.GameMap;
import NG.InputHandling.ClickShader;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import NG.Tools.Vectors;
import org.joml.Vector3f;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Geert van Ieperen created on 10-2-2019.
 */
public class DynamicState implements GameState {
    private final CollisionDetection entityList;
    private Game game;

    public DynamicState() {
        entityList = new CollisionDetection();
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
        entityList.setWorld(game.get(GameMap.class)::checkEntityCollision);
    }

    @Override
    public void update(float gameTime) {
        entityList.processCollisions(gameTime);
    }

    @Override
    public void addEntity(Entity entity) {
        assert !entityList.contains(entity) : "duplicate entity";
        entityList.addEntity(entity);
    }

    @Override
    public void draw(SGL gl) {
        entityList.forEach(e -> e.draw(gl));
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

            entity = entityList.rayTrace(origin, direction);
        }

        if (entity == null) return false;

        tool.apply(entity, xSc, ySc);
        return true;
    }

    @Override
    public void writeToDataStream(DataOutput out) throws IOException {
        Collection<Entity> entities = entityList.getEntityList();
        List<Storable> box = new ArrayList<>(entities.size());

        for (Entity e : entities) {
            if (e instanceof Storable) {
                box.add((Storable) e);
            }
        }

        Storable.writeCollection(out, box);
    }

    public DynamicState(DataInput in) throws IOException, ClassNotFoundException {
        List<Entity> list = Storable.readCollection(in, Entity.class);
        entityList = new CollisionDetection(list);
    }

    @Override
    public void cleanup() {
        entityList.cleanup();
    }
}
