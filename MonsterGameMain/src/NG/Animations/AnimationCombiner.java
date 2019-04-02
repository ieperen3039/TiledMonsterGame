package NG.Animations;

import NG.Animations.ColladaLoader.Converter;
import NG.Storable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Geert van Ieperen created on 6-3-2019.
 */
public class AnimationCombiner implements UniversalAnimation, PartialAnimation {
    private final Map<AnimationBone, PartialAnimation> mux;
    private final float duration;

    public AnimationCombiner(Path... binaries) {
        mux = new HashMap<>();
        float duration = 0;

        for (Path path : binaries) {
            PartialAnimation part = Converter.loadAnimation(path.toFile());
            duration = part.duration();
            add(part);
        }

        this.duration = duration;
    }

    public AnimationCombiner(PartialAnimation... parts) {
        mux = new HashMap<>();
        duration = parts[0].duration();

        for (PartialAnimation part : parts) {
            assert part.duration() == duration;
            add(part);
        }
    }

    public void add(PartialAnimation part) {
        for (AnimationBone bone : part.getDomain()) {
            mux.put(bone, part);
        }
    }

    @Override
    public Matrix4fc transformationOf(AnimationBone bone, float timeSinceStart) {
        PartialAnimation animation = mux.get(bone);
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
        Storable.writeCollection(out, mux.values());
    }

    public AnimationCombiner(DataInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        this.mux = new HashMap<>(size);
        float duration = 0;

        for (int i = 0; i < size; i++) {
            PartialAnimation ani = Storable.read(in, PartialAnimation.class);
            duration = ani.duration();
            add(ani);
        }

        this.duration = duration;
    }
}
