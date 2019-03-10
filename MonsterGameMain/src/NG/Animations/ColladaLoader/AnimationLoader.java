package NG.Animations.ColladaLoader;


import org.joml.Matrix4f;

import java.util.List;

public class AnimationLoader {
    private XmlNode animationData;
    private String rootJointName;

    public AnimationLoader(XmlNode animationData, String rootJointName) {
        this.animationData = animationData;
        this.rootJointName = rootJointName;
    }

    public AnimationData extractAnimation() {
        float[] times = getKeyTimes();
        float duration = times[times.length - 1];
        KeyFrameData[] keyFrames = initKeyFrames(times);

        List<XmlNode> animationNodes = animationData.getChildren("animation");
        for (XmlNode jointNode : animationNodes) {
            loadJointTransforms(keyFrames, jointNode, rootJointName);
        }

        return new AnimationData(duration, keyFrames);
    }

    private float[] getKeyTimes() {
        XmlNode timeData = animationData.getChild("animation").getChild("source").getChild("float_array");
        String[] rawTimes = timeData.getData().split(" ");
        float[] times = new float[rawTimes.length];
        for (int i = 0; i < times.length; i++) {
            times[i] = Float.parseFloat(rawTimes[i]);
        }
        return times;
    }

    private KeyFrameData[] initKeyFrames(float[] times) {
        KeyFrameData[] frames = new KeyFrameData[times.length];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = new KeyFrameData(times[i]);
        }
        return frames;
    }

    private void loadJointTransforms(KeyFrameData[] frames, XmlNode jointData, String rootNodeId) {
        String jointNameId = getJointName(jointData);
        String dataId = getDataId(jointData);
        XmlNode transformData = jointData.getChildWithAttribute("source", "id", dataId);
        String[] rawData = transformData.getChild("float_array").getData().split(" ");
        processTransforms(jointNameId, rawData, frames, jointNameId.equals(rootNodeId));
    }

    private String getDataId(XmlNode jointData) {
        XmlNode node = jointData.getChild("sampler").getChildWithAttribute("input", "semantic", "OUTPUT");
        return node.getAttribute("source").substring(1);
    }

    private String getJointName(XmlNode jointData) {
        XmlNode channelNode = jointData.getChild("channel");
        String data = channelNode.getAttribute("target");
        return data.split("/")[0].replaceAll("Armature_", "");
    }

    private void processTransforms(String jointName, String[] rawData, KeyFrameData[] keyFrames, boolean root) {
        for (KeyFrameData keyFrame : keyFrames) {
            Matrix4f transform = ColladaLoader.parseFloatMatrix(rawData);

            if (root) {
                transform.rotateXYZ(0, 0, (float) Math.toRadians(-90));
            }

            keyFrame.jointTransforms.put(jointName, transform);
        }
    }
}
