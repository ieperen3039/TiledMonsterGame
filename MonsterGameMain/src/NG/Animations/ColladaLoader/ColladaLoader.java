package NG.Animations.ColladaLoader;

import NG.Tools.Logger;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ColladaLoader {
    private XmlNode node;

    public ColladaLoader(Path colladaFile) {
        try {
            node = XmlParser.loadXmlFile(colladaFile);

        } catch (IOException e) {
            Logger.ERROR.print(e);
        }
    }

    public JointData loadColladaSkeleton() {
        XmlNode controllers = node.getChild("library_controllers");

        XmlNode skinningData = controllers.getChild("controller").getChild("skin");
        XmlNode inputNode = skinningData.getChild("vertex_weights");

        String jointDataId = inputNode.getChildWithAttribute("input", "semantic", "JOINT")
                .getAttribute("source")
                .substring(1);

        XmlNode jointsNode = skinningData.getChildWithAttribute("source", "id", jointDataId).getChild("Name_array");
        String[] names = jointsNode.getData().split(" ");

        List<String> boneOrder = Arrays.asList(names);
        XmlNode skeletonNode = node.getChild("library_visual_scenes");
        SkeletonLoader jointsLoader = new SkeletonLoader(skeletonNode, boneOrder);

        return jointsLoader.extractBoneData();
    }

    public AnimationData loadColladaAnimation() {
        XmlNode animNode = node.getChild("library_animations");
        XmlNode jointsNode = node.getChild("library_visual_scenes");
        AnimationLoader loader = new AnimationLoader(animNode, jointsNode);
        return loader.extractAnimation();
    }

    public static Matrix4f parseMatrix(String[] rawData) {
        Matrix4f matrix = new Matrix4f();

        float[] matrixData = new float[16];
        for (int i = 0; i < matrixData.length; i++) {
            matrixData[i] = Float.parseFloat(rawData[i]);
        }
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        buffer.put(matrixData);
        buffer.flip();
        matrix.set(buffer);
        return matrix;
    }
}
