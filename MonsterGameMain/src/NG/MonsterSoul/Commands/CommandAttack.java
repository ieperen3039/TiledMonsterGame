package NG.MonsterSoul.Commands;

import NG.Actions.ActionFireProjectile;
import NG.Actions.EntityAction;
import NG.Engine.Game;
import NG.Entities.MonsterEntity;
import NG.MonsterSoul.Living;
import org.joml.Vector2ic;

import java.util.Collections;
import java.util.List;

/**
 * @author Geert van Ieperen created on 5-4-2019.
 */
public class CommandAttack extends Command {
    private static final float DURATION = 1;
    private final MonsterEntity entity;
    private final Vector2ic target;
    private final ActionFireProjectile.Type type;

    public CommandAttack(Living source, MonsterEntity entity, Vector2ic target, ActionFireProjectile.Type type) {
        super(source, entity.getController());
        this.entity = entity;
        this.target = target;
        this.type = type;
    }

    @Override
    public List<EntityAction> toActions(Game game, EntityAction preceding, float startTime) {
        return Collections.singletonList(new ActionFireProjectile(game, entity, target, type, startTime, DURATION));
    }
}
