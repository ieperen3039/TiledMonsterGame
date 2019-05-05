package NG.Animations;

import NG.Storable;
import NG.Tools.Directory;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.io.DataOutputStream;
import java.io.File;
import java.util.Collections;
import java.util.Set;

/**
 * @author Geert van Ieperen created on 6-3-2019.
 */
public enum BodyAnimation implements UniversalAnimation {
    //    WALK_START("walkStart.anibi"),
    WALK_CYCLE("walkCycle.anibi"),
    //    WALK_END("walkStop.anibi"),
    BASE_POSE(basePose()),

    ;

    private final PartialAnimation animation;

    BodyAnimation(String... filePath) {
        File file = Directory.animations.getFile(filePath);
        animation = Storable.readFromFileRequired(file, PartialAnimation.class);
    }

    BodyAnimation(PartialAnimation baked) {
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


    public static PartialAnimation basePose() {
        return new PartialAnimation() {
            @Override
            public Matrix4fc transformationOf(AnimationBone bone, float timeSinceStart) {
                return new Matrix4f();
            }

            @Override
            public float duration() {
                return Float.POSITIVE_INFINITY;
            }

            @Override
            public Set<AnimationBone> getDomain() {
                return Collections.emptySet();
            }

            @Override
            public void writeToDataStream(DataOutputStream out) {
            }
        };
    }
}
