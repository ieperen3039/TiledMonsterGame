package NG.Actions.Commands;

import NG.Actions.ActionJump;
import NG.Actions.ActionWalk;
import NG.Actions.EntityAction;
import NG.Core.Game;
import NG.GameMap.GameMap;
import NG.InputHandling.MouseTools.CommandProvider;
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

    public CommandWalk(Living receiver, Vector2ic position) {
        super(receiver);
        this.target = new Vector2i(position);
    }

    @Override
    public EntityAction getAction(Game game, Vector3fc beginPosition, float gameTime) {
        final float walkSpeed = 2f;
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
            return new ActionJump(beginPosition, map.getPosition(beginCoord), walkSpeed * 1.5f);
        }
        return new ActionWalk(game, beginPosition, beginCoord, walkSpeed);
    }

    public static CommandProvider walkCommand() {
        return new CommandProvider("Walk") {
            @Override
            public Command create(Living receiver, Vector2ic target) {
                return new CommandWalk(receiver, target);
            }
        };
    }
}
