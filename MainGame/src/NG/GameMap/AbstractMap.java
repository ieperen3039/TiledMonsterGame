package NG.GameMap;

import NG.CollisionDetection.BoundingBox;
import NG.Core.Game;
import NG.Entities.StaticEntity;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Settings.Settings;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.*;

import java.util.ArrayList;
import java.util.List;

/**
 * An object that represents the world where all other entities stand on. This includes both the graphical and the
 * physical representation. The map considers a difference between coordinates and position, in that a coordinate may be
 * of different magnitude than an equivalent position.
 * @author Geert van Ieperen. Created on 29-9-2018.
 */
public abstract class AbstractMap extends StaticEntity implements GameMap {
    public AbstractMap() {
    }

    @Override
    public int getHeightAt(Vector2ic position) {
        return getHeightAt(position.x(), position.y());
    }

    @Override
    public Vector3i getCoordinate3D(int x, int y) {
        return new Vector3i(x, y, getHeightAt(x, y));
    }

    @Override
    public Vector3f getPosition(Vector2ic mapCoord) {
        return getPosition(mapCoord.x(), mapCoord.y());
    }

    public boolean checkMouseClick(MouseTool tool, int xSc, int ySc, Game game) {
        Vector3f origin = new Vector3f();
        Vector3f direction = new Vector3f();
        Vectors.windowCoordToRay(game, xSc, ySc, origin, direction);
        direction.normalize(Settings.Z_FAR - Settings.Z_NEAR);

        float t = this.getIntersection(origin, direction, 0);
        if (t == 1) return false;

        Vector3f position = new Vector3f(direction).mul(t).add(origin);
        tool.apply(position, xSc, ySc);
        return true;
    }

    @Override
    public float intersectFractionBoundingBox(
            BoundingBox hitbox, Vector3fc origin, Vector3fc direction, float maximum
    ) {
        for (Vector3f corner : hitbox.corners()) {
            corner.add(origin);

            float newIntersect = this.getIntersection(corner, direction, 0);
            if (newIntersect < maximum) maximum = newIntersect;
        }

        return maximum;
    }

    @Override
    public float getIntersection(Vector3fc origin, Vector3fc direction, float gameTime) {
        if (direction.x() == 0 && direction.y() == 0) return 1;

        Vector2ic size = getSize();
        Vector2f coordPos = getCoordPosf(origin);
        Vector2f coordDir = getCoordDirf(direction);

        Vector2f worldClip = new Vector2f();
        boolean isOnWorld = Intersectionf.intersectRayAab(
                coordPos.x, coordPos.y, 0,
                coordDir.x, coordDir.y, 0,
                0, 0, -1,
                size.x(), size.y(), 1,
                worldClip
        );

        if (!isOnWorld) return 1;

        if (worldClip.x > 0) {
            coordPos.add(coordDir.mul(worldClip.x));

        } else {
            // check this tile before setting up voxel ray casting
            Float secFrac = getTileIntersect(origin, direction, (int) coordPos.x, (int) coordPos.y);
            if (secFrac >= 0 && secFrac < 1) {
                return secFrac;
            }
        }

        int xCoord = (int) coordPos.x;
        int yCoord = (int) coordPos.y;

        boolean xIsPos = coordDir.x > 0;
        boolean yIsPos = coordDir.y > 0;

        final int dx = (xIsPos ? 1 : -1);
        final int dy = (yIsPos ? 1 : -1);

        coordDir.normalize();

        float xIntersect = Intersectionf.intersectRayPlane(
                coordPos.x, coordPos.y, 0, coordDir.x, coordDir.y, 0,
                xCoord + dx, yCoord + dy, 0, (xIsPos ? -1 : 1), 0, 0,
                1e-3f
        );
        float yIntersect = Intersectionf.intersectRayPlane(
                coordPos.x, coordPos.y, 0, coordDir.x, coordDir.y, 0,
                xCoord + dx, yCoord + dy, 0, 0, (yIsPos ? -1 : 1), 0,
                1e-3f
        );

        float tMaxX = xIntersect;
        float tMaxY = yIntersect;

        final float dtx = (coordDir.x() == 0 ? Float.POSITIVE_INFINITY : (1f / coordDir.x));
        final float dty = (coordDir.y() == 0 ? Float.POSITIVE_INFINITY : (1f / coordDir.y));

        List<Vector2i> coords = new ArrayList<>();

        while (xCoord >= 0 && yCoord >= 0 && xCoord < size.x() && yCoord < size.y()) {
            coords.add(new Vector2i(xCoord, yCoord));
            Float secFrac = getTileIntersect(origin, direction, xCoord, yCoord);

            if (secFrac == null) {
                Logger.ASSERT.printf("got (%d, %d) which is out of bounds", xCoord, yCoord);
                return 1;

            } else if (secFrac >= 0 && secFrac < 1) {
                setHighlights(coords.toArray(new Vector2ic[0]));
                return secFrac;
            }

            if (tMaxX < tMaxY) {
                tMaxX = tMaxX + dtx;
                xCoord = xCoord + dx;
            } else {
                tMaxY = tMaxY + dty;
                yCoord = yCoord + dy;
            }
        }

        setHighlights(coords.toArray(new Vector2ic[0]));
        return 1;
    }

    /** returns the floating-point transformation to coordinates */
    protected abstract Vector2f getCoordDirf(Vector3fc direction);

    /** returns the floating-point transformation to coordinates */
    protected abstract Vector2f getCoordPosf(Vector3fc origin);

    @Override
    public List<Vector3f> getShapePoints(List<Vector3f> dest, float gameTime) {
        dest.clear(); // returning all points is expensive and can be left out
        return dest;
    }

    /**
     * computes the intersection of a ray on the given coordinate
     * @param origin    the origin of the ray in real space
     * @param direction the direction of the ray
     * @param xCoord    the x coordinate
     * @param yCoord    the y coordinate
     * @return the first intersection of the ray with this tile, {@link Float#POSITIVE_INFINITY} if it does not hit and
     * null if the given coordinate is not on the map.
     */
    abstract Float getTileIntersect(Vector3fc origin, Vector3fc direction, int xCoord, int yCoord);

    @Override
    public BoundingBox getHitbox() {
        Vector2ic size = getSize();
        return new BoundingBox(0, 0, Float.NEGATIVE_INFINITY, size.x(), size.y(), Float.POSITIVE_INFINITY);
    }
}
