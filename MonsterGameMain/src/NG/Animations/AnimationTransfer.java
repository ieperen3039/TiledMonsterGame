package NG.Animations;

import NG.Actions.EntityAction;
import org.joml.Matrix4fc;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 24-3-2019.
 */
public class AnimationTransfer implements UniversalAnimation {
    private static Map<UniversalAnimation, Map<UniversalAnimation, AnimationTransfer>> transferPairs = new HashMap<>();

    private UniversalAnimation original;
    private UniversalAnimation transfer;
    private float connectFraction;
    private float invScaling;

    public AnimationTransfer(UniversalAnimation original, UniversalAnimation transfer, float connectFraction) {
        this.original = original;
        this.transfer = transfer;

        float duration = transfer.duration() + ((1 - connectFraction) * original.duration());
        invScaling = original.duration() / duration;

        this.connectFraction = connectFraction;
    }

    @Override
    public Matrix4fc transformationOf(AnimationBone bone, float timeSinceStart) {
        float transferDuration = transfer.duration();

        float adjustedTSS = timeSinceStart * invScaling;

        if (adjustedTSS < transferDuration) {
            return transfer.transformationOf(bone, adjustedTSS);

        } else {
            float adjusted = (original.duration() * connectFraction) + ((adjustedTSS - transferDuration));
            assert adjusted < original.duration();
            return original.transformationOf(bone, adjusted);
        }
    }

    @Override
    public float duration() {
        return original.duration();
    }

    /**
     * adds an animation trio to the possible transfers. In general, the second animation should register the
     * transfers.
     * @param firstAni        the animation that is played by the first action
     * @param secondAni       the animation that is played by the second action
     * @param result          the animation that transfers first to second
     * @param connectFraction what fraction of the animation of second is skipped. The {@code result} animation is
     *                        concatenated with the last {@code connectFraction} of the second animation. If 1, the
     *                        entire animation of second is skipped. The combination is scaled to match the original
     *                        duration.
     */
    public static void add(
            UniversalAnimation firstAni, UniversalAnimation secondAni, BodyAnimation result, float connectFraction
    ) {
        AnimationTransfer value = new AnimationTransfer(secondAni, result, connectFraction);
        transferPairs.computeIfAbsent(firstAni, (k) -> new HashMap<>()).put(secondAni, value);
    }

    /**
     * returns the animation connecting the first action to the second action
     * @param first  the previous action
     * @param second the action that is going to be executed.
     * @return The animation that replaces the animation of second, or null if no such is registered.
     */
    public static AnimationTransfer get(EntityAction first, EntityAction second) {
        Map<UniversalAnimation, AnimationTransfer> map = transferPairs.get(first.getAnimation());
        return map == null ? null : map.getOrDefault(second.getAnimation(), null);
    }

    public static boolean contains(EntityAction first, EntityAction second) {
        Map map = transferPairs.get(first.getAnimation());
        return map != null && map.containsKey(second.getAnimation());
    }
}
