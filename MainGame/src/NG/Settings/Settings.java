package NG.Settings;

import NG.Actions.EntityAction;
import NG.DataStructures.Generic.Color4f;

/**
 * A class that collects a number of settings. It is the only class whose fields are always initialized upon creation.
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class Settings {
    public static final String GAME_NAME = "MonsterGame"; // laaaaame

    // debug settings
    public final boolean DEBUG = true;
    public boolean DEBUG_SCREEN = DEBUG;
    public boolean RENDER_HITBOXES = DEBUG;

    // game engine settings
    public int TARGET_TPS = 60;
    public static final int CHUNK_SIZE = 16; // number of tiles in a chunk
    public static final float TILE_SIZE = 2f;
    public static final float TILE_SIZE_Z = 0.5f;
    public static final float GRAVITY_CONSTANT = 10f; // 9,81
    public static final float MIN_COLLISION_CHECK = EntityAction.ACCEPTABLE_DIFFERENCE / 2f;

    // video settings
    public int TARGET_FPS = 40;
    public static final int DEFAULT_WINDOW_WIDTH = 1600;
    public static final int DEFAULT_WINDOW_HEIGHT = 900;
    public static final float FOV = (float) Math.toRadians(30);
    public static final float Z_NEAR = 0.1f;
    public static final float Z_FAR = 1000;
    public boolean V_SYNC = false;
    public int ANTIALIAS_LEVEL = 1;
    public boolean ISOMETRIC_VIEW = false;

    public int STATIC_SHADOW_RESOLUTION = 0;
    public int DYNAMIC_SHADOW_RESOLUTION = 0;

    public float RENDER_DELAY = 1f / TARGET_TPS;
    public float CAMERA_ZOOM_SPEED = 0.1f;
    public int MAX_CAMERA_DIST = (int) Z_FAR;
    public float MIN_CAMERA_DIST = 1f;

    public float PARTICLE_SIZE = 0.2f;
    public float PARTICLE_MODIFIER = 1f;

    // UI settings
    public boolean HIDE_CURSOR_ON_MAP = !DEBUG;

    // ambiance settings
    public static final Color4f AMBIENT_LIGHT = new Color4f(1, 1, 1, 0.15f);
    public static final Color4f FOG_COLOR = Color4f.GREY;
}
