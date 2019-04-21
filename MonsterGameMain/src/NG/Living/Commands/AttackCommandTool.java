package NG.Living.Commands;

import NG.Engine.Game;
import NG.Entities.MonsterEntity;
import NG.Entities.ProjectilePowerBall;
import NG.GameMap.GameMap;
import NG.InputHandling.MouseTools.DefaultMouseTool;
import NG.Living.Player;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

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
        GameMap map = game.get(GameMap.class);

//        // exact position
//        float z = map.getHeightAt(position.x(), position.y());
//        Vector3f target = new Vector3f(position.x(), position.y(), z);

        // coordinate position
        Vector3i coord = map.getCoordinate(position);
        Vector3f target = map.getPosition(coord.x, coord.y);

        ProjectilePowerBall projectile = new ProjectilePowerBall(game, target, 1f, 1f);
        Command command = new CommandAttack(new Player(), entity, projectile);
        entity.getController().queueCommand(command);
    }
}
