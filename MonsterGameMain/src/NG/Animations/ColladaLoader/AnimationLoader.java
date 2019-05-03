package NG.Animations.ColladaLoader;


import NG.Animations.AnimationBone;
import NG.Animations.BodyModel;
import NG.Tools.Toolbox;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.*;

/**
 * loads the animations of the given collada xml tree, and store it as a relative transformation of the given BodyModel
 */
public class AnimationLoader {
    private Map<AnimationBone, TransformList> mapping = new HashMap<>();
    private float maxFrameTime = 0;

    public AnimationLoader(XmlNode animationData, SkeletonLoader jointsLoader, BodyModel bodyModel) {
        List<XmlNode> animationNodes = animationData.getChildren("animation");
        for (XmlNode node : animationNodes) {
            String name = jointsLoader.getNameOf(idOf(node));
            assert name != null : idOf(node);
            AnimationBone bone = bodyModel.getBone(name);
            mapping.put(bone, getJointTransformations(node, bone));
        }
    }

    private TransformList getJointTransformations(XmlNode jointData, AnimationBone bone) {
        XmlNode timeNode = getSource(Sampler.INPUT, jointData);
        String timeData = timeNode.getChild("float_array").getData();
        String[] rawTimes = Toolbox.WHITESPACE_PATTERN.split(timeData);
        int nrOfFrames = rawTimes.length;

        XmlNode frameNode = getSource(Sampler.OUTPUT, jointData);
        String frameData = frameNode.getChild("float_array").getData();
        String[] rawFrames = Toolbox.WHITESPACE_PATTERN.split(frameData);
        assert rawFrames.length == (nrOfFrames * 16);

        TransformList joint = new TransformList();
        Matrix4fc inverse = bone.getInverseTransform();

        for (int i = 0; i < nrOfFrames; i++) {
            float time = Float.parseFloat(rawTimes[i]);
            Matrix4f frame = ColladaLoader.parseFloatMatrix(rawFrames, 16 * i);
            frame.mulLocal(inverse);

            joint.addNonCopy(time, frame);

            if (time > maxFrameTime) {
                maxFrameTime = time;
            }
        }

        return joint;
    }

    private String idOf(XmlNode jointData) {
        XmlNode channelNode = jointData.getChild("channel");
        String data = channelNode.getAttribute("target");
        return data.split("/")[0];
    }

    private XmlNode getSource(Sampler sampler, XmlNode animationNode) {
        XmlNode node = animationNode.getChild("sampler").getChildWithAttribute("input", "semantic", sampler.toString());
        String samplerName = node.getAttribute("source").substring(1);
        return animationNode.getChildWithAttribute("source", "id", samplerName);
    }

    enum Sampler {
        INPUT, OUTPUT, INTERPOLATION
    }

    public float duration() {
        return maxFrameTime;
    }

    public Map<AnimationBone, TransformList> boneMapping() {
        return mapping;
    }

    public static class TransformList {
        // these lists are always sorted
        public final ArrayList<Float> timestamps;
        public final ArrayList<Matrix4fc> frames;

        private TransformList() {
            frames = new ArrayList<>();
            timestamps = new ArrayList<>();
        }

        public void add(float time, Matrix4fc frame) {
            addNonCopy(time, new Matrix4f(frame));
        }

        public void addNonCopy(float time, Matrix4fc frame) {
            int index = Collections.binarySearch(timestamps, time);

            if (index < 0) {
                // index = -(insertion point) - 1  <=>  insertion point = -index - 1
                index = -index - 1;
            }

            timestamps.add(index, time);
            frames.add(index, frame);
        }

        public float[] getTimestamps() {
            float[] floats = new float[timestamps.size()];
            for (int i = 0; i < timestamps.size(); i++) {
                floats[i] = timestamps.get(i);
            }
            return floats;
        }

        public Matrix4fc[] getFrames() {
            return frames.toArray(new Matrix4fc[0]);
        }
    }
}
