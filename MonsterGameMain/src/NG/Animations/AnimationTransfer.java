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
    private float transScaling;
    private final float origScaling;

    /**
     * A transfer into another animation. The preceding animation is defined externally.
     * @param original        the animation to transfer into
     * @param transfer        the replacing animation
     * @param connectFraction what fraction of the original transformation is replaced
     */
    private AnimationTransfer(UniversalAnimation original, UniversalAnimation transfer, float connectFraction) {
        this.original = original;
        this.transfer = transfer;

        // unscaled duration of the combined animation
        float origPart = (1 - connectFraction) * original.duration();
        float duration = transfer.duration() + origPart;
        transScaling = original.duration() / duration;

        float remain = duration * (1 - connectFraction);
        origScaling = origPart / remain;
    }

    @Override
    public Matrix4fc transformationOf(AnimationBone bone, float timeSinceStart) {

        // time in the unscaled combination
        float adjustedTSS = timeSinceStart * transScaling;

        if (adjustedTSS < transfer.duration()) {
            return transfer.transformationOf(bone, adjustedTSS);

        } else {
            adjustedTSS = timeSinceStart * origScaling;
            return original.transformationOf(bone, adjustedTSS);
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
     * @param secondAni       the animation that the replacement transfers into
     * @param replacement     the animation that transfers first to second
     * @param connectFraction what fraction of the animation of second is skipped. The {@code result} animation is
     *                        concatenated with the last {@code connectFraction} of the second animation. If 1, the
     *                        entire animation of second is skipped. The combination is scaled to match the original
     *                        duration.
     */
    public static void add(
            UniversalAnimation firstAni, UniversalAnimation secondAni, BodyAnimation replacement, float connectFraction
    ) {
        AnimationTransfer value = new AnimationTransfer(secondAni, replacement, connectFraction);
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
