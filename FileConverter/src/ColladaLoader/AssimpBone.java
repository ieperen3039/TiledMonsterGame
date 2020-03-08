package ColladaLoader;

import NG.Animations.SkeletonBone;
import NG.DataStructures.Generic.Color4f;
import NG.DataStructures.Generic.PairList;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import org.joml.*;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Geert van Ieperen created on 6-5-2019.
 */
public class AssimpBone {
    public final String name;

    private AINode data;
    private List<AssimpBone> childBones;
    private List<Integer> meshIndices;
    private List<MeshFile> meshes;

    public AssimpBone(AINode source) {
        this.data = source;
        this.childBones = new ArrayList<>();
        this.meshIndices = new ArrayList<>();
        this.name = source.mName().dataString();

        PointerBuffer childArrayPointer = source.mChildren();
        assert childArrayPointer != null;

        for (int i = 0; i < source.mNumChildren(); i++) {
            AINode childNode = AINode.create(childArrayPointer.get(i));

            // bones do not have meshes, but mesh-nodes do
            if (childNode.mNumMeshes() == 0) {
                childBones.add(new AssimpBone(childNode));

            } else {
                IntBuffer intBuffer = Objects.requireNonNull(childNode.mMeshes());
                for (int j = 0; j < childNode.mNumMeshes(); j++) {
                    meshIndices.add(intBuffer.get());
                }
            }
        }
    }

    public Matrix4fc getTransformation() {
        AIMatrix4x4 m = data.mTransformation();
        return new Matrix4f(
                m.a1(), m.a2(), m.a3(), m.a4(),
                m.b1(), m.b2(), m.b3(), m.b4(),
                m.c1(), m.c2(), m.c3(), m.c4(),
                m.d1(), m.d2(), m.d3(), m.d4()
        );
    }

    public List<AssimpBone> childBones() {
        return Collections.unmodifiableList(childBones);
    }

    public List<MeshFile> createMeshes(AIScene scene, SkeletonBone root) {
        if (meshes == null) {
            meshes = new ArrayList<>(meshIndices.size());

            PointerBuffer pointerBuffer = Objects.requireNonNull(scene.mMeshes());
            for (int i : meshIndices) {
                AIMesh aiMesh = AIMesh.create(pointerBuffer.get(i));
                int numBones = aiMesh.mNumBones();
                int numVertices = aiMesh.mNumVertices();

                ArrayList<PairList<SkeletonBone, Float>> influences = new ArrayList<>(numBones);

                for (int j = 0; j < numVertices; j++) {
                    influences.add(new PairList<>(4));
                }

                if (numBones > 0) {
                    List<PairList<SkeletonBone, Float>> newInfl = readInfluences(root, aiMesh);

                    for (int j = 0; j < numVertices; j++) {
                        influences.get(j).addAll(newInfl.get(j));
                    }
                }

                MeshFile mesh = toMeshFile(aiMesh, influences);

                meshes.add(mesh);
            }
        }

        return meshes;
    }

    /**
     * @return the influences of a set of skeleton bones on given indices of vertices. The influence of bone b on vertex
     * i is given as result.get(i).getRight(indexOfLeft(b))
     */
    public static List<PairList<SkeletonBone, Float>> readInfluences(SkeletonBone root, AIMesh mesh) {
        List<PairList<SkeletonBone, Float>> influences = new ArrayList<>();

        for (int i = 0; i < mesh.mNumVertices(); i++) {
            influences.add(new PairList<>(1));
        }

        PointerBuffer buffer = Objects.requireNonNull(mesh.mBones());
        for (int j = 0; j < mesh.mNumBones(); j++) {
            AIBone aiBone = AIBone.create(buffer.get());
            String boneName = aiBone.mName().dataString();
            SkeletonBone bone = root.findBone(boneName);

            AIVertexWeight.Buffer vertexWeights = aiBone.mWeights();
            for (int i = 0; i < aiBone.mNumWeights(); i++) {
                AIVertexWeight weight = vertexWeights.get();
                influences.get(weight.mVertexId()).add(bone, weight.mWeight());
            }
        }

        return influences;
    }

    public void forAll(Consumer<AssimpBone> action) {
        action.accept(this);
        for (AssimpBone b : childBones) {
            b.forAll(action);
        }
    }

    private static MeshFile toMeshFile(AIMesh mesh, List<PairList<SkeletonBone, Float>> influences) {
        String name = mesh.mName().dataString();
        int size = mesh.mNumVertices();

        List<Vector3fc> vertices = new ArrayList<>(size);
        List<Vector3fc> normals = new ArrayList<>();
        List<Mesh.Face> faces = new ArrayList<>(mesh.mNumFaces());
        List<Color4f> colors = new ArrayList<>();
        List<Vector2fc> textureCoords = new ArrayList<>();

        AIVector3D.Buffer vertexBuffer = mesh.mVertices();
        AIFace.Buffer faceBuffer = mesh.mFaces();
        AIVector3D.Buffer normalBuffer = mesh.mNormals();
        PointerBuffer textureBuffer = mesh.mTextureCoords();
        PointerBuffer colorBuffer = mesh.mColors();

        for (int j = 0; j < size; j++) {
            AIVector3D aiVector = vertexBuffer.get();
            vertices.add(new Vector3f(aiVector.x(), aiVector.y(), aiVector.z()));
        }
        for (int j = 0; j < mesh.mNumFaces(); j++) {
            AIFace aiFace = faceBuffer.get();
            faces.add(getFace(aiFace));
        }

        if (normalBuffer != null) {
            for (int j = 0; j < size; j++) {
                AIVector3D aiVector = normalBuffer.get();
                normals.add(new Vector3f(aiVector.x(), aiVector.y(), aiVector.z()));
            }
        }

        if (mesh.mTextureCoords(0) != null) {
            for (int j = 0; j < size; j++) {
                AIVector3D aiVector = AIVector3D.create(textureBuffer.get());
                textureCoords.add(new Vector2f(aiVector.x(), aiVector.y()));
            }
        }
        if (mesh.mColors(0) != null) {
            for (int j = 0; j < size; j++) {
                AIColor4D aiColor = AIColor4D.create(colorBuffer.get());
                colors.add(new Color4f(aiColor.r(), aiColor.g(), aiColor.b(), aiColor.a()));
            }
        }

        return new MeshFile(name, vertices, normals, faces, textureCoords, colors, influences);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + name;
    }

    private static Mesh.Face getFace(AIFace aiFace) {
        IntBuffer intBuffer = aiFace.mIndices();
        int[] indices = new int[aiFace.mNumIndices()];
        intBuffer.get(indices);
        return new Mesh.Face(indices, indices, indices, indices);
    }

    public SkeletonBone toSkeletonBone() {
        ArrayList<SkeletonBone> elts = new ArrayList<>();
        for (AssimpBone child : childBones()) {
            if (child.name.endsWith(".IK")) continue;
            elts.add(child.toSkeletonBone()); // recursively add children
        }

        elts.trimToSize();
        return new SkeletonBone(name, elts, getTransformation());
    }
}
