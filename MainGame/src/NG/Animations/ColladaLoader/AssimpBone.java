package NG.Animations.ColladaLoader;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AINode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Geert van Ieperen created on 6-5-2019.
 */
public class AssimpBone {
    public final String name;

    private AINode data;
    private List<AssimpBone> children;

    public AssimpBone(AINode source) {
        this.data = source;
        this.children = new ArrayList<>();
        this.name = source.mName().dataString();

        PointerBuffer childArrayPointer = source.mChildren();
        assert childArrayPointer != null;

        for (int i = 0; i < source.mNumChildren(); i++) {
            AINode childNode = AINode.create(childArrayPointer.get(i));
            // bones do not have meshes, but mesh-nodes do
            if (childNode.mNumMeshes() == 0) {
                children.add(new AssimpBone(childNode));
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

    public Collection<AssimpBone> children() {
        return Collections.unmodifiableList(children);
    }
}
