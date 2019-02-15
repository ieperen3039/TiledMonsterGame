package NG.Entities.Actions;

import NG.Engine.Game;
import NG.Entities.MonsterEntity;
import NG.GameMap.GameMap;
import NG.MonsterSoul.Living;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.List;

/**
 * @author Geert van Ieperen created on 13-2-2019.
 */
public class CommandWalk extends Command {
    private final Vector2ic target;

    protected CommandWalk(Living source, Vector2ic target) {
        super(source);
        this.target = new Vector2i(target);
    }

    @Override
    public EntityAction toAction(Game game, float beginTime, Vector2ic beginPosition, MonsterEntity entity) {
        List<Vector2i> path = GameMap.findPath(beginPosition, target); // TODO pathfinding
        EntityAction[] actions = new EntityAction[path.size()];

        Vector2ic lastPos = beginPosition;
        float lastEndTime = beginTime;

        for (int i = 0; i < path.size(); i++) {
            Vector2i pos = path.get(i);
            float walkSpeed = 0;//entity.stat(WALK_SPEED);

            EntityAction a = new ActionWalk(game, lastPos, pos, walkSpeed);
            actions[i] = a;

            lastPos = pos;
            lastEndTime = a.getEndTime();
        }

        return new CompoundAction(actions);
    }
}
