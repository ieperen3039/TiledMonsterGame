package NG.Entities;

import NG.Animations.BodyModel;
import NG.Animations.BoneElement;
import NG.Animations.SkeletonBone;
import NG.CollisionDetection.BoundingBox;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Generic.Pair;
import NG.Living.MonsterSoul;
import NG.Living.SoulDescription;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shapes.GenericShapes;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 6-3-2019.
 */
public class CubeMonster extends MonsterSoul {
    private static final float HALF_SIZE = 0.4f;

    private final Map<SkeletonBone, BoneElement> boneMap;
    private final BoundingBox hitbox = new BoundingBox(-HALF_SIZE, -HALF_SIZE, 0, HALF_SIZE, HALF_SIZE, HALF_SIZE * 2);

    public CubeMonster(File description) throws IOException {
        super(new SoulDescription(description));
        boneMap = Collections.singletonMap(BodyModel.CUBE.getBone("cube_root"),
                new CubeElement(HALF_SIZE)
        );
    }

    public CubeMonster(DataInputStream in) throws IOException, ClassNotFoundException {
        super(in);
        boneMap = null;
    }

    @Override
    protected MonsterEntity getNewEntity(Game game, Vector2i coordinate, Vector3fc direction) {
        return new Entity(game, coordinate, direction, this);
    }

    /**
     * @author Geert van Ieperen created on 4-2-2019.
     */
    public class Entity extends MonsterEntity {
        /** the direction this entity is facing relative to the world */
        private Pair<Vector3fc, Float> currentFace;
        /** the direction this entity is rotating to relative to the world */
        private Pair<Vector3fc, Float> targetFace;

        /**
         * a default cube that can move and interact like any other monster
         * @param game
         * @param initialCoordinate
         * @param faceDirection
         * @param controller
         */
        public Entity(
                Game game, Vector2i initialCoordinate, Vector3fc faceDirection, CubeMonster controller
        ) {
            super(game, initialCoordinate, controller, BodyModel.CUBE, boneMap);

            float gametime = game.get(GameTimer.class).getGametime();
            Vector3f eyeDir = new Vector3f(faceDirection);
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
        public String toString() {
            return "CubeMonster";
        }

        @Override
        public BoundingBox getHitbox() {
            return hitbox;
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

    private static class CubeElement extends BoneElement {
        private final float size;

        CubeElement(float size) {
            super(GenericShapes.CUBE, Material.ROUGH);
            this.size = size;
        }

        @Override
        public void draw(SGL gl, NG.Entities.Entity entity) {
            gl.pushMatrix();
            {
                gl.scale(size);
                super.draw(gl, entity);
            }
            gl.popMatrix();
        }
    }
}
