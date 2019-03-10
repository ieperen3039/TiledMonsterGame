package NG.Animations;

import NG.GameEvent.Actions.ActionIdle;
import NG.Storable;
import NG.Tools.Directory;
import org.joml.Matrix4fc;

import java.io.*;
import java.util.Set;

/**
 * @author Geert van Ieperen created on 6-3-2019.
 */
public enum BodyAnimation implements Animation {
    WALK_START("walkStart.anibi"),
    WALK_CYCLE("walkCycle.anibi"),
    WALK_END("walkStop.anibi"),
    IDLE(ActionIdle.idleAnimation(0)),

    ;

    private final Animation animation;

    BodyAnimation(String... filePath) {
        File file = Directory.animations.getFile(filePath);

        if (!file.exists() || file.isDirectory()) {
            throw new RuntimeException("File " + file + " is missing");
        }

        // load from binary
        try (InputStream fileStream = new FileInputStream(file)) {
            DataInput in = new DataInputStream(fileStream);
            animation = Storable.read(in, Animation.class);

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Exception when loading file", e);
        }
    }

    BodyAnimation(Animation baked) {
        animation = baked;
    }

    @Override
    public Matrix4fc transformationOf(AnimationBone bone, float timeSinceStart) {
        return animation.transformationOf(bone, timeSinceStart);
    }

    @Override
    public float duration() {
        return animation.duration();
    }

    @Override
    public Set<AnimationBone> getDomain() {
        return animation.getDomain();
    }

    @Override
    public void writeToDataStream(DataOutput out) {
        // Storable#write() will not call this method as this is an Enum.
    }
}
