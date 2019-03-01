package NG.Animations;

import NG.DataStructures.Interpolation.VectorInterpolator;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public class Animation {
    public final Map<AnimationBone, Joint> implementation;

    public Animation(Path path) {
        implementation = new HashMap<>();

    }

    public Vector3fc rotationOf(AnimationBone bone, float timeSinceStart) {
        Joint target = implementation.get(bone);
        if (target == null) throw new IllegalArgumentException("Bone " + bone + " is not a target of this animation");
        return target.interpolate(timeSinceStart);
    }

    /**
     * Interpolates a fixed, cyclic movement using interpolation. Not to be confused with {@link VectorInterpolator},
     * which interpolates dynamically on timestamps
     * @author Geert van Ieperen created on 28-2-2019.
     */
    private static class Joint {
        private Vector3fc[] positions;
        private float[] timeStamps; // sorted
        private float duration;

        public Vector3fc interpolate(float timeSinceBegin) {
            if (timeSinceBegin > duration) {
                throw new IllegalArgumentException("Time was " + timeSinceBegin + " but duration is " + duration());
            }

            int index = Arrays.binarySearch(timeStamps, timeSinceBegin);
            if (index > 0) return positions[index];

            // index = -(insertion point) - 1  <=>  insertion point = -index - 1
            int lowerPoint = -index - 2;
            float deltaTime = timeStamps[lowerPoint + 1] - timeStamps[lowerPoint];
            float fraction = (timeSinceBegin - timeStamps[lowerPoint]) / deltaTime;

            // TODO: advanced interpolation
            Vector3fc lowerPosition = positions[lowerPoint];
            Vector3fc higherPosition = positions[lowerPoint + 1];
            return new Vector3f(lowerPosition).lerp(higherPosition, fraction);
        }

        public float duration() {
            return duration;
        }
    }
}
