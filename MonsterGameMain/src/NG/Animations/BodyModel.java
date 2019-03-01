package NG.Animations;

import NG.Tools.Directory;

import java.nio.file.Path;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public enum BodyModel {
    ANTHRO(
            "anthro.dae"
    ),

    ;

    public final AnimationBone root;

    BodyModel(String... filePath) {
        Path path = Directory.skeletons.getPath(filePath);
        root = null;
    }
}
