package NG.Actions.Attacks;

import NG.Actions.ActionMarkers.ActionMarker;
import NG.Actions.EntityAction;
import NG.Animations.BodyAnimation;
import NG.Animations.UniversalAnimation;
import NG.CollisionDetection.GameState;
import NG.Core.Game;
import NG.Entities.MonsterEntity;
import NG.Entities.Projectiles.Projectile;
import NG.Entities.Projectiles.ProjectilePowerBall;
import NG.Settings.Settings;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Supplier;

/**
 * the action of firing a single {@link ProjectilePowerBall}
 * @author Geert van Ieperen created on 5-4-2019.
 */
public class ActionFireProjectile implements EntityAction {
    private static final float aniFireMoment = 0.5f;
    private static final float JUMP_GRAVITY = Settings.GRAVITY_CONSTANT;
    protected final Vector3fc startEnd;

    protected final float duration;

    public ActionFireProjectile(
            Game game, MonsterEntity source, Vector2ic target, float startTime, float duration
    ) {
        this.startEnd = source.getPositionAt(startTime);
        this.duration = duration;

        float relativeSpawnTime = duration * aniFireMoment;
        float spawnTime = relativeSpawnTime + startTime;
        Vector3f spawnPoint = getPositionAt(relativeSpawnTime);

        Supplier<Boolean> validator = () -> source.getActionAt(spawnTime).left.equals(this);
        Projectile elt = new ProjectilePowerBall(game, source, spawnPoint, target, spawnTime, validator, 2, 0.5f);
        game.get(GameState.class).addEntity(elt);
    }

    @Override
    public String toString() {
        return "Fire projectile (at " + Vectors.toString(getEndPosition()) + ")";
    }

    @Override
    public float duration() {
        return duration;
    }

    @Override
    public boolean hasWorldCollision() {
        return true;
    }

    @Override
    public Vector3f getPositionAt(float timeSinceStart) {
        if (timeSinceStart <= 0 || timeSinceStart >= duration) return new Vector3f(startEnd);

        float fraction = Math.max(Toolbox.interpolate(-.1f, 1f, timeSinceStart / duration), 0);
        float x = duration * fraction; // NOT timeSinceStart

        return new Vector3f(
                startEnd.x(), startEnd.y(),
                // z = -Fg x^2 + a x ; a = Fg * duration ; (result of z(duration) = 0)
                -JUMP_GRAVITY * x * x + JUMP_GRAVITY * duration * x + startEnd.z()
        );
    }

    @Override
    public UniversalAnimation getAnimation() {
        return BodyAnimation.BASE_POSE;
    }

    @Override
    public ActionMarker getMarker() {
        return ActionMarker.EMPTY_MARKER;
    }
}
