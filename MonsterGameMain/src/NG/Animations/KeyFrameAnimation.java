package NG.Animations;

import NG.Animations.ColladaLoader.AnimationData;
import NG.Animations.ColladaLoader.KeyFrameData;
import NG.Entities.Entity;
import NG.GameEvent.Actions.EntityAction;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3fc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public class KeyFrameAnimation implements Animation {
    public final BodyModel target;
    private final Map<AnimationBone, Integer> indices;
    private final float[] timeFrames;
    private final Quaternionf[][] keyFrames; // [joints][frames]
    private float duration;

    public KeyFrameAnimation(BodyModel target, AnimationData data) {
        this.target = target;
        int nrOfJoints = target.size();
        indices = new HashMap<>(nrOfJoints);
        KeyFrameData[] frames = data.keyFrames;
        keyFrames = new Quaternionf[nrOfJoints][frames.length];
        timeFrames = new float[frames.length];

        for (int j = 0; j < frames.length; j++) {
            KeyFrameData frame = frames[j];
            timeFrames[j] = frame.time;
        }

        List<AnimationBone> elements = target.getElements();
        for (int i = 0; i < nrOfJoints; i++) {
            AnimationBone bone = elements.get(i);
            indices.put(bone, i);

            for (int j = 0; j < frames.length; j++) {
                Matrix4f transform = frames[j].jointTransforms.get(bone.getName());
                // process transformation into a rotation
                keyFrames[i][j] = transform.getUnnormalizedRotation(new Quaternionf());
            }
        }

        duration = data.lengthSeconds;

    }

    @Override
    public void draw(SGL gl, Entity entity, EntityAction action, float timeSinceStart) {
        Map<AnimationBone, BoneElement> elements = entity.getBoneMapping();

        Vector3fc position = action.getPositionAfter(timeSinceStart);
        Quaternionf baseRotation = action.getRotation(timeSinceStart);

        gl.pushMatrix();
        {
            gl.rotate(baseRotation);
            target.root.draw(gl, entity, elements, this, timeSinceStart, position);
        }
        gl.popMatrix();
    }

    @Override
    public Quaternionf rotationOf(AnimationBone bone, float timeSinceStart) {
        if (timeSinceStart > duration) {
            throw new IllegalArgumentException("Time was " + timeSinceStart + " but duration is " + duration);
        }

        Integer index = indices.get(bone);
        if (index == null) throw new IllegalArgumentException("Bone " + bone + " is not a target of this animation");
        Quaternionf[] posLine = keyFrames[index];
        return interpolate(timeFrames, posLine, timeSinceStart);
    }

    public float getDuration() {
        return duration;
    }

    private Quaternionf interpolate(float[] timeStamps, Quaternionf[] positions, float timeSinceStart) {

        int index = Arrays.binarySearch(timeStamps, timeSinceStart);
        if (index > 0) return positions[index]; // precise element

        // index = -(insertion point) - 1  <=>  insertion point = -index - 1
        int lowerPoint = -index - 2;
        float deltaTime = timeStamps[lowerPoint + 1] - timeStamps[lowerPoint];
        float fraction = (timeSinceStart - timeStamps[lowerPoint]) / deltaTime;

        // TODO: advanced interpolation
        Quaternionf lowerPosition = positions[lowerPoint];
        Quaternionf higherPosition = positions[lowerPoint + 1];
        return new Quaternionf(lowerPosition).nlerpIterative(higherPosition, fraction, 0.1f);
    }

    @Override
    public void writeToFile(DataOutput out) throws IOException {
        String rootName = target.name();
        out.writeUTF(rootName);
        int nrOfIndices = indices.size();
        out.writeInt(nrOfIndices);

        for (AnimationBone bone : indices.keySet()) {
            out.writeUTF(bone.getName());
            out.writeInt(indices.get(bone));
        }

        out.writeInt(timeFrames.length);
        for (float frame : timeFrames) {
            out.writeFloat(frame);
        }

        out.writeInt(keyFrames.length);
        for (Quaternionf[] movement : keyFrames) {
            for (Quaternionf rot : movement) {
                Storable.writeQuaternionf(out, rot);
            }
        }

        out.writeFloat(duration);
    }

    /**
     * Reads an animation from the data input.
     * @param in the data input stream
     * @throws IOException if anything goes wrong
     */
    public KeyFrameAnimation(DataInput in) throws IOException {
        target = BodyModel.valueOf(in.readUTF());
        int nrOfIndices = in.readInt();

        indices = new HashMap<>(nrOfIndices);
        for (int i = 0; i < nrOfIndices; i++) {
            AnimationBone bone = AnimationBone.getByName(in.readUTF());
            int index = in.readInt();
            indices.put(bone, index);
        }

        int nrOfFrames = in.readInt();
        timeFrames = new float[nrOfFrames];
        for (int i = 0; i < nrOfFrames; i++) {
            timeFrames[i] = in.readFloat();
        }

        int nrOfJoints = in.readInt();
        keyFrames = new Quaternionf[nrOfJoints][nrOfFrames];
        for (int i = 0; i < nrOfJoints; i++) {
            for (int j = 0; j < nrOfFrames; j++) {
                keyFrames[i][j] = Storable.readQuaternionf(in);
            }
        }

        duration = in.readInt();
    }
}
