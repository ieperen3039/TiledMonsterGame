package NG.Tools;

import NG.Camera.TycoonFixedCamera;
import NG.CollisionDetection.PhysicsEngine;
import NG.Core.GameAspect;
import NG.Core.GameService;
import NG.Core.GameTimer;
import NG.Core.Version;
import NG.DataStructures.Generic.Color4f;
import NG.GUIMenu.Frames.FrameManagerImpl;
import NG.GameMap.GameMap;
import NG.GameMap.TileMap;
import NG.InputHandling.MouseToolCallbacks;
import NG.Rendering.GLFWWindow;
import NG.Rendering.Lights.GameLights;
import NG.Rendering.Lights.SingleShadowMapLights;
import NG.Rendering.RenderLoop;
import NG.Settings.Settings;
import org.joml.Vector3f;

import java.util.List;

/**
 * @author Geert van Ieperen created on 6-2-2019.
 */
public class DecoyGame extends GameService {

    public DecoyGame(String title, RenderLoop renderloop, Settings settings, Version version) {
        super(version, Thread.currentThread().getName(), renderloop, settings);
        Logger.INFO.print("Starting up a partial game engine...");

        add(new GameTimer(settings.RENDER_DELAY));
        add(new GLFWWindow(title, new GLFWWindow.Settings(settings), true));
        add(new MouseToolCallbacks());
        add(new SingleShadowMapLights());
        add(new FrameManagerImpl());
        add(new TileMap(Settings.CHUNK_SIZE));
        add(new TycoonFixedCamera(new Vector3f(), 0, 10));
        add(new PhysicsEngine());

        renderloop.addHudItem(get(FrameManagerImpl.class)::draw);
        get(GameLights.class).addDirectionalLight(new Vector3f(1, -1.5f, 2), Color4f.WHITE, 0.5f);
    }

    public void setGameMap(GameMap gameMap) {
        List<GameMap> oldMap = getAll(GameMap.class);
        add(gameMap);
        oldMap.forEach(this::remove);
        oldMap.forEach(GameAspect::cleanup);
    }
}
