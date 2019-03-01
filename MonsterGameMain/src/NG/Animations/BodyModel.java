package NG.Animations;

import NG.Animations.ColladaLoader.ColladaLoader;
import NG.Animations.ColladaLoader.JointData;
import NG.Tools.Directory;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public enum BodyModel {
    ANTHRO(
            "anthro.dae"
    ),

    ;

    public static final String BINARY_SKELETON_EXT = ".skelbi"; //
    public final AnimationBone root;
    private final List<AnimationBone> parts;

    BodyModel(String... filePath) {
        Path path = Directory.skeletons.getPath(filePath);
        String binaryName = path.getFileName().toString();
        File file = new File(binaryName.replace(".dae", BINARY_SKELETON_EXT));

        if (file.exists()) {
            // load skelly from binary
            try (InputStream fileStream = new FileInputStream(file)) {
                DataInput in = new DataInputStream(fileStream);
                root = new AnimationBone(in);

            } catch (IOException e) {
//                Logger.ERROR.print(e);
                throw new RuntimeException(e);
            }

        } else {
            // extract from collada file
            ColladaLoader loader = new ColladaLoader(path);
            JointData skeletonData = loader.loadColladaSkeleton();
            root = new AnimationBone(skeletonData);
            // also store binary
            try (OutputStream fileStream = new FileOutputStream(file)) {
                DataOutput out = new DataOutputStream(fileStream);
                root.writeToFile(out);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        parts = root.stream().collect(Collectors.toUnmodifiableList());
    }

    List<AnimationBone> getElements() {
        return parts;
    }

    public int size() {
        return root.nrOfChildren() + 1;
    }}
