package NG.MonsterSoul.Commands;

import NG.Engine.Game;
import NG.Entities.Actions.ActionWalk;
import NG.Entities.Actions.EntityAction;
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

    public CommandWalk(Living source, Vector2ic target) {
        super(source);
        this.target = new Vector2i(target);
    }

    @Override
    public List<EntityAction> toActions(Game game, EntityAction preceding) {
        Vector2ic beginPosition = preceding.getEndPosition();

        if (beginPosition.equals(target)) {
            // already there, return an empty list of actions
            return Collections.emptyList();
        }

        List<Vector2i> path = game.map().findPath(beginPosition, target, 1f, 0.5f); // TODO entity parameters

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
