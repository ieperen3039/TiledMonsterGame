package NG.Animations;

import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.*;
import java.util.stream.Stream;

/**
 * a rotation of a child bone on a given relative position of some parent animation bone, specifying an initial rotation
 * and additional rotation angles. Objects of this type are immutable
 */
public class AnimationBone {
    private static final HashMap<String, AnimationBone> knownBones = new HashMap<>();

    private final Vector3fc offset;
    private final Quaternionfc baseRotation;
    private final Collection<AnimationBone> subElements;
    private Integer hashCash = null;

    /**
     * @param name         the
     * @param offset       position relative to the last joint
     * @param baseRotation the rotation of this joint relative to the rotation of the last joint
     * @param childs       the child nodes of this joint/bone
     */
    public AnimationBone(String name, Vector3fc offset, Quaternionfc baseRotation, List<AnimationBone> childs) {
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

    public boolean isRoot() {
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
     * @param gl            the current gl renderer
     * @param entity        the entity that is being drawn
     * @param elements      a mapping of bone descriptions to implementations.
     * @param parentScale   the scaling of the bone this is attached to. Only applies for the initial offset
     * @param animationTime the time since the start of this bone's animation in seconds
     */
    public void draw(
            SGL gl, Entity entity, Map<AnimationBone, BoneElement> elements, Animation animation, Vector3fc parentScale,
            float animationTime
    ) {
        gl.pushMatrix();
        {
            Vector3f jointPosition = new Vector3f(offset).mul(parentScale);
            gl.translate(jointPosition);
            gl.rotate(baseRotation);

            Vector3fc rotation = animation.rotationOf(this, animationTime);
            gl.rotateXYZ(rotation);

            BoneElement bone = elements.get(this);
            bone.draw(gl, entity);

            for (AnimationBone elt : subElements) {
                elt.draw(gl, entity, elements, animation, bone.scalingFactor(), animationTime);
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

    public int size() {
        int sum = 0;
        for (AnimationBone subElement : subElements) {
            sum += subElement.size();
        }
        return sum;
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
