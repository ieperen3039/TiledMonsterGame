package NG.Animations;

import NG.Actions.ActionIdle;
import NG.Actions.EntityAction;
import NG.CollisionDetection.BoundingBox;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Generic.Pair;
import NG.Entities.Dummy;
import NG.Entities.MovingEntity;
import NG.Rendering.MatrixStack.SGL;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * An animation that maps {@link SkeletonBone} instances to rotations over time. As animation bones of different
 * skeletons do not overlap, one animation can be used for all different body models. An animation is stateless.
 * @author Geert van Ieperen created on 1-3-2019.
 */
public interface PartialAnimation extends Serializable {
    /**
     * @param bone           a bone considered by this animation
     * @param timeSinceStart the time since the start of this animation
     * @return the transformation of the joint that controls the given bone. If {@code timeSinceStart} is more than
     * {@link #duration()}, the result is undefined.
     * @throws IllegalArgumentException if the given bone is not part of this animation
     * @see #getDomain()
     */
    Matrix4fc transformationOf(SkeletonBone bone, float timeSinceStart);

    /**
     * query the duration of this animation.
     * @return the last timestamp for which this animation has values.
     */
    float duration();

    /**
     * @return an immutable set of bones which are accepted by {@link #transformationOf(SkeletonBone, float)}
     */
    Set<SkeletonBone> getDomain();

    /** an entity that plays a given animation using a robot mesh */
    class Demonstrator extends Dummy implements MovingEntity {
        private final GameTimer timer;
        private Vector3f position = new Vector3f();
        private UniversalAnimation ani;
        private BodyModel model;
        private float startTime = 0;

        private static final Map<SkeletonBone, BoneElement> ROBOT_MESH_MAP = new SkeletonBone.BodyBuilder(BodyModel.ANTHRO)
                .add("Spine", RobotMeshes.ROBOT_TORSO.meshResource())
                .add("UpperArm.L", RobotMeshes.ROBOT_UPPER_ARM.meshResource())
                .add("LowerArm.L", RobotMeshes.ROBOT_LOWER_ARM.meshResource())
                .add("UpperLeg.L", RobotMeshes.ROBOT_UPPER_ARM.meshResource())
                .add("LowerLeg.L", RobotMeshes.ROBOT_LOWER_ARM.meshResource())
                .add("UpperArm.R", RobotMeshes.ROBOT_UPPER_ARM.meshResource())
                .add("LowerArm.R", RobotMeshes.ROBOT_LOWER_ARM.meshResource())
                .add("UpperLeg.R", RobotMeshes.ROBOT_UPPER_ARM.meshResource())
                .add("LowerLeg.R", RobotMeshes.ROBOT_LOWER_ARM.meshResource())
                .add("Foot.L", RobotMeshes.ROBOT_FOOT.meshResource())
                .add("Foot.R", RobotMeshes.ROBOT_FOOT.meshResource())
                .add("Head", RobotMeshes.ROBOT_HEAD.meshResource())
                .add("Ear.L", RobotMeshes.ROBOT_EAR.meshResource())
                .add("Ear.R", RobotMeshes.ROBOT_EAR.meshResource())
                .add("TopFinger.L", RobotMeshes.ROBOT_CLAW.meshResource())
                .add("FrontFinger.L", RobotMeshes.ROBOT_CLAW.meshResource())
                .add("BackFinger.L", RobotMeshes.ROBOT_CLAW.meshResource())
                .add("TopFinger.R", RobotMeshes.ROBOT_CLAW.meshResource())
                .add("FrontFinger.R", RobotMeshes.ROBOT_CLAW.meshResource())
                .add("BackFinger.R", RobotMeshes.ROBOT_CLAW.meshResource())
                .get();

        public Demonstrator(UniversalAnimation ani, BodyModel model, GameTimer timer) {
            this.timer = timer;

            setModel(model);
            setAnimation(ani);
        }

        @Override
        public void draw(SGL gl) {
            float aniTime = (timer.getRendertime() - startTime) % ani.duration();
            model.draw(gl, this, ROBOT_MESH_MAP, ani, aniTime);
        }

        public void setPosition(Vector3fc position) {
            this.position.set(position);
        }

        @Override
        public Vector3f getPositionAt(float gameTime) {
            return new Vector3f(position);
        }

        public void setAnimation(UniversalAnimation ani) {
            this.ani = ani;
        }

        public void setModel(BodyModel model) {
            this.model = model;
            this.startTime = timer.getRendertime();
        }

        @Override
        public BoundingBox getHitbox(float gameTime) {
            return new BoundingBox(0, 0, 0, 0, 0, 0);
        }

        @Override
        public Pair<EntityAction, Float> getActionAt(float gameTime) {
            return new Pair<>(new ActionIdle(position), 0f);
        }

        @Override
        public void restore(Game game) {
        }
    }
}
