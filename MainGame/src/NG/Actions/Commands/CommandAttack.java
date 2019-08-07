package NG.Actions.Commands;

import NG.Actions.ActionFireProjectile;
import NG.Actions.EntityAction;
import NG.Core.Game;
import NG.Entities.MonsterEntity;
import NG.Entities.Projectiles.Projectile;
import NG.Living.Living;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 5-4-2019.
 */
public class CommandAttack extends Command {
    private static final float DURATION = 1.5f;
    private final MonsterEntity entity;
    private Projectile elt;

    public CommandAttack(Living source, MonsterEntity entity, Projectile projectile) {
        super(source, entity.getController());
        this.entity = entity;
        elt = projectile;
    }

    @Override
    public EntityAction getAction(Game game, Vector3fc startPosition, float gameTime) {
        if (elt.isLaunched()) {
            return null;
        } else {
            return new ActionFireProjectile(game, entity, elt, gameTime, DURATION);
        }
    }
}
