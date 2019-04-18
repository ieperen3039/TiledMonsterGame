package NG.GameState;

import NG.CollisionDetection.CollisionDetection;
import NG.Engine.Game;
import NG.Entities.Entity;
import NG.GameMap.ClaimRegistry;
import NG.GameMap.GameMap;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import NG.Tools.Vectors;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

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
    public boolean checkMouseClick(MouseTool tool, int xSc, int ySc) {
        Vector3f origin = new Vector3f();
        Vector3f direction = new Vector3f();
        Vectors.windowCoordToRay(game, xSc, ySc, origin, direction);

        GameMap map = game.get(GameMap.class);
        Vector3f position = map.intersectWithRay(origin, direction);
        Vector3i asCoord = map.getCoordinate(position);
        Entity claimant = game.get(ClaimRegistry.class).getClaim(new Vector2i(asCoord.x, asCoord.y));
        if (claimant == null) return false;

        tool.apply(claimant, xSc, ySc);
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
