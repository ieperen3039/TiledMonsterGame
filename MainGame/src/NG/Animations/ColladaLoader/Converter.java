package NG.Animations.ColladaLoader;

import NG.Animations.*;
import NG.Storable;
import NG.Tools.Directory;
import NG.Tools.Logger;

import java.io.*;
import java.nio.file.Path;

/**
 * @author Geert van Ieperen created on 1-3-2019.
 */
public class Converter {

    /** reloads all animations and skeletons */
    public static void main(String[] args) {
        rewriteSkeleton("anthro.skelbi");

        rewriteAnimation("walkCycle.anibi", BodyModel.ANTHRO, "walkCycleAnthro.anibi");
    }


    /**
     * creates a skelbi file from a equally named collada file (.dae) in the collada folder
     * @param target the .skelbi file to rewrite
     * @see Directory#colladaFiles
     */
    public static void rewriteSkeleton(String target) {
        String colladaFile = target.replace(".skelbi", ".dae");
        File source = Directory.colladaFiles.getFile(colladaFile);
        if (!source.exists()) throw new IllegalArgumentException(source.getPath());
        File skeletonFile = Directory.skeletons.getFile(target);

        Logger.INFO.print("Converting " + source);
        // extract from collada file
        AssimpScene scene = new AssimpScene(source);
        SkeletonBone root = new SkeletonBone(scene.getSkeleton());
        scene.dispose();
        root.writeToFile(skeletonFile);
    }

    /**
     * writes an universal animation from several collada files (.dae). Each collada file contains (possibly several)
     * armatures and animations. The used armatures must be known.
     * @param target    the target file to write
     * @param bodyModel
     * @param sources   which files should be used
     */
    public static void rewriteAnimation(String target, BodyModel bodyModel, String... sources) {
        Path[] paths = new Path[sources.length];

        for (int i = 0; i < sources.length; i++) {
            Path path = paths[i] = Directory.animations.getPath(sources[i]);

            File file = path.toFile();
            String source = file.getName().replace(".anibi", ".dae");
            File colladaFile = Directory.colladaFiles.getFile(source);
            if (!colladaFile.exists()) throw new IllegalArgumentException(colladaFile.getPath());

            // extract from collada file

            try (AssimpScene scene = new AssimpScene(colladaFile)) {
                // only 1 animation
                AssimpAnimation animation = scene.getAnimations().get(0);
                KeyFrameAnimation anim = new KeyFrameAnimation(animation, bodyModel);
                anim.writeToFile(file);
            }
        }

        PartialAnimation animation = new AnimationCombiner(paths);

        File targetFile = Directory.animations.getFile(target);
        animation.writeToFile(targetFile);
    }

    public static PartialAnimation loadAnimation(File file) {
        String fileName = file.getName();

        // create binary if absent
        if (!file.exists()) { // TODO read description file
//            String source = fileName.replace(".anibi", ".dae");
//            File colladaFile = Directory.colladaFiles.getFile(source);

            throw new RuntimeException("animation not found: " + fileName);
        }

        // load from binary
        PartialAnimation animation;
        try (InputStream fileStream = new FileInputStream(file)) {
            DataInputStream in = new DataInputStream(fileStream);
            animation = Storable.read(in, PartialAnimation.class);

        } catch (IOException | ClassNotFoundException e) {
//                Logger.ERROR.print(e);
            throw new RuntimeException(e);
        }

        return animation;
    }
}
