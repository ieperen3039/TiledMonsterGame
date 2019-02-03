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
    public int TARGET_TPS = 10;

    // video settings
    public int TARGET_FPS = 60;
    public boolean V_SYNC = true;
    public int WINDOW_WIDTH = 1200;
    public int WINDOW_HEIGHT = 800;
    public static float FOV = (float) Math.toRadians(40);
    public static float Z_NEAR = 0.01f;
    public static float Z_FAR = 1000;
    public int ANTIALIAS_LEVEL = 1;
    public boolean ISOMETRIC_VIEW = true;
    public float CAMERA_ZOOM_SPEED = 0.1f;
    public int MAX_CAMERA_DIST = 1000;
    public float MIN_CAMERA_DIST = 0.5f;
    public int SHADOW_RESOLUTION = 1024;

    // UI settings
    public static int TOOL_BAR_HEIGHT = 80;

    public Color4f AMBIENT_LIGHT = new Color4f(1, 1, 1, 0.2f);
}
