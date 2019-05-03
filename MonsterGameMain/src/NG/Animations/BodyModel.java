package NG.Animations;

import NG.Actions.EntityAction;
import NG.Animations.ColladaLoader.Converter;
import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import NG.Tools.Directory;

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
    CUBE(new AnimationBone("cube_root", 0, 0, 0, 0, 0, 0, 0, 0)),
    ANTHRO("anthro.skelbi"),
    TEST_ANTHRO(getAnthro());

    private AnimationBone root;
    private final Map<String, AnimationBone> parts;

    BodyModel(String... filePath) {
        Path path = Directory.skeletons.getPath(filePath);
        File file = path.toFile();

        if (!file.exists()) {
            Converter.rewriteSkeleton(file.getName(), toString());
        }

        // load skelly from binary
        root = Storable.readFromFileRequired(file, AnimationBone.class);
        parts = getParts(root);
    }

    BodyModel(AnimationBone root) {
        this.root = root;
        parts = getParts(root);
    }

    private HashMap<String, AnimationBone> getParts(AnimationBone root) {
        HashMap<String, AnimationBone> p = new HashMap<>();
        root.forEach(b -> p.put(b.getName(), b));
        return p;
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
        AnimationTransfer transfer = AnimationTransfer.get(previous, action);
        UniversalAnimation animation = (transfer == null) ? action.getAnimation() : transfer;
        float animationTime = (timeSinceStart / action.duration()) * animation.duration();

        draw(gl, entity, map, animation, animationTime);
    }

    public void draw(
            SGL gl, Entity entity, Map<AnimationBone, BoneElement> map, UniversalAnimation ani, float aniTime
    ) {
        root.draw(gl, entity, map, ani, aniTime);
    }

    /**
     * replaces the body model of this model with the given model
     * @param root the root bone of the new model
     */
    public void replace(AnimationBone root) {
        this.root = root;
        parts.clear();
        root.forEach(b -> parts.put(b.getName(), b));
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

    public static AnimationBone getAnthro() {
        return new AnimationBone(
                "ANTHRO_Spine",
                0, 0, 0,
                0, 0, 0, 0, 0.1f,
                new AnimationBone(
                        "ANTHRO_Head",
                        -0.5f, 0f, 6.5f,
                        0, 0, 0, 0, 0,
                        new AnimationBone(
                                "ANTHRO_Ear.L",
                                0.0f, 1.0f, 3.2f,
                                0, 0, 0, 0, 0
                        ),
                        new AnimationBone(
                                "ANTHRO_Ear.R",
                                0.0f, -1.0f, 3.2f,
                                0, 0, 0, 0, 0
                        )
                ),
                new AnimationBone(
                        "ANTHRO_UpperArm.L",
                        -1.0f, 5.0f, 3.5f,
                        1, 0, 1, 90, 0,
                        new AnimationBone(
                                "ANTHRO_LowerArm.L",
                                0, 0, 8.0f,
                                0, 0, 0, 0, 0
                        )
                ),
                new AnimationBone(
                        "ANTHRO_UpperArm.R",
                        -1.0f, -5.0f, 3.5f,
                        1, 0, -1, 90, 0,
                        new AnimationBone(
                                "ANTHRO_LowerArm.R",
                                0, 0, 8.0f,
                                0, 0, 0, 0, 0
                        )
                ),
                new AnimationBone(
                        "ANTHRO_UpperLeg.L",
                        0, 2.0f, -7.0f,
                        1, 0, 0, 180, 0,
                        new AnimationBone(
                                "ANTHRO_LowerLeg.L",
                                0, 0, 8.0f,
                                0, 0, 0, 0, 0,
                                new AnimationBone(
                                        "ANTHRO_Foot.L",
                                        0, 0, 8.0f,
                                        0, 0, 0, 0, 0
                                )
                        )
                ),
                new AnimationBone(
                        "ANTHRO_UpperLeg.R",
                        0, -2.0f, -7.0f,
                        1, 0, 0, // this makes no difference to anthro_upper_leg_left, but for the idea...
                        -180, 0,
                        new AnimationBone(
                                "ANTHRO_LowerLeg.R",
                                0, 0, 8.0f,
                                0, 0, 0,
                                0, 0,
                                new AnimationBone(
                                        "ANTHRO_Foot.R",
                                        0, 0, 8.0f,
                                        0, 0, 0, 0, 0
                                )
                        )
                )
        );
    }
}
