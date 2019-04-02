package NG.Animations;

import NG.Animations.ColladaLoader.Converter;
import NG.Entities.Entity;
import NG.GameEvent.Actions.EntityAction;
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

    public AnimationBone getBone(String boneName) {
        return parts.get(boneName);
    }

    /**
     * draws the given action as played by the given entity
     * @param gl             the sgl object
     * @param entity         the entity executing this animation, or null if not applicable
     * @param map            a mapping from the bones of the entity to the elements of the body
     * @param timeSinceStart game time since the start of the action
     * @param action         the action being executed by the entity on the given time
     * @param previous       the action previously executed
     */
    public void draw(
            SGL gl, Entity entity, Map<AnimationBone, BoneElement> map, float timeSinceStart, EntityAction action,
            EntityAction previous
    ) {
        AnimationTransfer transfer = AnimationTransfer.get(action, previous);
        UniversalAnimation animation = (transfer == null) ? action.getAnimation() : transfer;
        float animationTime = (timeSinceStart / action.duration()) * animation.duration();

        draw(gl, entity, map, animation, animationTime);
    }

    public void draw(
            SGL gl, Entity entity, Map<AnimationBone, BoneElement> map, UniversalAnimation ani, float aniTime
    ) {
        root.draw(gl, entity, map, ani, aniTime, Vectors.Scaling.UNIFORM);
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
