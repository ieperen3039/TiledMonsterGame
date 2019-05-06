package NG.Animations;

import NG.Animations.ColladaLoader.Converter;
import NG.Storable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Geert van Ieperen created on 6-3-2019.
 */
public class AnimationCombiner implements UniversalAnimation, PartialAnimation {
    private final Map<SkeletonBone, PartialAnimation> mux;
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
        for (SkeletonBone bone : part.getDomain()) {
            mux.put(bone, part);
        }
    }

    @Override
    public Matrix4fc transformationOf(SkeletonBone bone, float timeSinceStart) {
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
    public Set<SkeletonBone> getDomain() {
        return mux.keySet();
    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        Collection<? extends Storable> box = mux.values();
        out.writeInt(box.size());
        for (Storable s : box) {
            Storable.writeSafe(out, s);
        }
    }

    public AnimationCombiner(DataInputStream in) throws IOException {
        int size = in.readInt();
        this.mux = new HashMap<>(size);
        float duration = 0;

        for (int i = 0; i < size; i++) {
            PartialAnimation ani = Storable.readSafe(in, PartialAnimation.class);
            if (ani == null) continue;
            duration = ani.duration();
            add(ani);
        }

        this.duration = duration;
    }
}
