package NG.Actions.Commands;

import NG.Actions.ActionJump;
import NG.Actions.ActionWalk;
import NG.Actions.EntityAction;
import NG.Engine.Game;
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

    public CommandWalk(Living source, Living target, Vector2ic position) {
        super(source, target);
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

        if (!path.hasNext()) {
            Vector3fc tgtPos = map.getPosition(beginCoord);
            if (Vectors.almostEqual(tgtPos, beginPosition)) return null; // already there

            if (beginPosition.z() > map.getHeightAt(beginCoord)) {
                return new ActionJump(beginPosition, map.getPosition(beginCoord), walkSpeed);
            }
            return new ActionWalk(game, beginPosition, beginCoord, walkSpeed);
        }

        return new ActionWalk(game, beginPosition, path.next(), walkSpeed);
    }

}