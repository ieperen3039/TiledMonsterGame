package NG.Animations;

import NG.Animations.ColladaLoader.Converter;
import NG.Storable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Geert van Ieperen created on 6-3-2019.
 */
public class AnimationCombiner implements Animation {
    private final Map<AnimationBone, Animation> mux;
    private final float duration;

    public AnimationCombiner(Path... binaries) {
        mux = new HashMap<>();
        float duration = 0;

        for (Path path : binaries) {
            Animation part = Converter.loadAnimation(path.toFile());
            duration = part.duration();
            add(part);
        }

        this.duration = duration;
    }

    public AnimationCombiner(Animation... parts) {
        mux = new HashMap<>();
        duration = parts[0].duration();

        for (Animation part : parts) {
            assert part.duration() == duration;
            add(part);
        }
    }

    public void add(Animation part) {
        for (AnimationBone bone : part.getDomain()) {
            mux.put(bone, part);
        }
    }

    @Override
    public Matrix4fc transformationOf(AnimationBone bone, float timeSinceStart) {
        Animation animation = mux.get(bone);
        if (animation == null) {
            return new Matrix4f();
        }
        return animation.transformationOf(bone, timeSinceStart);
    }

    @Override
    public float duration() {
        return duration;
    }

    @Override
    public Set<AnimationBone> getDomain() {
        return mux.keySet();
    }

    @Override
    public void writeToDataStream(DataOutput out) throws IOException {
        Collection<Animation> values = mux.values();
        Storable.writeCollection(out, new HashSet<>(values));
    }

    public AnimationCombiner(DataInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        this.mux = new HashMap<>(size);
        float duration = 0;

        for (int i = 0; i < size; i++) {
            Animation ani = Storable.read(in, Animation.class);
            duration = ani.duration();
            add(ani);
        }

        this.duration = duration;
    }
}
