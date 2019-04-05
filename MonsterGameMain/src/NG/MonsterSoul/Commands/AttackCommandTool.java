package NG.MonsterSoul.Commands;

import NG.Engine.Game;
import NG.Entities.MonsterEntity;
import NG.GameMap.GameMap;
import NG.InputHandling.MouseTools.DefaultMouseTool;
import NG.MonsterSoul.Player;
import org.joml.Vector2i;
import org.joml.Vector3fc;
import org.joml.Vector3i;

import static NG.Actions.ActionFireProjectile.Type.POWERBALL;

/**
 * @author Geert van Ieperen created on 5-4-2019.
 */
public class AttackCommandTool extends DefaultMouseTool {
    private final Game game;
    private MonsterEntity entity;

    public AttackCommandTool(Game game, MonsterEntity entity) {
        this.game = game;
        this.entity = entity;
    }

    @Override
    public void apply(Vector3fc position) {
        Vector3i coord = game.get(GameMap.class).getCoordinate(position);
        Command command = new CommandAttack(new Player(), entity, new Vector2i(coord.x, coord.y), POWERBALL);
        entity.getController().accept(command);
    }
}
