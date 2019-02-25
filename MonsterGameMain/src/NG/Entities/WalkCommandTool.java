package NG.Entities;

import NG.ActionHandling.MouseTools.DefaultMouseTool;
import NG.Engine.Game;
import NG.MonsterSoul.Commands.Command;
import NG.MonsterSoul.Commands.CommandWalk;
import NG.MonsterSoul.MonsterSoul;
import org.joml.Vector2i;
import org.joml.Vector3fc;
import org.joml.Vector3i;

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
        Vector3i coord = game.map().getCoordinate(position);
        Command command = new CommandWalk(controller, new Vector2i(coord.x, coord.y));
        controller.accept(command);
    }
}
