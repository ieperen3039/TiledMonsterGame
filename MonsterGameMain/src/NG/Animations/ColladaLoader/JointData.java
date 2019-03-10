package NG.Animations.ColladaLoader;

import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the extracted data for a single joint in the model. This stores the joint's index, name, and local bind
 * transform.
 * @author Karl
 */
public class JointData {

    public final String name;
    public final Matrix4f bindLocalTransform;

    public final List<JointData> children = new ArrayList<JointData>();

    public JointData(String name, Matrix4f bindLocalTransform) {
        this.name = name;
        this.bindLocalTransform = bindLocalTransform;
    }

    public void addChild(JointData child) {
        children.add(child);
    }

}
