package NG.GameMap;

import NG.CollisionDetection.BoundingBox;
import NG.Core.Game;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Rendering.MatrixStack.SGL;
import org.joml.*;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;

import static NG.Settings.Settings.TILE_SIZE;

/**
 * an empty map that does absolutely nothing at all
 * @author Geert van Ieperen created on 28-2-2019.
 */
public class EmptyMap extends AbstractMap {
    public EmptyMap() {
    }

    @Override
    public void generateNew(MapGeneratorMod mapGenerator) {
    }

    @Override
    public float getHeightAt(float x, float y) {
        return 0;
    }

    @Override
    public int getHeightAt(int x, int y) {
        return 0;
    }

    @Override
    public Vector2i getCoordinate(Vector3fc position) {
        return new Vector2i();
    }

    @Override
    public Vector3f getPosition(int x, int y) {
        return new Vector3f();
    }

    @Override
    public void draw(SGL gl) {

    }

    @Override
    public BoundingBox getHitbox(float gameTime) {
        return new BoundingBox(0, 0, 0, 0, 0, 0);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {

    }

    @Override
    public void setHighlights(Vector2ic... coordinates) {

    }

    @Override
    public Vector2ic getSize() {
        return new Vector2i();
    }

    @Override
    public Collection<Vector2i> findPath(Vector2ic beginPosition, Vector2ic target, float walkSpeed, float climbSpeed) {
        return Collections.emptyList();
    }

    @Override
    public Float getTileIntersect(Vector3fc origin, Vector3fc direction, int xCoord, int yCoord) {
        return null;
    }

    @Override
    public boolean checkMouseClick(MouseTool tool, int xSc, int ySc) {
        return false;
    }

    @Override
    public void init(Game game) throws Exception {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public Vector2f getCoordDirf(Vector3fc direction) {
        return new Vector2f(direction.x() / TILE_SIZE, direction.y() / TILE_SIZE);
    }

    @Override
    public Vector2f getCoordPosf(Vector3fc origin) {
        return new Vector2f(origin.x() / TILE_SIZE, origin.y() / TILE_SIZE);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }

    @Override
    public void restore(Game game) {

    }
}
