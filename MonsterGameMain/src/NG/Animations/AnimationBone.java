package NG.Animations;

import NG.Animations.ColladaLoader.JointData;
import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import NG.Tools.Vectors;
import org.joml.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * a rotation of a child bone on a given relative position of some parent animation bone, specifying an initial rotation
 * and additional rotation angles. Objects of this type are immutable
 */
public class AnimationBone implements Storable {
    private static final HashMap<String, AnimationBone> knownBones = new HashMap<>();

    private String name;
    private final Vector3fc offset;
    private final Quaternionfc baseRotation;
    private final Collection<AnimationBone> subElements;
    private Integer hashCash = null;

    /**
     * @param name         the name of this bone
     * @param offset       position relative to the last joint
     * @param baseRotation the rotation of this joint relative to the rotation of the last joint
     * @param childs       the child nodes of this joint/bone
     */
    public AnimationBone(String name, Vector3fc offset, Quaternionfc baseRotation, List<AnimationBone> childs) {
        this.name = name;
        this.offset = offset;
        this.baseRotation = baseRotation;
        this.subElements = childs;
        if (knownBones.containsKey(name)) {
            throw new IllegalArgumentException("Bone " + name + " already exists");
        }
        knownBones.put(name, this);
    }

    public AnimationBone(
            String name, Vector3fc offset, float xRot, float yRot, float zRot, AnimationBone... childs
    ) {
        this(name, offset, new Quaternionf().rotateXYZ(xRot, yRot, zRot), Arrays.asList(childs));
    }

    public AnimationBone(JointData skeletonData) {
        Matrix4f transform = skeletonData.bindLocalTransform;

        offset = transform.transformPosition(Vectors.newZeroVector());
        baseRotation = transform.getNormalizedRotation(new Quaternionf());
        subElements = new ArrayList<>();
        knownBones.put(skeletonData.name, this);

        for (JointData child : skeletonData.children) {
            subElements.add(new AnimationBone(child)); // recursively add children
        }
    }

    public boolean isLeaf() {
        return subElements.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AnimationBone) {
            AnimationBone other = (AnimationBone) obj;
            return this.equals(other);
        }
        return false;
    }

    public boolean equals(AnimationBone other) {
        return hashCode() == other.hashCode() &&
                offset.equals(other.offset) &&
                baseRotation.equals(other.baseRotation) &&
                subElements.containsAll(other.subElements);
    }

    @Override
    public int hashCode() {
        if (hashCash == null) hashCash = Objects.hash(offset, baseRotation, subElements);
        return hashCash;
    }

    /**
     * draw this bone and all of its children using the supplied mapping.
     * @param gl            the current gl renderer
     * @param entity        the entity that is being drawn
     * @param elements      a mapping of bone descriptions to implementations.
     * @param animationTime the time since the start of this bone's animation in seconds
     * @param position      the position of the joint where this bone is attached to.
     */
    public void draw(
            SGL gl, Entity entity, Map<AnimationBone, BoneElement> elements, Animation animation,
            float animationTime, Vector3fc position
    ) {
        gl.pushMatrix();
        {
            gl.translate(position);
            gl.rotate(baseRotation);

            Quaternionf rotation = animation.rotationOf(this, animationTime);
            gl.rotate(rotation);

            BoneElement bone = elements.get(this);
            bone.draw(gl, entity);
            Vector3fc scaling = bone.scalingFactor();

            for (AnimationBone elt : subElements) {
                Vector3f jointPosition = new Vector3f(elt.offset).mul(scaling);
                elt.draw(gl, entity, elements, animation, animationTime, jointPosition);
            }

        }
        gl.popMatrix();
    }

    public Stream<AnimationBone> stream() {
        return Stream.concat(
                Stream.of(this),
                subElements.stream()
                        .flatMap(AnimationBone::stream)
        );
    }

    public int nrOfChildren() {
        int sum = 0;
        for (AnimationBone subElement : subElements) {
            sum += subElement.nrOfChildren() + 1;
        }
        return sum;
    }

    @Override
    public String toString() {
        return "Bone " + getName();
    }

    protected String getName() {
        return name;
    }

    @Override
    public void writeToFile(DataOutput out) throws IOException {
        out.writeUTF(name);
        Storable.writeVector3f(out, offset);
        Storable.writeQuaternionf(out, baseRotation);
        out.writeInt(subElements.size());
        for (AnimationBone elt : subElements) {
            elt.writeToFile(out);
        }
    }

    public AnimationBone(DataInput in) throws IOException {
        name = in.readUTF();
        offset = Storable.readVector3f(in);
        baseRotation = Storable.readQuaternionf(in);

        int nOfElts = in.readInt();
        subElements = new ArrayList<>(nOfElts);
        for (int i = 0; i < nOfElts; i++) {
            subElements.add(new AnimationBone(in));
        }
    }

    /**
     * looks for the loaded bones and returns the bone with matching name if one exists
     * @param name the name of the bone
     * @return a bone with the specified name, or null if no such bone exists or if it isn't loaded yet.
     */
    public static AnimationBone getByName(String name) {
        return knownBones.get(name);
    }
}
