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

    /** reloads all partial animations */
    public static void main(String[] args) {
        rewriteSkeleton("anthro.skelbi");
        BodyModel.values();
        rewriteAnimation("walkStart.anibi", "walk1.anibi");
        rewriteAnimation("walkCycle.anibi", "walk2.anibi");
        rewriteAnimation("walkStop.anibi", "walk3.anibi");
    }


    public static void colladaToBinary(File source, File skeletonFile, File animationFile) {
        if (!source.exists()) throw new IllegalArgumentException(source.getPath());
        Logger.INFO.print("Converting " + source);

        try {
            // extract from collada file
            ColladaLoader loader = new ColladaLoader(source);

            AnimationBone root = loader.loadSkeleton();
            KeyFrameAnimation anim = loader.loadAnimation(root);

            // store binary
            if (skeletonFile != null) root.writeToFile(skeletonFile);
            if (animationFile != null) anim.writeToFile(animationFile);

        } catch (IOException e) {
            Logger.ERROR.print(e);
        }
    }

    public static void rewriteSkeleton(String target) {
        String colladaFile = target.replace(".skelbi", ".dae");
        File source = Directory.colladaFiles.getFile(colladaFile);
        File skeletonFile = Directory.skeletons.getFile(target);
        colladaToBinary(source, skeletonFile, null);
    }

    public static void rewriteAnimation(String target, String... sources) {
        Path[] paths = new Path[sources.length];

        for (int i = 0; i < sources.length; i++) {
            Path path = paths[i] = Directory.animations.getPath(sources[i]);

            File file = path.toFile();
            String source = file.getName().replace(".anibi", ".dae");
            File colladaFile = Directory.colladaFiles.getFile(source);
            colladaToBinary(colladaFile, null, file);
        }

        Animation animation = new AnimationCombiner(paths);

        File targetFile = Directory.animations.getFile(target);
        animation.writeToFile(targetFile);
    }

    protected static AnimationBone getAnthro() {
        return new AnimationBone(
                "anthro_torso",
                0, 0, 0,
                0, 0, 0,
                new AnimationBone(
                        "anthro_head",
                        -0.5f, 0f, 6.5f,
                        0, 0, 0,
                        new AnimationBone(
                                "anthro_ear_left",
                                0.0f, 1.0f, 3.2f,
                                0, 0, 0
                        ),
                        new AnimationBone(
                                "anthro_ear_right",
                                0.0f, -1.0f, 3.2f,
                                0, 0, 0
                        )
                ),
                new AnimationBone(
                        "anthro_upper_arm_left",
                        -1.0f, 5.0f, 3.5f,
                        90, 0, 90,
                        new AnimationBone(
                                "anthro_lower_arm_left",
                                0, 0, 8.0f,
                                0, 0, 0
                        )
                ),
                new AnimationBone(
                        "anthro_upper_arm_right",
                        -1.0f, -5.0f, 3.5f,
                        90, 0, -90,
                        new AnimationBone(
                                "anthro_lower_arm_right",
                                0, 0, 8.0f,
                                0, 0, 0
                        )
                ),
                new AnimationBone(
                        "anthro_upper_leg_left",
                        0, 2.0f, -7.0f,
                        180, 0, 0,
                        new AnimationBone(
                                "anthro_lower_leg_left",
                                0, 0, 8.0f,
                                0, 0, 0,
                                new AnimationBone(
                                        "anthro_foot_left",
                                        0, 0, 8.0f,
                                        0, 0, 0
                                )
                        )
                ),
                new AnimationBone(
                        "anthro_upper_leg_right",
                        0, -2.0f, -7.0f,
                        -180, 0, 0, // this makes no difference to anthro_upper_leg_left, but for the idea...
                        new AnimationBone(
                                "anthro_lower_leg_right",
                                0, 0, 8.0f,
                                0, 0, 0,
                                new AnimationBone(
                                        "anthro_foot_right",
                                        0, 0, 8.0f,
                                        0, 0, 0
                                )
                        )
                )
        );
    }

    public static void createIfAbsent(File file) {
        String fileName = file.getName();

        if (!file.exists()) {
            File colladaFile;
            File animationFile;
            File skeletonFile;

            if (fileName.endsWith(".skelbi")) {
                String source = fileName.replace(".skelbi", ".dae");
                String anim = fileName.replace(".skelbi", ".anibi");

                colladaFile = Directory.colladaFiles.getFile(source);
                animationFile = Directory.animations.getFile(anim);
                skeletonFile = file;

            } else if (fileName.endsWith(".anibi")) {
                String source = fileName.replace(".anibi", ".dae");
                String skel = fileName.replace(".anibi", ".skelbi");

                colladaFile = Directory.colladaFiles.getFile(source);
                animationFile = file;
                skeletonFile = Directory.skeletons.getFile(skel);

            } else {
                throw new IllegalArgumentException(file + " is not a .skelbi nor .anibi file");
            }

            colladaToBinary(colladaFile, skeletonFile, animationFile);
        }
    }

    public static Animation loadAnimation(File file) {
        String fileName = file.getName();

        // create binary if absent
        if (!file.exists()) {
            String source = fileName.replace(".anibi", ".dae");
            File colladaFile = Directory.colladaFiles.getFile(source);

            colladaToBinary(colladaFile, null, file);
        }

        // load from binary
        Animation animation;
        try (InputStream fileStream = new FileInputStream(file)) {
            DataInput in = new DataInputStream(fileStream);
            animation = Storable.read(in, Animation.class);

        } catch (IOException | ClassNotFoundException e) {
//                Logger.ERROR.print(e);
            throw new RuntimeException(e);
        }

        return animation;
    }
}
