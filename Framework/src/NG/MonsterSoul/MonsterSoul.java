package NG.MonsterSoul;

import NG.DataStructures.Direction;
import NG.Engine.Game;
import NG.Entities.CubeMonster;
import NG.Entities.MonsterEntity;
import NG.Tools.Toolbox;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public class MonsterSoul implements Living {
    private MonsterEntity currentEntity;
    private List<Direction> plannedMovement = new ArrayList<>();

    public MonsterEntity getAsEntity(Game game, Vector2i coordinate, Vector3fc direction) {
        if (currentEntity != null) {
            currentEntity.dispose();
        }

        currentEntity = new CubeMonster(game, coordinate, direction, this);
        plannedMovement.clear();
        return currentEntity;
    }

    public void update() {

    }

    @Override
    public void command(Command command) {
        // consider to reject

        // if command is accepted
        if (command instanceof MoveCommand) {
            Vector2ic tgt = ((MoveCommand) command).getTargetPosition();
            // pathfinding
            plannedMovement.add(Direction.NONE); // anything but this
        }
    }

    @Override
    public void accept(Stimulus stimulus) {
        // react
    }

    public Direction targetDirection() {
        int r = Toolbox.random.nextInt(50);
        if (r < 5) return Direction.values()[r];
        return Direction.NONE;
    }

//    private enum Emotions {
//        CURIOUSNESS, FRIGHT, RESPECT, CONFIDENCE
//    }
}
