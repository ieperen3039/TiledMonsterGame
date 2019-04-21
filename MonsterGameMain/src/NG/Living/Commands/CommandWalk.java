package NG.Living.Commands;

import NG.Actions.ActionWalk;
import NG.Actions.EntityAction;
import NG.Engine.Game;
import NG.GameMap.GameMap;
import NG.Living.Living;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.joml.Vector3i;

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

        Vector3i beginCoord3d = map.getCoordinate(beginPosition);
        Vector2i beginCoord = new Vector2i(beginCoord3d.x, beginCoord3d.y);

        Iterator<Vector2i> path = map
                .findPath(beginCoord, target, walkSpeed, 0.1f)
                .iterator();

        if (!path.hasNext()) return null; // already there

        return new ActionWalk(game, beginCoord, path.next(), walkSpeed);
    }
}
