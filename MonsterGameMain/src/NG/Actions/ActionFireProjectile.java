package NG.Actions;

import NG.Animations.BodyAnimation;
import NG.Animations.UniversalAnimation;
import NG.Engine.Game;
import NG.Entities.MonsterEntity;
import NG.Entities.Projectile;
import NG.Entities.ProjectilePowerBall;
import NG.GameEvent.Event;
import NG.GameEvent.EventLoop;
import NG.GameEvent.ProjectileSpawnEvent;
import NG.Tools.Vectors;
import org.joml.Vector3f;

/**
 * the action of firing a single {@link ProjectilePowerBall}
 * @author Geert van Ieperen created on 5-4-2019.
 */
// TODO find appropriate action
public class ActionFireProjectile extends ActionIdle {
    private static final float aniFireMoment = 0.5f;

    public ActionFireProjectile(
            Game game, MonsterEntity source, Projectile elt, float startTime, float duration
    ) {
        super(source.getPositionAt(startTime), duration);

        float spawnTime = duration * aniFireMoment + startTime;
        Vector3f spawnPosition = source.getPositionAt(spawnTime).add(0, 0, 1);

        // TODO replace validity check with something more robust
        Event e = new ProjectileSpawnEvent(game, elt, spawnPosition, spawnTime, () -> source.getCurrentAction() == this);
        game.get(EventLoop.class).addEvent(e);
    }

    @Override
    public UniversalAnimation getAnimation() {
        return BodyAnimation.IDLE;
    }

    @Override
    public String toString() {
        return "Fire projectile (at " + Vectors.toString(getEndPosition()) + ")";
    }

}
