package NG.GameState;

import NG.InputHandling.MouseTools.MouseTool;
import NG.Engine.Game;
import NG.Entities.Entity;
import NG.GameMap.GameMap;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shapes.Primitives.Collision;
import NG.Storable;
import NG.Tools.Vectors;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Geert van Ieperen created on 10-2-2019.
 */
public class StaticState implements GameState {
    private final List<Entity> entities;
    private Game game;

    public StaticState() {
        entities = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
    }

    @Override
    public void addEntity(Entity entity) {
        assert !entities.contains(entity) : "duplicate entity";
        entities.add(entity);
    }

    @Override
    public void draw(SGL gl) {
        entities.forEach(e -> e.draw(gl));
    }

    @Override
    public Collision getEntityCollision(Vector3fc from, Vector3fc to) {
        return null;
    }

    @Override
    public boolean checkMouseClick(MouseTool tool, int xSc, int ySc) {
        Vector3f origin = new Vector3f();
        Vector3f direction = new Vector3f();
        Vectors.windowCoordToRay(game, xSc, ySc, origin, direction);

        GameMap map = game.map();
        Vector3f position = map.intersectWithRay(origin, direction);
        Vector3i asCoord = map.getCoordinate(position);
        Entity claimant = game.claims().getClaim(new Vector2i(asCoord.x, asCoord.y));
        if (claimant == null) return false;

        tool.apply(claimant, xSc, ySc);
        return true;
    }

    @Override
    public void writeToDataStream(DataOutput out) throws IOException {
        List<Storable> box = new ArrayList<>(entities.size());

        for (Entity e : entities) {
            if (e instanceof Storable) {
                box.add((Storable) e);
            }
        }

        Storable.writeCollection(out, box);
    }

    public StaticState(DataInput in) throws IOException, ClassNotFoundException {
        List<Entity> list = Storable.readCollection(in, Entity.class);
        entities = Collections.synchronizedList(list);
    }

    @Override
    public void cleanup() {
        entities.clear();
    }
}
