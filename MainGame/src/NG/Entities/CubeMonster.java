package NG.Entities;

import NG.Animations.BodyModel;
import NG.Animations.BoneElement;
import NG.Animations.SkeletonBone;
import NG.CollisionDetection.BoundingBox;
import NG.Core.Game;
import NG.Living.MonsterSoul;
import NG.Living.SoulDescription;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shapes.GenericShapes;
import org.joml.Vector2i;
import org.joml.Vector3fc;

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

    @Override
    protected MonsterEntity getNewEntity(Game game, Vector2i coordinate, Vector3fc direction) {
        return new Entity(game, coordinate, this);
    }

    /**
     * Entity of a cube
     */
    public class Entity extends MonsterEntity {

        /**
         * a default cube that can move and interact like any other monster
         */
        public Entity(Game game, Vector2i initialCoordinate, CubeMonster controller) {
            super(game, initialCoordinate, controller, BodyModel.CUBE, boneMap);
        }

        @Override
        public String toString() {
            return "CubeMonster";
        }

        @Override
        public BoundingBox getHitbox() {
            return hitbox;
        }
    }

    /**
     * Bone of a cube
     */
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
