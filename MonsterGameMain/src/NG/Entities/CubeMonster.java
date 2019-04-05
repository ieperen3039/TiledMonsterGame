package NG.Entities;

import NG.Animations.AnimationBone;
import NG.Animations.BodyModel;
import NG.Animations.BoneElement;
import NG.DataStructures.Generic.Pair;
import NG.Engine.Game;
import NG.Engine.GameTimer;
import NG.MonsterSoul.MonsterSoul;
import NG.Rendering.Shapes.GenericShapes;
import org.joml.AABBf;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 6-3-2019.
 */
public class CubeMonster extends MonsterSoul {
    private static final float SIZE = 0.4f;
    private static final BodyModel BODY_MODEL = BodyModel.CUBE;

    private final Map<AnimationBone, BoneElement> boneMap;

    public CubeMonster(File description) throws IOException {
        super(description);
        boneMap = Collections.singletonMap(
                BODY_MODEL.getBone("cube_root"),
                new BoneElement(GenericShapes.CUBE, new Vector3f(SIZE))
        );
    }

    public CubeMonster(DataInput in) throws IOException, ClassNotFoundException {
        super(in);
        boneMap = null;
    }

    @Override
    protected MonsterEntity getNewEntity(Game game, Vector2i coordinate, Vector3fc direction) {
        return new Entity(game, coordinate, direction, this, boneMap);
    }

    /**
     * @author Geert van Ieperen created on 4-2-2019.
     */
    public static class Entity extends MonsterEntity {
        private AABBf hitbox;

        /** the direction this entity is facing relative to the world */
        private Pair<Vector3fc, Float> currentFace;
        /** the direction this entity is rotating to relative to the world */
        private Pair<Vector3fc, Float> targetFace;
        private Map<AnimationBone, BoneElement> boneMap;

        /**
         * a default cube that can move and interact like any other monster
         * @param game
         * @param initialCoordinate
         * @param faceDirection
         * @param controller
         * @param boneMap
         */
        public Entity(
                Game game, Vector2i initialCoordinate, Vector3fc faceDirection, CubeMonster controller,
                Map<AnimationBone, BoneElement> boneMap
        ) {
            super(game, initialCoordinate, controller);
            this.boneMap = boneMap;

            float gametime = game.get(GameTimer.class).getGametime();
            Vector3f eyeDir = new Vector3f(faceDirection);

            float sq2 = (float) Math.sqrt(2);
            this.hitbox = new AABBf(-sq2, -sq2, -sq2, sq2, sq2, sq2);
            this.currentFace = new Pair<>(eyeDir, gametime);
            this.targetFace = currentFace;
        }

        /**
         * Returns the rotation of this entity on the given moment. This is influenced by {@link
         * #setTargetRotation(Vector3fc)}
         * @param currentTime the time of measurement.
         * @return the rotation at the given moment
         */
        private Vector3f getFaceRotation(float currentTime) {
            if (currentTime > targetFace.right) {
                return new Vector3f(targetFace.left);
            }

            float base = currentFace.right;
            float range = targetFace.right - base;
            float fraction = (currentTime - base) / range;

            Vector3fc current = currentFace.left;
            Vector3fc target = targetFace.left;

            return new Vector3f(current).lerp(target, fraction);
        }

        @Override
        public void lookAt(Vector3fc position) {
            float gametime = game.get(GameTimer.class).getGametime();
            Vector3f dir = getPositionAt(gametime);
            position.sub(dir, dir);
            setTargetRotation(dir);
        }

        @Override
        public String toString() {
            return "CubeMonster";
        }

        @Override
        protected BodyModel bodyModel() {
            return BODY_MODEL;
        }

        @Override
        protected Map<AnimationBone, BoneElement> getBoneMapping() {
            return boneMap;
        }

        protected void setTargetRotation(Vector3fc direction) {
            float gametime = game.get(GameTimer.class).getGametime();
            Vector3fc curDir = getFaceRotation(gametime);
            float angle = curDir.angle(direction);

            currentFace = new Pair<>(getFaceRotation(gametime), gametime);
            float rotationSpeedRS = 0.1f;
            targetFace = new Pair<>(direction, gametime + angle / rotationSpeedRS);
        }
    }
}
