package NG.Animations;

import NG.Animations.ColladaLoader.ColladaLoader;
import NG.Animations.ColladaLoader.JointData;
import NG.Tools.Directory;

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

    public final AnimationBone root;
    private final List<AnimationBone> parts;

    BodyModel(String... filePath) {
        Path path = Directory.skeletons.getPath(filePath);
        ColladaLoader loader = new ColladaLoader(path);
        JointData skeletonData = loader.loadColladaSkeleton();
        root = new AnimationBone(skeletonData);

        parts = root.stream().collect(Collectors.toUnmodifiableList());
    }

    List<AnimationBone> getElements() {
        return parts;
    }

    public int size() {
        return root.nrOfChildren() + 1;
    }}
