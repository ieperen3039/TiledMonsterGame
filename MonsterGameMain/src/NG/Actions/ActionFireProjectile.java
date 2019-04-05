package NG.Actions;

import NG.Animations.BodyAnimation;
import NG.Animations.UniversalAnimation;
import NG.Engine.Game;
import NG.Engine.GameTimer;
import NG.Entities.MonsterEntity;
import org.joml.Vector2ic;

/**
 * @author Geert van Ieperen created on 5-4-2019.
 */
public class ActionFireProjectile extends ActionIdle {
    private static final float aniFireMoment = 0.7f;

    public ActionFireProjectile(Game game, EntityAction preceding, float duration, MonsterEntity source) {
        this(game, preceding.getEndCoordinate(), duration, source);
    }

    public ActionFireProjectile(Game game, Vector2ic coordinate, float duration, MonsterEntity source) {
        super(game, coordinate, duration);

        float now = game.get(GameTimer.class).getGametime();
    }

    @Override
    public UniversalAnimation getAnimation() {
        return BodyAnimation.IDLE;
    }
}
