package ColladaLoader;

import NG.Animations.BodyModel;
import NG.Animations.KeyFrameAnimation;
import NG.DataStructures.Generic.PairList;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Geert van Ieperen created on 6-5-2019.
 */
public class AssimpAnimation {
    private final Map<String, PairList<Float, Matrix4f>> animation = new HashMap<>();

    private float duration;
    private String animationName;

    public AssimpAnimation(AIAnimation rootStruct) {
        this.duration = (float) (rootStruct.mDuration() * rootStruct.mTicksPerSecond());
        this.animationName = rootStruct.mName().dataString();

        PointerBuffer nodeArray = Objects.requireNonNull(rootStruct.mChannels());
        for (int i = 0; i < rootStruct.mNumChannels(); i++) {
            Map<Float, Matrix4f> boneMap = new HashMap<>();
            AINodeAnim node = AINodeAnim.create(nodeArray.get(i));
            String nodeName = node.mNodeName().dataString();

            if (node.mNumRotationKeys() == 0 && node.mNumPositionKeys() == 0) continue;

            AIVectorKey.Buffer positionArray = Objects.requireNonNull(node.mPositionKeys());
            for (int j = 0; j < node.mNumPositionKeys(); j++) {
                AIVectorKey vectorKey = positionArray.get(j);
                float time = (float) vectorKey.mTime();
                AIVector3D aiVector = vectorKey.mValue();
                Vector3fc position = new Vector3f(aiVector.x(), aiVector.y(), aiVector.z());
                boneMap.put(time, new Matrix4f().translation(position));
            }

            AIQuatKey.Buffer rotationArray = Objects.requireNonNull(node.mRotationKeys());
            for (int j = 0; j < node.mNumRotationKeys(); j++) {
                AIQuatKey quatKey = rotationArray.get(j);
                float time = (float) quatKey.mTime();
                AIQuaternion aiQuat = quatKey.mValue();
                Quaternionf position = new Quaternionf(aiQuat.x(), aiQuat.y(), aiQuat.z(), aiQuat.w());
                boneMap.computeIfAbsent(time, t -> new Matrix4f()).rotate(position);
            }

            AIVectorKey.Buffer scalingArray = Objects.requireNonNull(node.mScalingKeys());
            for (int j = 0; j < node.mNumScalingKeys(); j++) {
                AIVectorKey vectorKey = scalingArray.get(j);
                float time = (float) vectorKey.mTime();
                AIVector3D aiVector = vectorKey.mValue();
                Vector3fc scaling = new Vector3f(aiVector.x(), aiVector.y(), aiVector.z());
                boneMap.computeIfAbsent(time, t -> new Matrix4f()).scale(scaling);
            }

            PairList<Float, Matrix4f> boneList = new PairList<>(boneMap);
            animation.put(nodeName, boneList);
        }
    }

    public String name() {
        return animationName;
    }

    public float duration() {
        return duration;
    }

    public Collection<? extends String> bones() {
        return animation.keySet();
    }

    public PairList<Float, Matrix4f> get(String boneName) {
        return animation.get(boneName);
    }

    public KeyFrameAnimation toKeyFrames(BodyModel bodyModel) {
        return new KeyFrameAnimation(bodyModel, animation, duration);
    }
}
