package NG.Living.Commands;

import NG.Engine.Game;
import NG.Entities.MonsterEntity;
import NG.GameMap.GameMap;
import NG.InputHandling.MouseTools.DefaultMouseTool;
import NG.Living.MonsterSoul;
import NG.Living.Player;
import org.joml.Vector2i;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 18-2-2019.
 */
public class WalkCommandTool extends DefaultMouseTool {
    private Game game;
    private MonsterSoul controller;

    public WalkCommandTool(Game game, MonsterEntity entity) {
        this.game = game;
        controller = entity.getController();
    }

    @Override
    public void apply(Vector3fc position) {
        Vector2i coord = game.get(GameMap.class).getCoordinate(position);
        Command command = new CommandWalk(new Player(), controller, coord);
        controller.queueCommand(command);
    }
}
