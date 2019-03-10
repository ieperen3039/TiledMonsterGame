package NG.Animations;

import NG.Animations.ColladaLoader.Converter;
import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import NG.Tools.Directory;
import NG.Tools.Vectors;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public enum BodyModel {
    CUBE("cube.skelbi"),
    ANTHRO("anthro.skelbi"),

    ;

    private final AnimationBone root;
    private final List<AnimationBone> parts;

    BodyModel(String... filePath) {
        Path path = Directory.skeletons.getPath(filePath);
        File file = path.toFile();

        if (!file.exists()) {
            Converter.rewriteSkeleton(file.getName());
        }

        // load skelly from binary
        try (InputStream fileStream = new FileInputStream(file)) {
            DataInput in = new DataInputStream(fileStream);
            root = Storable.read(in, AnimationBone.class);

        } catch (IOException | ClassNotFoundException e) {
//                Logger.ERROR.print(e);
            throw new RuntimeException(e);
        }

        parts = root.stream().collect(Collectors.toUnmodifiableList());
    }

    List<AnimationBone> getElements() {
        return parts;
    }

    public int size() {
        return root.nrOfChildren() + 1;
    }

    /** draws this model using the given parameters */
    public void draw(
            SGL gl, Entity entity, Map<AnimationBone, BoneElement> map, Animation animation, float timeSinceStart
    ) {
        root.draw(gl, entity, map, animation, timeSinceStart, Vectors.Scaling.UNIFORM);
    }

}
