package ColladaLoader;

import NG.Animations.*;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Tools.Directory;
import NG.Tools.Logger;
import NG.Tools.SerializationTools;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Geert van Ieperen created on 1-3-2019.
 */
@SuppressWarnings("Duplicates")
public class Converter {

    /** reloads all animations and skeletons */
    public static void main(String[] args) throws IOException {
        rewriteSkeletonAndMeshes("anthro.dae");
        rewriteAnimation("walkCycle.anibi", BodyModel.ANTHRO, "walkCycleAnthro.dae");
    }

    public static void rewriteSkeletonAndMeshes(String source) throws IOException {
        File sourceFile = Directory.rawFiles.getFile(source);
        if (!sourceFile.exists()) throw new IllegalArgumentException(sourceFile.getPath());
        String fileBaseName = source.substring(0, source.lastIndexOf("."));

        Logger.INFO.print("Converting " + sourceFile);
        // extract from file
        AssimpScene scene = new AssimpScene(sourceFile);
        writeSkeleton(fileBaseName, scene);
        writeMapping(fileBaseName, scene);
        scene.close();
    }

    public static void writeSkeleton(String fileBaseName, AssimpScene scene) {
        SkeletonBone root = scene.getSkeleton().toSkeletonBone();
        File skeletonFile = Directory.skeletons.getFile(fileBaseName + ".skelbi");
        SerializationTools.writeToFile(skeletonFile, root);
    }

    public static void writeMapping(String fileBaseName, AssimpScene scene) throws IOException {
        List<MeshFile> meshes = scene.getMeshes();
        for (MeshFile mesh : meshes) {
            boolean hasMap = Directory.meshes.getFile(fileBaseName).mkdirs();
            assert hasMap;
            File meshFile = Directory.meshes.getFile(fileBaseName, fileBaseName + ".obj");
            mesh.writeOBJFile(meshFile);
        }
    }

    /**
     * writes an universal animation from several collada files (.dae). Each collada file contains an armature and
     * animations. The used armatures must be known.
     * @param target    the target file to write
     * @param bodyModel
     * @param sources   which files should be used
     */
    public static void rewriteAnimation(String target, BodyModel bodyModel, String... sources) {
        PartialAnimation[] partials = new PartialAnimation[sources.length];

        for (int i = 0; i < sources.length; i++) {
            String source = sources[i];
            File sourceFile = Directory.rawFiles.getFile(source);
            if (!sourceFile.exists()) throw new IllegalArgumentException(sourceFile.getPath());

            try (AssimpScene scene = new AssimpScene(sourceFile)) {
                // only 1 animation
                AssimpAnimation animation = scene.getAnimations().get(0);
                KeyFrameAnimation anim = animation.toKeyFrames(bodyModel);
                partials[i] = anim;
            }
        }

        PartialAnimation animation = new AnimationCombiner(partials);

        File targetFile = Directory.animations.getFile(target);
        SerializationTools.writeToFile(targetFile, animation);
    }
}
