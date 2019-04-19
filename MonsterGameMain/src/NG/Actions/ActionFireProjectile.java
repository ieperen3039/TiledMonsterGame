package NG.Actions;

import NG.Animations.BodyAnimation;
import NG.Animations.UniversalAnimation;
import NG.Engine.Game;
import NG.Entities.MonsterEntity;
import NG.Entities.ProjectilePowerBall;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * the action of firing a single {@link ProjectilePowerBall}
 * @author Geert van Ieperen created on 5-4-2019.
 */
public class ActionFireProjectile extends ActionIdle {
    private static final float aniFireMoment = 0.5f;
    private final Type type;

    public enum Type {
        POWERBALL
    }

    // TODO find appropriate action
    public ActionFireProjectile(
            Game game, MonsterEntity source, Vector3fc target, Type type, float startTime, float duration
    ) {
        super(game, source.getLastAction(), duration);
        this.type = type;

        Vector3f spawnPosition = source.getPositionAt(startTime).add(0, 0, 1);
    }

    @Override
    public UniversalAnimation getAnimation() {
        return BodyAnimation.IDLE;
    }

    @Override
    public String toString() {
        return "Fire projectile (at " + Vectors.toString(getEndCoordinate()) + ")";
    }
}
