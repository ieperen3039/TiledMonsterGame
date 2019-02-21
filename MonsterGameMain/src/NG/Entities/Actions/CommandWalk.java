package NG.Entities.Actions;

import NG.Engine.Game;
import NG.Entities.MonsterEntity;
import NG.GameMap.GameMap;
import NG.MonsterSoul.Living;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3i;

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
    public EntityAction toAction(Game game, MonsterEntity entity) {
        GameMap map = game.map();
        Vector3f lastActionEndPos = entity.getLastQueuedAction().getEndPosition();
        Vector3i lastActionEndCoord = map.getCoordinate(lastActionEndPos);
        Vector2ic beginPosition = new Vector2i(lastActionEndCoord.x, lastActionEndCoord.y);

        List<Vector2i> path = map.findPath(beginPosition, target, 1f, 0.5f); // TODO entity parameters

        if (path.isEmpty()) {
            // already there, but return a non-null action
            return new ActionIdle(game, beginPosition);
        }

        Vector2ic lastPos = beginPosition;
        EntityAction[] actions = new EntityAction[path.size()];

        for (int i = 0; i < actions.length; i++) {
            Vector2i pos = path.get(i);
            float walkSpeed = 1;//entity.stat(WALK_SPEED);

            actions[i] = new ActionWalk(game, lastPos, pos, walkSpeed);

            lastPos = pos;
        }

        return new CompoundAction(actions);
    }
}
