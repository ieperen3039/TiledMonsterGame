package NG.Actions.Commands;

import NG.Actions.Attacks.ActionFireProjectile;
import NG.Actions.EntityAction;
import NG.Core.Game;
import NG.Entities.MonsterEntity;
import NG.Entities.Projectiles.Projectile;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 5-4-2019.
 */
public class CommandAttack extends Command {
    private static final float DURATION = 1f;
    private Projectile elt;

    public CommandAttack(Projectile projectile) {
        super();
        elt = projectile;
    }

    @Override
    public EntityAction getAction(Game game, Vector3fc startPosition, float gameTime, MonsterEntity entity) {
        return elt.isLaunched() ? null : new ActionFireProjectile(game, entity, elt, gameTime, DURATION);
    }
}
