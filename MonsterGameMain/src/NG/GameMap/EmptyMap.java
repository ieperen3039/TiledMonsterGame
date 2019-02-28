package NG.GameMap;

import NG.ActionHandling.MouseTools.MouseTool;
import NG.Engine.Game;
import NG.Rendering.MatrixStack.SGL;
import org.joml.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
    public Vector3i getCoordinate(Vector3fc position) {
        return new Vector3i();
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
    public List<Vector2i> findPath(
            Vector2ic beginPosition, Vector2ic target, float walkSpeed, float climbSpeed
    ) {
        return Collections.emptyList();
    }

    @Override
    public boolean checkMouseClick(MouseTool tool, int xSc, int ySc) {
        return false;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void writeToFile(DataOutput out) throws IOException {

    }

    public EmptyMap(DataInput in) {

    }
}
