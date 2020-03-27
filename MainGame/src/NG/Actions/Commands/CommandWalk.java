package NG.Actions.Commands;

import NG.Actions.ActionJump;
import NG.Actions.ActionLinearMove;
import NG.Actions.ActionWalk;
import NG.Actions.EntityAction;
import NG.Core.Game;
import NG.Entities.MonsterEntity;
import NG.GameMap.GameMap;
import NG.Living.Living;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3fc;

import java.util.Iterator;

import static NG.Actions.EntityAction.ACCEPTABLE_DIFFERENCE;

/**
 * @author Geert van Ieperen created on 13-2-2019.
 */
public class CommandWalk extends Command {
    private final Vector2ic target;

    public CommandWalk(Living receiver, Vector2ic position) {
        super();
        this.target = new Vector2i(position);
    }

    @Override
    public EntityAction getAction(
            Game game, MonsterEntity entity, Vector3fc beginPosition, float gameTime
    ) {
        final float walkSpeed = entity.getController().props.walkSpeed;
        GameMap map = game.get(GameMap.class);

        Vector2i coordinate = map.getCoordinate(beginPosition);

        Iterator<Vector2i> path = map
                .findPath(coordinate, target, walkSpeed, 0.1f)
                .iterator();

        if (path.hasNext()) {
            coordinate = path.next();

        } else {
            Vector3fc tgtPos = map.getPosition(coordinate);
            if (Vectors.almostEqual(tgtPos, beginPosition)) return null; // already there
        }

        float startHeight = map.getHeightAt(beginPosition.x(), beginPosition.y());

        if (beginPosition.z() < startHeight - ACCEPTABLE_DIFFERENCE) {
            Logger.ASSERT.print("Entity below ground " + beginPosition);
            return new ActionLinearMove(beginPosition, map.getPosition(coordinate), 10f);
        }

        if (beginPosition.z() > (startHeight + ACCEPTABLE_DIFFERENCE)) {
            return new ActionJump(beginPosition, map.getPosition(coordinate), walkSpeed * 1.5f);
        }

        return new ActionWalk(game, beginPosition, coordinate, walkSpeed);
    }

}
