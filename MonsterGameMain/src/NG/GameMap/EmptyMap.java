package NG.GameMap;

import NG.Engine.Game;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Rendering.MatrixStack.SGL;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * an empty map that does absolutely nothing at all
 * @author Geert van Ieperen created on 28-2-2019.
 */
public class EmptyMap implements GameMap {
    public EmptyMap() {
    }

    @Override
    public void init(Game game) throws Exception {

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
    public Vector3f intersectWithRay(Vector3fc origin, Vector3fc direction) {
        return new Vector3f();
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
    public Collection<Vector2i> findPath(
            Vector2ic beginPosition, Vector2ic target, float walkSpeed, float climbSpeed
    ) {
        return Collections.emptyList();
    }

    @Override
    public float intersectFraction(Vector3fc origin, Vector3fc direction, float maximum) {
        return 0;
    }

    @Override
    public boolean checkMouseClick(MouseTool tool, int xSc, int ySc) {
        return false;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void writeToDataStream(DataOutput out) throws IOException {

    }

    public EmptyMap(DataInput in) {

    }
}
