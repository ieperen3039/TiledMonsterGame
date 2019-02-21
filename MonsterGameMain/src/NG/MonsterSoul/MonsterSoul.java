package NG.MonsterSoul;

import NG.Engine.Game;
import NG.Entities.Actions.Command;
import NG.Entities.CubeMonster;
import NG.Entities.MonsterEntity;
import NG.Storable;
import org.joml.Vector2i;
import org.joml.Vector3fc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public class MonsterSoul implements Living, Storable {
    private MonsterEntity currentEntity;

    private Emotion.Collection emotions; // TODO initialize

    public MonsterSoul(Path emotions) {
        this.emotions = new Emotion.Collection(emotions);
    }

    public MonsterEntity getAsEntity(Game game, Vector2i coordinate, Vector3fc direction) {
        if (currentEntity != null) {
            currentEntity.dispose();
        }

        currentEntity = new CubeMonster(game, coordinate, direction, this);
        return currentEntity;
    }

    public void update() {

    }

    @Override
    public void command(Command command) {
        // consider to reject
//        if (emotions.get(Emotion.RESPECT) > 100) ; // todo ideas for making this work

        currentEntity.execute(command, this);
    }

    @Override
    public void accept(Stimulus stimulus) {
        // react
    }

    @Override
    public void writeToFile(DataOutput out) throws IOException {
        emotions.writeToFile(out);
    }

    public MonsterSoul(DataInput in) throws IOException {
        emotions = new Emotion.Collection(in);
    }
}
