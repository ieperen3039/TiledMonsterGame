package NG.Animations.ColladaLoader;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.assimp.Assimp.*;

/**
 * @author Geert van Ieperen created on 6-5-2019.
 */
public class AssimpScene implements AutoCloseable {
    private final AIScene scene;

    public AssimpScene(File file) {
        AIPropertyStore importProperties = Objects.requireNonNull(aiCreatePropertyStore());
        Assimp.aiSetImportPropertyInteger(importProperties, AI_CONFIG_IMPORT_COLLADA_USE_COLLADA_NAMES, 1);

        scene = Assimp.aiImportFileExWithProperties(
                file.getPath(),
                aiProcess_Triangulate | aiProcess_JoinIdenticalVertices |
                        aiProcess_LimitBoneWeights | aiProcess_ImproveCacheLocality,
                null,
                importProperties
        );

        aiReleasePropertyStore(importProperties);
    }

    public AssimpBone getSkeleton() {
        AINode source = Objects.requireNonNull(scene.mRootNode());
        return new AssimpBone(source);
    }

    public List<AssimpAnimation> getAnimations() {
        ArrayList<AssimpAnimation> result = new ArrayList<>();
        PointerBuffer aniArrayPointer = scene.mAnimations();
        assert aniArrayPointer != null;

        for (int i = 0; i < scene.mNumAnimations(); i++) {
            AIAnimation ani = AIAnimation.create(aniArrayPointer.get(i));
            result.add(new AssimpAnimation(ani));
        }

        return result;
    }

    public void dispose() {
    }

    @Override
    public void close() {
        Assimp.aiReleaseImport(scene);
    }
}
