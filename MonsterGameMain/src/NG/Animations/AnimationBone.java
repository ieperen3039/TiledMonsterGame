package NG.Animations;

import NG.Animations.ColladaLoader.JointData;
import NG.Entities.Entity;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import org.joml.Matrix4fc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * a rotation of a child bone on a given relative position of some parent animation bone, specifying an initial rotation
 * and additional rotation angles. Objects of this type are immutable
 */
public class AnimationBone implements Storable {
    private final String name;
    private final Collection<AnimationBone> subElements;
    private Integer hashCash = null;

    /**
     * @param name     the name of this bone
     * @param childs   the child nodes of this joint/bone
     */
    public AnimationBone(
            String name, List<AnimationBone> childs
    ) {
        this.name = name;
        this.subElements = childs;
    }

    /**
     * a helper-constructor for recursively defining a bone tree in program code
     * @param name    unique name of this bone
     * @param childs  a summation of the child nodes of this bone.
     */
    public AnimationBone(String name, AnimationBone... childs) {
        this(name, Arrays.asList(childs));
    }

    public AnimationBone(JointData skeletonData) {
        this.name = skeletonData.name;
        subElements = new ArrayList<>();

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
            Matrix4fc transformation = animation.transformationOf(this, animationTime);
            if (transformation == null) {
                throw new NullPointerException(String.format("Animation %s has no support for %s", animation, this));
            }
            gl.multiplyAffine(transformation);

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
        subElements.forEach(e -> e.forEach(action));
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
    public void writeToDataStream(DataOutput out) throws IOException {
        out.writeUTF(name);

        out.writeInt(subElements.size());
        for (AnimationBone elt : subElements) {
            elt.writeToDataStream(out);
        }
    }

    public AnimationBone(DataInput in) throws IOException {
        name = in.readUTF();

        int nOfElts = in.readInt();
        subElements = new ArrayList<>(nOfElts);
        for (int i = 0; i < nOfElts; i++) {
            subElements.add(new AnimationBone(in));
        }
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
