package NG.Animations.ColladaLoader;

import NG.Animations.AnimationBone;
import NG.Animations.BodyModel;
import NG.Animations.KeyFrameAnimation;
import NG.Tools.Vectors;
import org.joml.Matrix4f;

import java.io.File;
import java.io.IOException;

public class ColladaLoader {
    private XmlNode animNode;
    private SkeletonLoader jointsLoader = null;
    private final XmlNode jointsNode;

    public ColladaLoader(File colladaFile) throws IOException {
        XmlNode node = XmlParser.loadXmlFile(colladaFile);
        animNode = node.getChild("library_animations");
        XmlNode sceneElements = node.getChild("library_visual_scenes").getChild("visual_scene");
        jointsNode = sceneElements.getChildWithAttribute("node", "id", "Armature");

        assert jointsNode != null;
    }

    public AnimationBone loadSkeleton(String bodyModel) {
        JointData skeletonData = getJointsLoader(bodyModel).getBoneData();
        return new AnimationBone(skeletonData);
    }

    private SkeletonLoader getJointsLoader(String bodyModel) {
        if (jointsLoader == null) jointsLoader = new SkeletonLoader(jointsNode, bodyModel);
        return jointsLoader;
    }

    public KeyFrameAnimation loadAnimation(BodyModel bodyModel) {
        if (animNode == null) throw new IllegalStateException();
        AnimationLoader loader = new AnimationLoader(animNode, getJointsLoader(bodyModel.toString()), bodyModel);
        return new KeyFrameAnimation(loader.boneMapping(), loader.duration(), bodyModel);
    }

    static Matrix4f parseFloatMatrix(String[] rawData, int startIndex) {
        float[] matrixData = new float[16];
        for (int i = 0; i < 16; i++) {
            String elt = rawData[startIndex + i];
            matrixData[i] = Float.parseFloat(elt);
        }

        return Vectors.toMatrix4f(matrixData).transpose();
    }

}
