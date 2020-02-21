package NG.GameEvent;

import NG.CollisionDetection.GameState;
import NG.Core.Game;
import NG.Entities.Projectiles.Projectile;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 19-4-2019.
 */
public class ProjectileSpawnEvent extends Event {
    private final Game game;
    private Projectile elt;
    private Vector3fc spawnPosition;
    private final Supplier<Boolean> validity;

    /**
     * @param elt           the projectile to be spawned
     * @param spawnPosition the position of spawning
     * @param eventTime     the time of spawning in seconds
     * @param validity      a function that checks whether this spawn is still valid on the current game time
     */
    public ProjectileSpawnEvent(
            Game game, Projectile elt, Vector3fc spawnPosition, float eventTime,
            Supplier<Boolean> validity
    ) {
        super(eventTime);
        this.game = game;
        this.elt = elt;
        this.spawnPosition = spawnPosition;
        this.validity = validity;
    }

    @Override
    public void run() {
        if (validity.get()) {
            elt.launch(spawnPosition, eventTime);
            game.get(GameState.class).addEntity(elt);
        }
    }

    /**
     * creates and schedules the spawning of a projectile on the given spawn time
     * @see #ProjectileSpawnEvent(Game, Projectile, Vector3fc, float, Supplier)
     */
    public static void create(
            Game game, Projectile projectile, float spawnTime, Vector3f spawnPosition
    ) {
        Event e = new ProjectileSpawnEvent(game, projectile, spawnPosition, spawnTime, () -> true);
        game.get(EventLoop.class).addEvent(e);
    }
}
