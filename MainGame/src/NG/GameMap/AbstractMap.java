package NG.GameMap;

import NG.CollisionDetection.BoundingBox;
import NG.Engine.Game;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Tools.Vectors;
import org.joml.*;

import java.lang.Math;

import static NG.Settings.Settings.TILE_SIZE;

/**
 * An object that represents the world where all other entities stand on. This includes both the graphical and the
 * physical representation. The map considers a difference between coordinates and position, in that a coordinate may be
 * of different magnitude than an equivalent position.
 * @author Geert van Ieperen. Created on 29-9-2018.
 */
public abstract class AbstractMap implements GameMap {

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

        float t = gridMapIntersection(origin, direction, 1);
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

            float newIntersect = gridMapIntersection(corner, direction, maximum);
            if (newIntersect < maximum) maximum = newIntersect;
        }

        return maximum;
    }

    @Override
    public float gridMapIntersection(Vector3fc origin, Vector3fc direction, float maximum) {
        if (direction.x() == 0 && direction.y() == 0) return maximum;

        Vector2ic size = getSize();

        Vector2f coordPos = new Vector2f(origin.x() / TILE_SIZE, origin.y() / TILE_SIZE); // possible off-by-one errors are fixed by worldClip
        Vector2f coordDir = new Vector2f(direction.x() / TILE_SIZE, direction.y() / TILE_SIZE); // also scale this vector

        Vector2f worldClip = new Vector2f();
        boolean isOnWorld = Intersectionf.intersectRayAab(
                coordPos.x, coordPos.y, 0,
                coordDir.x, coordDir.y, 0,
                1, 1, -1,
                size.x() - 1, size.y() - 1, 1,
                worldClip
        );
        if (!isOnWorld) return maximum;

        float adjMin = Math.max(worldClip.x, 0);
        float adjMax = Math.min(worldClip.y, maximum);

        coordPos.add(new Vector2f(coordDir).mul(adjMin));
        coordDir.mul(adjMax - adjMin);
        Vector2i lineTraverse = new Vector2i((int) coordPos.x, (int) coordPos.y);

        while (lineTraverse != null) {
            int xCoord = lineTraverse.x;
            int yCoord = lineTraverse.y;

            Float secFrac = getTileIntersect(origin, direction, xCoord, yCoord);

            if (secFrac == null) {
                return maximum;

            } else if (secFrac >= 0 && secFrac < 1) {
                return secFrac;
            }

            // no luck, try next coordinate
            lineTraverse = nextCoordinate(xCoord, yCoord, coordPos, coordDir, 1);
        }

        return maximum;
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

    /**
     * assuming you are at (xCoord, yCoord), computes the next coordinate hit by the given ray
     * @param xCoord    the current x coordinate
     * @param yCoord    the current y coordinate
     * @param origin    the origin of the ray
     * @param direction the direction of the ray
     * @return the next coordinate hit by the ray. If the previous coordinate was not hit by the ray, return a
     * coordinate closer to the ray than this one (will eventually return coordinates on the ray)
     */
    public static Vector2i nextCoordinate(
            int xCoord, int yCoord, Vector2fc origin, Vector2fc direction, float maximum
    ) {
        boolean xIsPos = direction.x() > 0;
        boolean yIsPos = direction.y() > 0;

        if (direction.x() == 0) {
            int yNext = yCoord + (yIsPos ? 1 : -1);
            if ((yNext - origin.y()) / direction.y() > maximum) { // a = o + d * maximum; maximum = (a - o) / d
                return null;
            } else {
                return new Vector2i(xCoord, yNext);
            }

        } else if (direction.y() == 0) {
            int xNext = xCoord + (xIsPos ? 1 : -1);
            if ((xNext - origin.x()) / direction.x() > maximum) {
                return null;
            } else {
                return new Vector2i(xNext, yCoord);
            }
        }

        float xIntersect = Intersectionf.intersectRayPlane(
                origin.x(), origin.y(), 0, direction.x(), direction.y(), 0,
                (xIsPos ? xCoord + 1 : xCoord), yCoord, 0, (xIsPos ? -1 : 1), 0, 0,
                1e-3f
        );
        float yIntersect = Intersectionf.intersectRayPlane(
                origin.x(), origin.y(), 0, direction.x(), direction.y(), 0,
                xCoord, (yIsPos ? yCoord + 1 : yCoord), 0, 0, (yIsPos ? -1 : 1), 0,
                1e-3f
        );

        if (xIntersect >= maximum && yIntersect >= maximum) {
            return null;
        }

        Vector2i next = new Vector2i(xCoord, yCoord);

        if (xIntersect <= yIntersect) next.add((xIsPos ? 1 : -1), 0);
        if (yIntersect <= xIntersect) next.add(0, (yIsPos ? 1 : -1));

        return next;
    }
}
