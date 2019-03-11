package NG.Animations.ColladaLoader;

import NG.Animations.BodyModel;

/**
 * Contains the extracted data for an animation, which includes the length of the entire animation and the data for all
 * the keyframes of the animation.
 * @author Karl
 */
public class AnimationData {
    public final BodyModel model;
    public final float lengthSeconds;
    public final KeyFrameData[] keyFrames;

    public AnimationData(BodyModel model, float lengthSeconds, KeyFrameData[] keyFrames) {
        this.model = model;
        this.lengthSeconds = lengthSeconds;
        this.keyFrames = keyFrames;
    }

}
