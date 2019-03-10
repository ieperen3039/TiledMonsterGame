package NG.Animations.ColladaLoader;

import NG.Animations.AnimationBone;
import NG.Animations.KeyFrameAnimation;
import NG.Tools.Vectors;
import org.joml.Matrix4f;

import java.io.File;
import java.io.IOException;

public class ColladaLoader {
    private XmlNode animNode;
    private XmlNode jointsNode;

    public ColladaLoader(File colladaFile) throws IOException {
        XmlNode node = XmlParser.loadXmlFile(colladaFile);
        animNode = node.getChild("library_animations");
        XmlNode sceneElements = node.getChild("library_visual_scenes").getChild("visual_scene");
        jointsNode = sceneElements.getChildWithAttribute("node", "id", "Armature");

        assert animNode != null && jointsNode != null;
    }

    protected AnimationBone loadSkeleton() {
        SkeletonLoader jointsLoader = new SkeletonLoader(jointsNode);
        JointData skeletonData = jointsLoader.extractBoneData();
        return new AnimationBone(skeletonData);
    }

    protected KeyFrameAnimation loadAnimation(AnimationBone root) {
        AnimationLoader loader = new AnimationLoader(animNode, root.getName());
        AnimationData animationData = loader.extractAnimation();
        return new KeyFrameAnimation(animationData);
    }

    static Matrix4f parseFloatMatrix(String[] rawData) {
        float[] matrixData = new float[16];
        for (int i = 0; i < matrixData.length; i++) {
            matrixData[i] = Float.parseFloat(rawData[i]);
        }

        return Vectors.toMatrix4f(matrixData).transpose();
    }

}
