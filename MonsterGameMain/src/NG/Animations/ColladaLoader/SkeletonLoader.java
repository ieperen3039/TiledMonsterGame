package NG.Animations.ColladaLoader;

import org.joml.Matrix4f;

public class SkeletonLoader {

    private XmlNode armatureData;

    public SkeletonLoader(XmlNode armature) {
        this.armatureData = armature;
    }

    public JointData extractBoneData() {
        return loadJointData(armatureData, true);
    }

    private JointData loadJointData(XmlNode jointNode, boolean isRoot) {
        JointData joint = extractMainJointData(jointNode, isRoot);

        for (XmlNode childNode : jointNode.getChildren("node")) {
            joint.addChild(loadJointData(childNode, false));
        }

        return joint;
    }

    private JointData extractMainJointData(XmlNode jointNode, boolean isRoot) {
        String name = jointNode.getAttribute("id").replace("Armature_", ""); // identifier used in animations

        XmlNode transform = jointNode.getChild("matrix");
        Matrix4f matrix;

        if (transform == null) {
            matrix = new Matrix4f();

        } else {
            String[] matrixData = transform.getData().split(" ");
            matrix = ColladaLoader.parseFloatMatrix(matrixData, 0);

            if (isRoot) {
                matrix.rotateXYZ(0, 0, (float) Math.toRadians(-90));
            }
        }

        return new JointData(name, matrix);
    }

}
