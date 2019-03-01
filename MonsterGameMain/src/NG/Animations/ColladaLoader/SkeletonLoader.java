package NG.Animations.ColladaLoader;

import org.joml.Matrix4f;

import java.util.List;

public class SkeletonLoader {

    private XmlNode armatureData;

    private List<String> boneOrder;

    public SkeletonLoader(XmlNode visualSceneNode, List<String> boneOrder) {
        this.armatureData = visualSceneNode.getChild("visual_scene").getChildWithAttribute("node", "id", "Armature");
        this.boneOrder = boneOrder;
    }

    public JointData extractBoneData() {
        XmlNode headNode = armatureData.getChild("node");
        return loadJointData(headNode, true);
    }

    private JointData loadJointData(XmlNode jointNode, boolean isRoot) {
        JointData joint = extractMainJointData(jointNode, isRoot);
        for (XmlNode childNode : jointNode.getChildren("node")) {
            joint.addChild(loadJointData(childNode, false));
        }
        return joint;
    }

    private JointData extractMainJointData(XmlNode jointNode, boolean isRoot) {
        String name = jointNode.getAttribute("id");
        name = name.replace("Armature_", "");
//        System.out.println("Name: "+ nameId);
//        System.out.println("Bones:");
//        for (String bone : boneOrder) {
//            System.out.println("  " + bone);
//        }
        int index = boneOrder.indexOf(name);
        String[] matrixData = jointNode.getChild("matrix").getData().split(" ");
        Matrix4f matrix = ColladaLoader.parseMatrix(matrixData);
        matrix.transpose();
        if (isRoot) {
            matrix.rotateXYZ(0, 0, (float) Math.toRadians(-90));
        }
        return new JointData(index, name, matrix);
    }

}
