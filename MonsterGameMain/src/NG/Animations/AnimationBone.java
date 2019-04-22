package NG.Animations;

import NG.Animations.ColladaLoader.JointData;
import NG.Entities.Entity;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import NG.Tools.Vectors;
import org.joml.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * a rotation of a child bone on a given relative position of some parent animation bone, specifying an initial rotation
 * and additional rotation angles. Objects of this type are immutable
 */
public class AnimationBone implements Storable {
    private final String name;
    private final Vector3fc offset;
    private final Quaternionfc rotation;
    private final Vector3fc scaling;
    private final Collection<AnimationBone> subElements;
    private Integer hashCash = null;

    /**
     * @param name     the name of this bone
     * @param offset   position relative to the last joint
     * @param rotation the rotation of this joint relative to the rotation of the last joint
     * @param scaling
     * @param childs   the child nodes of this joint/bone
     */
    public AnimationBone(
            String name, Vector3fc offset, Quaternionfc rotation, Vector3fc scaling, List<AnimationBone> childs
    ) {
        this.name = name;
        this.offset = offset;
        this.rotation = rotation;
        this.scaling = scaling;
        this.subElements = childs;
    }

    /**
     * a helper-constructor for recursively defining a bone tree in program code
     * @param name   unique name of this bone
     * @param xPos   the x offset relative to the parent bone
     * @param yPos   the y offset relative to the parent bone
     * @param zPos   the z offset relative to the parent bone
     * @param xRot   the x rotation relative to the parent bone
     * @param yRot   the y rotation relative to the parent bone
     * @param zRot   the z rotation relative to the parent bone
     * @param childs a summation of the child nodes of this bone.
     */
    public AnimationBone(
            String name, float xPos, float yPos, float zPos, float xRot, float yRot, float zRot, AnimationBone... childs
    ) {
        this(
                name, new Vector3f(xPos, yPos, zPos),
                new Quaternionf().rotateXYZ(xRot, yRot, zRot),
                Vectors.Scaling.UNIFORM,
                Arrays.asList(childs));
    }

    public AnimationBone(JointData skeletonData) {
        this.name = skeletonData.name;
        Matrix4f transform = skeletonData.bindLocalTransform;

        offset = transform.getTranslation(new Vector3f());
        rotation = transform.getNormalizedRotation(new Quaternionf());
        scaling = transform.getScale(new Vector3f());
        // TODO test assumption of scaling = Scaling.UNIFORM

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
                offset.equals(other.offset) &&
                rotation.equals(other.rotation) &&
                subElements.containsAll(other.subElements);
    }

    @Override
    public int hashCode() {
        if (hashCash == null) {
            int childHash = Arrays.hashCode(subElements.toArray(new AnimationBone[0]));
            hashCash = Objects.hash(offset, rotation, childHash);
        }
        return hashCash;
    }

    /**
     * draw this bone and all of its children using the supplied mapping.
     * @param gl            the current gl renderer
     * @param entity        the entity that is being drawn
     * @param elements      a mapping of bone descriptions to implementations.
     * @param animationTime the time since the start of this bone's animation in seconds
     * @param parentScaling scaling of the parent bone in (x, y, z) direction. This scaling is NOT propagated
     */
    public void draw(
            SGL gl, Entity entity, Map<AnimationBone, BoneElement> elements, UniversalAnimation animation,
            float animationTime, Vector3fc parentScaling
    ) {
        gl.pushMatrix();
        {
            Vector3f realOffset = new Vector3f(offset).mul(parentScaling);
            gl.translate(realOffset);
            gl.rotate(rotation);

            Matrix4fc transformation = animation.transformationOf(this, animationTime);
            if (transformation == null) {
                throw new NullPointerException(String.format("Animation %s has no support for %s", animation, this));
            }
            gl.multiplyAffine(transformation);

            Vector3f eltScaling = new Vector3f(scaling);

            BoneElement bone = elements.get(this);
            if (bone != null) {
                bone.draw(gl, entity);
                eltScaling.mul(bone.scaling());
            }

            for (AnimationBone elt : subElements) {
                elt.draw(gl, entity, elements, animation, animationTime, eltScaling);
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
        Storable.writeVector3f(out, offset);
        Storable.writeQuaternionf(out, rotation);
        Storable.writeVector3f(out, scaling);

        out.writeInt(subElements.size());
        for (AnimationBone elt : subElements) {
            elt.writeToDataStream(out);
        }
    }

    public AnimationBone(DataInput in) throws IOException {
        name = in.readUTF();
        offset = Storable.readVector3f(in);
        rotation = Storable.readQuaternionf(in);
        scaling = Storable.readVector3f(in);

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

        /** @see #add(String, BodyMesh, float, float, float) */
        public BodyBuilder add(String name, BodyMesh mesh, Vector3fc size) {
            return add(name, mesh, size.x(), size.y(), size.z());
        }

        /**
         * builds a new element on this body
         * @param name  name of the bone
         * @param mesh  the mesh to use
         * @param xSize the real width of the final mesh in x direction
         * @param ySize the real width of the final mesh in y direction
         * @param zSize the real width of the final mesh in z direction
         * @return this
         */
        public BodyBuilder add(String name, BodyMesh mesh, float xSize, float ySize, float zSize) {
            AnimationBone bone = model.getBone(name);

            // checks
            assert bone != null : "Unknown bone " + name;
            assert !body.containsKey(bone) : bone + " was already part of the builder";

            Vector3f scaling = new Vector3f(xSize, ySize, zSize);
            scaling.div(mesh.getSize()).div(bone.scaling);

            BoneElement element = new BoneElement(mesh, scaling, Material.ROUGH);
            body.put(bone, element);
            return this;
        }

        /**
         * @param name      name of the bone
         * @param mesh      the mesh to use
         * @param meshScale the relative scaling applied to the mesh
         * @return this
         */
        public BodyBuilder add(String name, BodyMesh mesh, float meshScale) {
            return add(name, mesh, mesh.getSize().mul(meshScale));
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
