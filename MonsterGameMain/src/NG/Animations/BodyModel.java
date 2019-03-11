package NG.Animations;

import NG.Animations.ColladaLoader.Converter;
import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import NG.Tools.Directory;
import NG.Tools.Vectors;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public enum BodyModel {
    CUBE("cube.skelbi"),
    ANTHRO("anthro.skelbi"),

    ;

    private final AnimationBone root;
    private final Map<String, AnimationBone> parts;

    BodyModel(String... filePath) {
        Path path = Directory.skeletons.getPath(filePath);
        File file = path.toFile();

        if (!file.exists()) {
            Converter.rewriteSkeleton(file.getName());
        }

        // load skelly from binary
        root = Storable.readFromFileRequired(file, AnimationBone.class);

        parts = new HashMap<>();
        root.stream().forEach(b -> parts.put(b.getName(), b));
    }

    Collection<AnimationBone> getElements() {
        return Collections.unmodifiableCollection(parts.values());
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

    public AnimationBone getBone(String boneName) {
        return parts.get(boneName);
    }

    /**
     * @param rootBone the name of the root bone of this body
     * @return the body model with the exact same bones as the given root bone
     */
    public static BodyModel getByRoot(AnimationBone rootBone) {
        for (BodyModel model : values()) {
            if (model.root.equals(rootBone)) {
                return model;
            }
        }

        return null;
    }
}
