package NG.Settings;

import NG.DataStructures.Generic.Color4f;

/**
 * A class that collects a number of settings. It is the only class whose fields are always initialized upon creation.
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class Settings {
    public static final String GAME_NAME = "MonsterGame"; // laaaaame
    public boolean DEBUG = true;

    // game engine settings
    public int TARGET_TPS = 30;
    public final int CHUNK_SIZE = 16; // number of tiles in a chunk
    public static final float TILE_SIZE = 2f;
    public static final float TILE_SIZE_Z = 0.5f;
    public boolean SAVE_WITH_SECURITY_BITS = true;

    // video settings
    public int TARGET_FPS = 60;
    public boolean V_SYNC = true;
    public int WINDOW_WIDTH = 1200;
    public int WINDOW_HEIGHT = 800;
    public static final float FOV = (float) Math.toRadians(40);
    public static final float Z_NEAR = 0.01f;
    public static final float Z_FAR = 1000;
    public int ANTIALIAS_LEVEL = 1;
    public boolean ISOMETRIC_VIEW = true;
    public float RENDER_DELAY = 1f / TARGET_TPS;
    public float CAMERA_ZOOM_SPEED = 0.1f;
    public int MAX_CAMERA_DIST = 1000;
    public float MIN_CAMERA_DIST = 0.5f;
    public int STATIC_SHADOW_RESOLUTION = 2048;
    public int DYNAMIC_SHADOW_RESOLUTION = 256;

    // UI settings
    public static int TOOL_BAR_HEIGHT = 70;

    public Color4f AMBIENT_LIGHT = new Color4f(1, 1, 1, 0.2f);
    public boolean DEBUG_SCREEN = true;
}