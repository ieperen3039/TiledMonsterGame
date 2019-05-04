package NG.Animations;

import NG.Animations.ColladaLoader.JointData;
import NG.Entities.Entity;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import NG.Tools.Toolbox;
import org.joml.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Math;
import java.util.*;
import java.util.function.Consumer;

/**
 * a rotation of a child bone on a given relative position of some parent animation bone, specifying an initial rotation
 * and additional rotation angles. Objects of this type are immutable
 */
public class AnimationBone implements Storable {
    private final String name;
    private final Matrix4fc transformation;
    private final Collection<AnimationBone> subElements;
    private Integer hashCash = null;

    /**
     * @param name     the name of this bone
     * @param offset   position relative to the last joint
     * @param rotation the rotation of this joint relative to the rotation of the last joint
     * @param childs   the child nodes of this joint/bone
     */
    public AnimationBone(
            String name, Vector3fc offset, Quaternionfc rotation, Vector3fc scaling, List<AnimationBone> childs
    ) {
        this.transformation = new Matrix4f().translationRotateScale(offset, rotation, scaling);
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
    public AnimationBone(
            String name, float xPos, float yPos, float zPos, float xRot, float yRot, float zRot, float angle,
            float scaling, AnimationBone... childs
    ) {
        this(
                name, new Vector3f(xPos, yPos, zPos),
                (angle == 0 ?
                        new Quaternionf() :
                        new Quaternionf().rotateAxis((float) Math.toRadians(angle), xRot, yRot, zRot)
                ),
                new Vector3f(scaling),
                Arrays.asList(childs));
    }

    public AnimationBone(JointData skeletonData) {
        this.name = skeletonData.name;
        this.transformation = skeletonData.bindLocalTransform;
        this.subElements = new ArrayList<>();

        for (JointData child : skeletonData.children) {
            if (child.name.endsWith(".IK")) continue;
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
                name.equals(other.name) &&
                subElements.containsAll(other.subElements);
    }

    @Override
    public int hashCode() {
        if (hashCash == null) {
            int childHash = Arrays.hashCode(subElements.toArray(new AnimationBone[0]));
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
            SGL gl, Entity entity, Map<AnimationBone, BoneElement> elements, UniversalAnimation animation,
            float animationTime
    ) {
        gl.pushMatrix();
        {
            gl.multiplyAffine(transformation);
            Toolbox.drawAxisFrame(gl);

            Matrix4fc transformation = animation.transformationOf(this, animationTime);
            if (transformation == null) {
                throw new NullPointerException(String.format("Animation %s has no support for %s", animation, this));
            }
            gl.multiplyAffine(transformation); // TODO commented out for testing

            BoneElement bone = elements.get(this);
            if (bone != null) {
                bone.draw(gl, entity);
            }

            for (AnimationBone elt : subElements) {
                elt.draw(gl, entity, elements, animation, animationTime);
            }
        }
        gl.popMatrix();
    }

    /**
     * depth-first execution of the action
     */
    public void forEach(Consumer<AnimationBone> action) {
        action.accept(this);
        for (AnimationBone e : subElements) {
            e.forEach(action);
        }
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

        for (AnimationBone elt : subElements) {
            elt.writeTree(builder, depth + 1);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        out.writeUTF(name);
        Storable.writeMatrix4f(out, transformation);

        out.writeInt(subElements.size());
        for (AnimationBone elt : subElements) {
            elt.writeToDataStream(out);
        }
    }

    public AnimationBone(DataInputStream in) throws IOException {
        name = in.readUTF();
        transformation = Storable.readMatrix4f(in);

        int nOfElts = in.readInt();
        subElements = new ArrayList<>(nOfElts);
        for (int i = 0; i < nOfElts; i++) {
            subElements.add(new AnimationBone(in));
        }
    }

    /**
     * @return the inverse of the local transformation of this bone
     */
    public Matrix4fc getInverseTransform() {
        return transformation.invertAffine(new Matrix4f());
    }

    /**
     * Allows easily building a map (AnimationBone, BoneElement) using bone names and meshes.
     */
    public static class BodyBuilder {
        private final Map<AnimationBone, BoneElement> body;
        private final BodyModel model;

        public BodyBuilder(BodyModel model) {
            this.model = model;
            body = new HashMap<>();
        }

        /**
         * builds a new element on this body
         * @param name name of the bone
         * @param mesh the mesh to use
         * @return this
         */
        public BodyBuilder add(String name, BodyMesh mesh) {
            AnimationBone bone = model.getBone(name);

            // checks
            assert bone != null : "Unknown bone " + name;
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
        public Map<AnimationBone, BoneElement> forEachRemaining(Consumer<AnimationBone> handler) {
            Set<AnimationBone> bonesLeft = new HashSet<>(model.getElements());
            bonesLeft.removeAll(body.keySet());
            bonesLeft.forEach(handler);
            return get();
        }

        /**
         * returns the built mapping. Future calls to add will write through to the retrieved map.
         * @return the resulting map
         */
        public Map<AnimationBone, BoneElement> get() {
            return body;
        }
    }

}
