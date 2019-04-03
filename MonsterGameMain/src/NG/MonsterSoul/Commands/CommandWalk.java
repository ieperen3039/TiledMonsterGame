package NG.MonsterSoul.Commands;

import NG.Actions.ActionWalk;
import NG.Actions.EntityAction;
import NG.Engine.Game;
import NG.GameMap.GameMap;
import NG.MonsterSoul.Living;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Geert van Ieperen created on 13-2-2019.
 */
public class CommandWalk extends Command {
    private final Vector2ic target;

    public CommandWalk(Living source, Living target, Vector2ic position) {
        super(source, target);
        this.target = new Vector2i(position);
    }

    @Override
    public List<EntityAction> toActions(Game game, EntityAction preceding) {
        Vector2ic beginPosition = preceding.getEndCoordinate();

        if (beginPosition.equals(target)) {
            // already there, return an empty list of actions
            return Collections.emptyList();
        }

        List<Vector2i> path = game.get(GameMap.class)
                .findPath(beginPosition, target, 1f, 0.5f); // TODO entity parameters

        Vector2ic lastPos = beginPosition;
        List<EntityAction> actions = new ArrayList<>(path.size());

        float walkSpeed = 1;//entity.stat(WALK_SPEED);
        for (Vector2i pos : path) {
            actions.add(new ActionWalk(game, lastPos, pos, walkSpeed));
            lastPos = pos;
        }

        return actions;
    }
}
