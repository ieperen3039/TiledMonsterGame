package NG.Animations.ColladaLoader;


import NG.Animations.BodyModel;
import NG.Tools.Toolbox;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.*;

public class AnimationLoader {
    private XmlNode animationData;
    private BodyModel model;
    private float maxFrameTime = 0;

    public AnimationLoader(XmlNode animationData, BodyModel model) {
        this.animationData = animationData;
        this.model = model;
    }

    public Map<String, TransformList> boneMapping() {
        Map<String, TransformList> mapping = new HashMap<>();

        List<XmlNode> animationNodes = animationData.getChildren("animation");
        for (XmlNode node : animationNodes) {
            mapping.put(getJointName(node), getJoint(node));
        }

        return mapping;
    }

    private TransformList getJoint(XmlNode jointData) {
        XmlNode timeNode = getSampler(Sampler.INPUT, jointData);
        String timeData = timeNode.getChild("float_array").getData();
        String[] rawTimes = Toolbox.WHITESPACE_PATTERN.split(timeData);
        int nrOfFrames = rawTimes.length;

        XmlNode frameNode = getSampler(Sampler.OUTPUT, jointData);
        String frameData = frameNode.getChild("float_array").getData();
        String[] rawFrames = Toolbox.WHITESPACE_PATTERN.split(frameData);
        assert rawFrames.length == (nrOfFrames * 16);

        TransformList joint = new TransformList();

        for (int i = 0; i < nrOfFrames; i++) {
            float time = Float.parseFloat(rawTimes[i]);
            Matrix4fc frame = ColladaLoader.parseFloatMatrix(rawFrames, 16 * i);
            joint.add(time, frame);

            if (time > maxFrameTime) {
                maxFrameTime = time;
            }
        }

        return joint;
    }

    private String getJointName(XmlNode jointData) {
        XmlNode channelNode = jointData.getChild("channel");
        String data = channelNode.getAttribute("target");
        return data.split("/")[0].replaceAll("Armature_", "");
    }

    private XmlNode getSampler(Sampler sampler, XmlNode jointData) {
        XmlNode node = jointData.getChild("sampler").getChildWithAttribute("input", "semantic", sampler.toString());
        String samplerName = node.getAttribute("source").substring(1);
        return jointData.getChildWithAttribute("source", "id", samplerName);
    }

    enum Sampler {
        INPUT, OUTPUT, INTERPOLATION
    }

    public float duration() {
        return maxFrameTime;
    }

    public static class TransformList {
        // these lists are always sorted
        private final ArrayList<Float> timestamps;
        private final ArrayList<Matrix4fc> frames;

        private TransformList() {
            frames = new ArrayList<>();
            timestamps = new ArrayList<>();
        }

        public void add(float time, Matrix4fc frame) {
            int index = Collections.binarySearch(timestamps, time);

            if (index < 0) {
                // index = -(insertion point) - 1  <=>  insertion point = -index - 1
                index = -index - 1;
            }

            timestamps.add(index, time);
            frames.add(index, new Matrix4f(frame));
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
