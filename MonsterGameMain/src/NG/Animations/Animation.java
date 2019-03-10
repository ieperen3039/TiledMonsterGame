package NG.Animations;

import NG.Engine.GameTimer;
import NG.Entities.Entity;
import NG.Entities.RobotMonster;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import org.joml.AABBf;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Map;
import java.util.Set;

/**
 * An animation that maps {@link AnimationBone} instances to rotations over time.
 * As animation bones of different skeletons do not overlap, one animation can be used for all different body models.
 * An animation is stateless.
 * @author Geert van Ieperen created on 1-3-2019.
 */
public interface Animation extends Storable {
    /**
     * @param bone           a bone considered by this animation
     * @param timeSinceStart the time since the start of this animation
     * @return the transformation of the joint that controls this specific bone, with on each axis the angles that is
     * rotated around that axis. If {@code timeSinceStart} is more than {@link #duration()}, the result is undefined.
     * @throws IllegalArgumentException if the given bone is not part of this animation
     */
    Matrix4fc transformationOf(AnimationBone bone, float timeSinceStart);

    /**
     * query the duration of this animation.
     * @return the last timestamp for which this animation has values.
     */
    float duration();

    Set<AnimationBone> getDomain();

    class Demonstrator implements Entity {
        private static final Map<AnimationBone, BoneElement> ROBOT_MESH_MAP = RobotMonster.getRobotMeshMap();
        private final GameTimer timer;

        private Vector3f position = new Vector3f();
        private Animation ani;
        private BodyModel model;
        private float startTime = 0;
        private boolean isDisposed;

        public Demonstrator(Animation ani, BodyModel model, GameTimer timer) {
            isDisposed = false;
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
        public Vector3f getPosition(float currentTime) {
            return position;
        }

        @Override
        public void onClick(int button) {
        }

        @Override
        public void dispose() {
            isDisposed = true;
        }

        @Override
        public boolean isDisposed() {
            return isDisposed;
        }

        @Override
        public AABBf hitbox() {
            return null;
        }

        public void setAnimation(Animation ani) {
            this.ani = ani;
        }

        public void setModel(BodyModel model) {
            this.model = model;
            this.startTime = timer.getRendertime();
        }
    }
}
