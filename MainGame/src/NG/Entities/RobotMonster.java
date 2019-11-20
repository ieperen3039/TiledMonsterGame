package NG.Entities;

import NG.Animations.BodyModel;
import NG.Animations.BoneElement;
import NG.Animations.RobotMeshes;
import NG.Animations.SkeletonBone;
import NG.CollisionDetection.BoundingBox;
import NG.Core.Game;
import NG.Living.MonsterSoul;
import NG.Living.SoulDescription;
import org.joml.Vector2i;
import org.joml.Vector3fc;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Not how you are supposed to build monsters
 * @author Geert van Ieperen created on 6-3-2019.
 */
public class RobotMonster extends MonsterSoul {
    // for every robot the exact same proportions
    private static final Map<SkeletonBone, BoneElement> boneMap = getRobotMeshMap();

    public RobotMonster(File description) throws IOException {
        super(new SoulDescription(description));
    }

    @Override
    protected MonsterEntity getNewEntity(Game game, Vector2i coordinate, Vector3fc direction) {
        return new Entity(game, coordinate);
    }

    private class Entity extends MonsterEntity {
        public Entity(Game game, Vector2i initialPosition) {
            super(game, initialPosition, RobotMonster.this, BodyModel.ANTHRO, boneMap);
        }

        @Override
        public BoundingBox getHitbox() {
            return new BoundingBox(-1, -1, 0, 1, 1, 2);
        }
    }

    public static Map<SkeletonBone, BoneElement> getRobotMeshMap() {
        return new SkeletonBone.BodyBuilder(BodyModel.ANTHRO)
                .add("Spine", RobotMeshes.ROBOT_TORSO)
                .add("UpperArm.L", RobotMeshes.ROBOT_UPPER_ARM)
                .add("LowerArm.L", RobotMeshes.ROBOT_LOWER_ARM)
                .add("UpperLeg.L", RobotMeshes.ROBOT_UPPER_ARM)
                .add("LowerLeg.L", RobotMeshes.ROBOT_LOWER_ARM)
                .add("UpperArm.R", RobotMeshes.ROBOT_UPPER_ARM)
                .add("LowerArm.R", RobotMeshes.ROBOT_LOWER_ARM)
                .add("UpperLeg.R", RobotMeshes.ROBOT_UPPER_ARM)
                .add("LowerLeg.R", RobotMeshes.ROBOT_LOWER_ARM)
                .add("Foot.L", RobotMeshes.ROBOT_FOOT)
                .add("Foot.R", RobotMeshes.ROBOT_FOOT)
                .add("Head", RobotMeshes.ROBOT_HEAD)
                .add("Ear.L", RobotMeshes.ROBOT_EAR)
                .add("Ear.R", RobotMeshes.ROBOT_EAR)
                .add("TopFinger.L", RobotMeshes.ROBOT_CLAW)
                .add("FrontFinger.L", RobotMeshes.ROBOT_CLAW)
                .add("BackFinger.L", RobotMeshes.ROBOT_CLAW)
                .add("TopFinger.R", RobotMeshes.ROBOT_CLAW)
                .add("FrontFinger.R", RobotMeshes.ROBOT_CLAW)
                .add("BackFinger.R", RobotMeshes.ROBOT_CLAW)
                .get();
    }
}
