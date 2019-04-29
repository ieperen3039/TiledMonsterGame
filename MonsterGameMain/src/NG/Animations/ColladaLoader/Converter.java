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
        rewriteSkeleton("anthro.skelbi", "ANTHRO");

//        rewriteAnimation("walkStart.anibi", "walk1.anibi");
        rewriteAnimation("walkCycle.anibi", BodyModel.ANTHRO, "walkCycleAnthro.anibi");
//        rewriteAnimation("walkStop.anibi", "walk3.anibi");
    }


    /**
     * creates a skelbi file from a equally named collada file (.dae) in the collada folder
     * @param target    the .skelbi file to rewrite
     * @param bodyModel
     * @see Directory#colladaFiles
     */
    public static void rewriteSkeleton(String target, String bodyModel) {
        String colladaFile = target.replace(".skelbi", ".dae");
        File source = Directory.colladaFiles.getFile(colladaFile);
        File skeletonFile = Directory.skeletons.getFile(target);
        if (!source.exists()) throw new IllegalArgumentException(source.getPath());
        Logger.INFO.print("Converting " + source);

        try {
            // extract from collada file
            ColladaLoader loader = new ColladaLoader(source);

            AnimationBone root = loader.loadSkeleton(bodyModel);
            root.writeToFile(skeletonFile);

        } catch (IOException e) {
            Logger.ERROR.print(e);
        }
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

            try {
                File file = path.toFile();
                String source = file.getName().replace(".anibi", ".dae");
                File colladaFile = Directory.colladaFiles.getFile(source);
                if (!colladaFile.exists()) throw new IllegalArgumentException(colladaFile.getPath());

                // extract from collada file
                ColladaLoader loader = new ColladaLoader(colladaFile);
                KeyFrameAnimation anim = loader.loadAnimation(bodyModel);
                anim.writeToFile(file);

            } catch (IOException e) {
                Logger.ERROR.print(e);
            }
        }

        PartialAnimation animation = new AnimationCombiner(paths);

        File targetFile = Directory.animations.getFile(target);
        animation.writeToFile(targetFile);
    }

    public static AnimationBone getAnthro() {
        return new AnimationBone(
                "ANTHRO_Spine",
                new AnimationBone(
                        "ANTHRO_Head",
                        new AnimationBone(
                                "ANTHRO_Ear.L"
                        ),
                        new AnimationBone(
                                "ANTHRO_Ear.R"
                        )
                ),
                new AnimationBone(
                        "ANTHRO_UpperArm.L",
                        new AnimationBone(
                                "ANTHRO_LowerArm.L"
                        )
                ),
                new AnimationBone(
                        "ANTHRO_UpperArm.R",
                        new AnimationBone(
                                "ANTHRO_LowerArm.R"
                        )
                ),
                new AnimationBone(
                        "ANTHRO_UpperLeg.L",
                        new AnimationBone(
                                "ANTHRO_LowerLeg.L",
                                new AnimationBone(
                                        "ANTHRO_Foot.L"
                                )
                        )
                ),
                new AnimationBone(
                        "ANTHRO_UpperLeg.R",
                        // this makes no difference to anthro_upper_leg_left, but for the idea...
                        new AnimationBone(
                                "ANTHRO_LowerLeg.R",
                                new AnimationBone(
                                        "ANTHRO_Foot.R"
                                )
                        )
                )
        );
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
            DataInput in = new DataInputStream(fileStream);
            animation = Storable.read(in, PartialAnimation.class);

        } catch (IOException | ClassNotFoundException e) {
//                Logger.ERROR.print(e);
            throw new RuntimeException(e);
        }

        return animation;
    }
}
