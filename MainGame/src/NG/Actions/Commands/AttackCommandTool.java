package NG.Actions.Commands;

import NG.Engine.Game;
import NG.Entities.MonsterEntity;
import NG.Entities.ProjectilePowerBall;
import NG.GameMap.GameMap;
import NG.InputHandling.MouseTools.DefaultMouseTool;
import NG.Living.Player;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 5-4-2019.
 */
public class AttackCommandTool extends DefaultMouseTool {
    private MonsterEntity entity;

    public AttackCommandTool(Game game, MonsterEntity entity) {
        super(game);
        this.entity = entity;
    }

    @Override
    public void apply(Vector3fc position) {
        GameMap map = game.get(GameMap.class);

//        // exact position
//        float z = map.getHeightAt(position.x(), position.y());
//        Vector3f target = new Vector3f(position.x(), position.y(), z);

        // coordinate position
        Vector2i coord = map.getCoordinate(position);
        Vector3f target = map.getPosition(coord.x, coord.y);

        ProjectilePowerBall projectile = new ProjectilePowerBall(game, entity, target, 2f, 0.5f);
        Command command = new CommandAttack(new Player(), entity, projectile);
        entity.getController().queueCommand(command);
    }
}
