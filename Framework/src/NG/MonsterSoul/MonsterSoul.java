package NG.MonsterSoul;

import NG.DataStructures.Direction;
import NG.DataStructures.Storable;
import NG.Engine.Game;
import NG.Entities.CubeMonster;
import NG.Entities.MonsterEntity;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3fc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public class MonsterSoul implements Living, Storable {
    private MonsterEntity currentEntity;
    private Queue<Direction> plannedMovement = new ArrayDeque<>();

    public MonsterSoul(Path emotions) {
        this.emotions = new Emotion.Collection(emotions);
    }

    private Emotion.Collection emotions; // TODO initialize

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
        if (emotions.get(Emotion.RESPECT) > 100) ; // todo ideas for making this work

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
        if (plannedMovement.isEmpty()) {
            return Direction.NONE;
        } else {
            return plannedMovement.remove();
        }
    }

    @Override
    public void writeToFile(DataOutput out) throws IOException {
        emotions.writeToFile(out);
    }

    @Override
    public void readFromFile(DataInput in) throws IOException {
        emotions.readFromFile(in);
    }
}
