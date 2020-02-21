package NG.GameMap;

import NG.CollisionDetection.BoundingBox;
import NG.Core.Game;
import NG.Entities.StaticEntity;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Settings.Settings;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.Math;
import org.joml.*;

/**
 * An object that represents the world where all other entities stand on. This includes both the graphical and the
 * physical representation. The map considers a difference between coordinates and position, in that a coordinate may be
 * of different magnitude than an equivalent position.
 * @author Geert van Ieperen. Created on 29-9-2018.
 */
public abstract class AbstractMap extends StaticEntity implements GameMap {

    public static final float EPSILON = 1f / (1 << 16);

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
        // edge case, direction == (0, 0, dz)
        if (direction.x() == 0 && direction.y() == 0) {
            Vector2i coord = getCoordinate(origin);
            Float sect = getTileIntersect(origin, direction, coord.x, coord.y);
            if (sect == null || sect < 0 || sect > 1) {
                return 1;
            } else {
                return sect;
            }
        }

        Vector2ic size = getSize();
        Vector2f coordPos = getCoordPosf(origin);
        Vector2f coordDir = getCoordDirf(direction).normalize();

        Vector2f worldClip = new Vector2f();
        boolean isOnWorld = Intersectionf.intersectRayAab(
                coordPos.x, coordPos.y, 0,
                coordDir.x, coordDir.y, 0,
                0, 0, -1,
                size.x() - 0.5f, size.y() - 0.5f, 1,
                worldClip
        );

        if (!isOnWorld) return 1;

        if (worldClip.x > 0) {
            coordPos.add(new Vector2f(coordDir).mul(worldClip.x));

            // negate rounding errors
            int xRound = (int) (coordPos.x);
            if (Math.abs(xRound - coordPos.x) < 0.0001f) coordPos.x = xRound;
            int yRound = (int) (coordPos.y);
            if (Math.abs(yRound - coordPos.y) < 0.0001f) coordPos.y = yRound;

        } else {
            // check this tile before setting up voxel ray casting
            Float secFrac = getTileIntersect(origin, direction, (int) coordPos.x, (int) coordPos.y);
            if (secFrac < 0) {
                return 1;
            } else if (secFrac < 1) {
                return secFrac;
            }
        }

        boolean xIsPos = coordDir.x > 0;
        boolean yIsPos = coordDir.y > 0;

        // next t of x border
        float pointX = xIsPos ? Math.ceil(coordPos.x) : Math.floor(coordPos.x);
        float normalX = (xIsPos ? -1 : 1);
        float denomX = normalX * coordDir.x;
        float tNextX = (denomX > EPSILON) ? 0.0f : ((pointX - coordPos.x) * normalX) / denomX;

        // next t of y border
        float pointY = yIsPos ? Math.ceil(coordPos.y) : Math.floor(coordPos.y);
        float normalY = (yIsPos ? -1 : 1);
        float denomY = normalY * coordDir.y;
        float tNextY = (denomY > EPSILON) ? 0.0f : ((pointY - coordPos.y) * normalY) / denomY;

        int xCoord = (int) coordPos.x;
        int yCoord = (int) coordPos.y;

        final int dx = (xIsPos ? 1 : -1);
        final int dy = (yIsPos ? 1 : -1);

        final float dtx = (coordDir.x() == 0 ? Float.POSITIVE_INFINITY : (dx / coordDir.x));
        final float dty = (coordDir.y() == 0 ? Float.POSITIVE_INFINITY : (dy / coordDir.y));

        while (xCoord >= 0 && yCoord >= 0 && xCoord < size.x() && yCoord < size.y()) {
            Float secFrac = getTileIntersect(origin, direction, xCoord, yCoord);

            if (secFrac == null) {
                Logger.ASSERT.printf("got (%d, %d) which is out of bounds", xCoord, yCoord);
                return 1;

            } else if (secFrac < 1) {
                assert secFrac > 0;
                return secFrac;
            }

            if (tNextX < tNextY) {
                tNextX = tNextX + dtx;
                xCoord = xCoord + dx;
            } else {
                tNextY = tNextY + dty;
                yCoord = yCoord + dy;
            }
        }

        return 1;
    }

    /** returns the floating-point transformation to coordinates */
    protected abstract Vector2f getCoordDirf(Vector3fc direction);

    /** returns the floating-point transformation to coordinates */
    protected abstract Vector2f getCoordPosf(Vector3fc origin);

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
}
