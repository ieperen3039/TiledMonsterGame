package NG.Animations.ColladaLoader;

import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SkeletonLoader {
    private Map<String, String> nameIDMapping = new HashMap<>();
    private JointData jointData;

    public SkeletonLoader(XmlNode armature, String model) {
        jointData = loadJointData(armature, model);
    }

    public JointData getBoneData() {
        return jointData;
    }

    private JointData loadJointData(XmlNode jointNode, String model) {
        JointData joint = extractMainJointData(jointNode, model);

        for (XmlNode childNode : jointNode.getChildren("node")) {
            JointData child = loadJointData(childNode, model);
            joint.addChild(child);
        }

        return joint;
    }

    private JointData extractMainJointData(XmlNode jointNode, String model) {
        String name = model + "_" + jointNode.getAttribute("name"); // name used in body mapping

        XmlNode transform = jointNode.getChild("matrix");
        Matrix4f matrix;

        if (transform == null) {
            matrix = new Matrix4f();

        } else {
            String[] matrixData = transform.getData().split(" ");
            try {
                matrix = ColladaLoader.parseFloatMatrix(matrixData, 0);
            } catch (Exception ex) {
                System.err.println(Arrays.toString(matrixData));
                throw ex;
            }
        }

        nameIDMapping.put(jointNode.getAttribute("id"), name);

        return new JointData(name, matrix);
    }

    public String getNameOf(String id) {
        return nameIDMapping.get(id);
    }
}
