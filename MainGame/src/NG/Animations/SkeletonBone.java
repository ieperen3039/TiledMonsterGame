package NG.Animations;

import NG.Entities.Entity;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Resources.Resource;
import NG.Tools.Toolbox;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

/**
 * a rotation of a child bone on a given relative position of some parent animation bone, specifying an initial rotation
 * and additional rotation angles. Objects of this type are immutable
 */
public class SkeletonBone implements Serializable {
    private final String name; // 'local' per bodyModel
    private final Matrix4fc transformation;
    private final Collection<SkeletonBone> subElements;
    private Integer hashCash = null;

    // @formatter:off
    private static final Matrix4fc SWAP_AXIS = new Matrix4f(
            1.0f,   0,      0,      0,
            0,      0,      -1.0f,  0,
            0,      1.0f,   0,      0,
            0,      0,      0,      1.0f
    ); // @formatter:on
    private static final Matrix4fc UNSWAP_AXIS = new Matrix4f(SWAP_AXIS).invertAffine();


    /**
     * @param name           the name of this bone
     * @param childs         the child nodes of this joint/bone
     * @param transformation the full transformation of the bone
     */
    public SkeletonBone(
            String name, List<SkeletonBone> childs, Matrix4fc transformation
    ) {
        this.transformation = transformation;
        this.name = name;
        this.subElements = childs;
    }

    /**
     * a helper-constructor for recursively defining a bone tree in program code
     * @param name    unique name of this bone
     * @param xPos    the x offset relative to the parent bone
     * @param yPos    the y offset relative to the parent bone
     * @param zPos    the z offset relative to the parent bone
     * @param xRot    the x element of the rotation axis
     * @param yRot    the y element of the rotation axis
     * @param zRot    the z element of the rotation axis
     * @param angle   angle of rotation relative to the parent bone in degrees
     * @param scaling the scaling factor relative to the parent bone
     * @param childs  a summation of the child nodes of this bone.
     */
    public SkeletonBone(
            String name, float xPos, float yPos, float zPos, float xRot, float yRot, float zRot, float angle,
            float scaling, SkeletonBone... childs
    ) {
        Quaternionf rotation = angle == 0 ? new Quaternionf() : new Quaternionf().rotateAxis((float) Math.toRadians(angle), xRot, yRot, zRot);

        this.transformation = new Matrix4f().translationRotateScale(
                xPos, yPos, zPos,
                rotation.x, rotation.y, rotation.z, rotation.w,
                scaling
        );
        this.name = name;
        this.subElements = Arrays.asList(childs);
    }

    public boolean isLeaf() {
        return subElements.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SkeletonBone) {
            SkeletonBone other = (SkeletonBone) obj;
            return this.equals(other);
        }
        return false;
    }

    public boolean equals(SkeletonBone other) {
        return hashCode() == other.hashCode() &&
                name.equals(other.name) &&
                subElements.containsAll(other.subElements);
    }

    @Override
    public int hashCode() {
        if (hashCash == null) {
            int childHash = Arrays.hashCode(subElements.toArray(new SkeletonBone[0]));
            hashCash = Objects.hash(name, childHash);
        }
        return hashCash;
    }

    /**
     * draw this bone and all of its children using the supplied mapping.
     * @param gl            the current gl renderer
     * @param entity        the entity that is being drawn
     * @param elements      a mapping of bone descriptions to implementations.
     * @param animationTime the time since the start of this bone's animation in seconds
     */
    public void draw(
            SGL gl, Entity entity, Map<SkeletonBone, BoneElement> elements, UniversalAnimation animation,
            float animationTime
    ) {
        gl.pushMatrix();
        {
            gl.multiplyAffine(transformation);

            Matrix4fc transformation = animation.transformationOf(this, animationTime);
            if (transformation == null) {
                throw new NullPointerException(String.format("Animation %s has no support for %s", animation, this));
            }
            gl.multiplyAffine(transformation);

            BoneElement bone = elements.get(this);
            if (bone != null) {
                Toolbox.drawAxisFrame(gl);
                bone.draw(gl, entity);
            }

            for (SkeletonBone elt : subElements) {
                elt.draw(gl, entity, elements, animation, animationTime);
            }
        }
        gl.popMatrix();
    }

    /**
     * depth-first execution of the action
     */
    public void forAll(Consumer<SkeletonBone> action) {
        action.accept(this);
        for (SkeletonBone e : subElements) {
            e.forAll(action);
        }
    }

    public int nrOfChildren() {
        int sum = 0;
        for (SkeletonBone subElement : subElements) {
            sum += subElement.nrOfChildren() + 1;
        }
        return sum;
    }

    @Override
    public String toString() {
        return "Bone " + getName();
    }

    /**
     * @return a tree representation of this bone and subelements
     */
    public String asTree() {
        StringBuilder builder = new StringBuilder();
        writeTree(builder, 0);
        return builder.toString();
    }

    // the number of lines created is equal to the number of calls to this method
    private void writeTree(StringBuilder builder, int depth) {
        for (int i = 0; i < depth; i++) {
            builder.append('\t');
        }
        builder.append(name);
        builder.append('\n');

        for (SkeletonBone elt : subElements) {
            elt.writeTree(builder, depth + 1);
        }
    }

    public String getName() {
        return name;
    }

    /**
     * @return the inverse of the local transformation of this bone
     */
    public Matrix4fc getInverseTransform() {
        return transformation.invertAffine(new Matrix4f());
    }

    public SkeletonBone findBone(String boneName) {
        if (name.equals(boneName)) return this;

        for (SkeletonBone elt : subElements) {
            SkeletonBone found = elt.findBone(boneName);
            if (found != null) return found;
        }

        return null;
    }

    /**
     * Allows easily building a map (AnimationBone, BoneElement) using bone names and meshes.
     */
    public static class BodyBuilder {
        private final Map<SkeletonBone, BoneElement> body;
        private final BodyModel model;

        public BodyBuilder(BodyModel model) {
            this.model = model;
            body = new HashMap<>();
        }

        /**
         * builds a new element on this body
         * @param name canonical name of the bone
         * @param mesh the mesh to use
         * @return this
         */
        public BodyBuilder add(String name, Resource<Mesh> mesh) {
            SkeletonBone bone = model.getBone(name);

            // checks
            assert bone != null : "Unknown bone " + name + " (" + model.getElements() + ")";
            assert !body.containsKey(bone) : bone + " was already part of the builder";

            BoneElement element = new BoneElement(mesh, Material.ROUGH);
            body.put(bone, element);
            return this;
        }

        /**
         * Executes the handler for each bone in the model not yet assigned.
         * @param handler a handler than accepts all bones not yet assigned
         * @return the result of {@link #get()}
         */
        public Map<SkeletonBone, BoneElement> forEachRemaining(Consumer<SkeletonBone> handler) {
            Set<SkeletonBone> bonesLeft = new HashSet<>(model.getElements());
            bonesLeft.removeAll(body.keySet());
            bonesLeft.forEach(handler);
            return get();
        }

        /**
         * returns the built mapping. Future calls to add will write through to the retrieved map.
         * @return the resulting map
         */
        public Map<SkeletonBone, BoneElement> get() {
            return body;
        }
    }

}
