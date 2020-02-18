package NG.Actions.Commands;

import NG.Actions.ActionJump;
import NG.Actions.ActionWalk;
import NG.Actions.EntityAction;
import NG.Core.Game;
import NG.GameMap.GameMap;
import NG.Living.Living;
import NG.Tools.Vectors;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3fc;

import java.util.Iterator;

/**
 * @author Geert van Ieperen created on 13-2-2019.
 */
public class CommandWalk extends Command {
    private final Vector2ic target;

    public CommandWalk(Living source, Living receiver, Vector2ic position) {
        super(source, receiver);
        this.target = new Vector2i(position);
    }

    @Override
    public EntityAction getAction(Game game, Vector3fc beginPosition, float gameTime) {
        final float walkSpeed = 1f;
        GameMap map = game.get(GameMap.class);

        Vector2i beginCoord = map.getCoordinate(beginPosition);

        Iterator<Vector2i> path = map
                .findPath(beginCoord, target, walkSpeed, 0.1f)
                .iterator();

        if (path.hasNext()) {
            beginCoord = path.next();

        } else {
            Vector3fc tgtPos = map.getPosition(beginCoord);
            if (Vectors.almostEqual(tgtPos, beginPosition)) return null; // already there
        }

        if (beginPosition.z() > (map.getHeightAt(beginPosition.x(), beginPosition.y()) + EntityAction.ON_GROUND_EPSILON)) {
            return new ActionJump(beginPosition, map.getPosition(beginCoord), walkSpeed * 2);
        }
        return new ActionWalk(game, beginPosition, beginCoord, walkSpeed);
    }

    public static CommandSelection.CommandProvider walkCommand() {
        return new CommandSelection.CommandProvider("Walk") {
            @Override
            public Command create(Living source, Living receiver, Vector2ic target) {
                return new CommandWalk(source, receiver, target);
            }
        };
    }
}
