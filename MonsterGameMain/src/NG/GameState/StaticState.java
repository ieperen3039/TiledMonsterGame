package NG.GameState;

import NG.ActionHandling.ClickShader;
import NG.ActionHandling.MouseTools.MouseTool;
import NG.Engine.Game;
import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shapes.Primitives.Collision;
import NG.Storable;
import NG.Tools.Logger;
import org.joml.Vector3fc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

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
    public void writeToFile(DataOutput out) throws IOException {
        List<Storable> box = new ArrayList<>(entities.size());

        for (Entity e : entities) {
            if (e instanceof Storable) {
                box.add((Storable) e);
            }
        }

        out.writeInt(box.size());
        for (Storable s : box) {
            Storable.writeToFile(out, s);
        }
    }

    public StaticState(DataInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        ArrayList<Entity> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            Entity entity = Storable.readFromFile(in, Entity.class);
            list.add(entity);
        }

        entities = Collections.synchronizedList(list);
    }

    @Override
    public void cleanup() {
        entities.clear();
    }
}
