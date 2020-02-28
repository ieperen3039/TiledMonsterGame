package NG.GUIMenu.HUD;

import NG.Camera.Camera;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Generic.Color4f;
import NG.Entities.MonsterEntity;
import NG.GUIMenu.Components.SComponent;
import NG.GUIMenu.Rendering.NVGOverlay;
import NG.GUIMenu.Rendering.SFrameLookAndFeel;
import NG.GameMap.GameMap;
import NG.InputHandling.EventCallbacks;
import NG.Living.MonsterSoul;
import NG.Living.Player;
import org.joml.*;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * @author Geert van Ieperen created on 16-2-2020.
 */
public class MiniMap extends SComponent implements EventCallbacks.CameraListener {
    public static final float SCALE = (float) Math.sqrt(2);
    private final Game game;
    private final int width;
    private final int height;
    private final int size = 50;
    private final int resolution = 1000;
    private int nvgImage = -1;

    private boolean isDirty = false;
    private float angle = 0;
    private boolean alwaysRefresh; // TODO correct isDirty mapping

    public MiniMap(Game game, int width, int height, boolean doAlwaysRefresh) {
        this.game = game;
        this.width = width;
        this.height = height;
        game.get(EventCallbacks.class).addCameraListener(this);
        setGrowthPolicy(false, false);
        alwaysRefresh = doAlwaysRefresh;
    }

    ByteBuffer getImageBuffer() {
        GameMap map = game.get(GameMap.class);
        Camera camera = game.get(Camera.class);
        Collection<MonsterSoul> team = game.get(Player.class).getTeam();
        float now = game.get(GameTimer.class).getRendertime();

        Map<Integer, Set<Integer>> occupied = new HashMap<>(team.size());
        for (MonsterSoul monsterSoul : team) {
            MonsterEntity e = monsterSoul.entity();
            if (e != null) {
                Vector3f positionAt = e.getPositionAt(now);
                Vector2i coordinate = map.getCoordinate(positionAt);
                occupied.computeIfAbsent(coordinate.x, HashSet::new).add(coordinate.y);
            }
        }

        Vector3fc cameraEye = camera.getEye();
        Vector3fc cameraDirection = camera.vectorToFocus();

        float f = map.gridMapIntersection(cameraEye, cameraDirection);
        Vector3f cameraFocus = new Vector3f(cameraDirection).mul(f).add(cameraEye);
        Vector2f cameraFocusCoord = map.getCoordPosf(cameraFocus);
        float zFocus = map.getHeightAt((int) cameraFocusCoord.x, (int) cameraFocusCoord.y);

        angle = (float) Math.atan2(-cameraDirection.y(), -cameraDirection.x());

        float xStart = cameraFocusCoord.x - size / 2f;
        float yStart = cameraFocusCoord.y - size / 2f;
        float scalar = (float) size / resolution;

        // Load texture contents into a byte buffer
        int byteSize = 4;
        ByteBuffer buffer = ByteBuffer.allocateDirect(byteSize * resolution * resolution);

        for (int xp = 0; xp < resolution; xp++) {
            for (int yp = 0; yp < resolution; yp++) {
                int xc = (int) (xStart + xp * scalar);
                int yc = (int) (yStart + yp * scalar);
                // if this coordinate is within the map
                if (isOnMap(xc, yc, map)) {
                    if (occupied.containsKey(xc) && occupied.get(xc).contains(yc)) {
                        Color4f.RED.put(buffer);

                    } else {
                        float height = map.getHeightAt(xc, yc);
                        float colorOffset = (height - zFocus) * 0.05f;
                        Color4f color = new Color4f(colorOffset * 3, colorOffset + 0.8f, colorOffset * 3);
                        color.put(buffer);
                    }

                } else {
                    Color4f.BLACK.put(buffer);
                }
            }
        }

        buffer.rewind();

        return buffer;
    }

    private boolean isOnMap(float x, float y, GameMap map) {
        Vector2ic size = map.getSize();
        return x >= 0 && x < size.x() && y >= 0 && y < size.y();
    }

    @Override
    public int minWidth() {
        return width;
    }

    @Override
    public int minHeight() {
        return height;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        NVGOverlay.Painter painter = design.getPainter();

        if (nvgImage == -1) {
            nvgImage = painter.createImageFromBuffer(getImageBuffer(), resolution, resolution);
            isDirty = false;

        } else if (isDirty || alwaysRefresh) {
            painter.updateImageFromBuffer(nvgImage, getImageBuffer());
            isDirty = false;
        }

        design.getPainter()
                .drawImage(nvgImage, screenPosition.x(), screenPosition.y(), getWidth(), getHeight(), angle, SCALE);
    }

    @Override
    public void onChange(Camera camera) {
        isDirty = true;
    }

    public void cleanup() {
        game.get(EventCallbacks.class).removeCameraListener(this);
    }
}
