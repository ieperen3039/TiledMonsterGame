package NG.Entities;

import NG.Animations.AnimationBone;
import NG.Animations.BodyModel;
import NG.Animations.BoneElement;
import NG.Animations.RobotMeshes;
import NG.CollisionDetection.BoundingBox;
import NG.Engine.Game;
import NG.Living.MonsterSoul;
import NG.Living.SoulDescription;
import org.joml.Vector2i;
import org.joml.Vector3fc;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 6-3-2019.
 */
public class RobotMonster extends MonsterSoul {
    // for every robot the exact same proportions
    private static final Map<AnimationBone, BoneElement> boneMap = getRobotMeshMap();

    public RobotMonster(File description) throws IOException {
        super(new SoulDescription(description));
    }

    @Override
    protected MonsterEntity getNewEntity(Game game, Vector2i coordinate, Vector3fc direction) {
        return new Entity(game, coordinate);
    }

    private class Entity extends MonsterEntity {
        public Entity(Game game, Vector2i initialPosition) {
            super(game, initialPosition, RobotMonster.this);
        }

        @Override
        protected BodyModel bodyModel() {
            return BodyModel.ANTHRO;
        }

        @Override
        protected Map<AnimationBone, BoneElement> getBoneMapping() {
            return boneMap;
        }

        @Override
        public BoundingBox hitbox() {
            return new BoundingBox(-1, -1, 0, 1, 1, 2);
        }
    }

    public static Map<AnimationBone, BoneElement> getRobotMeshMap() {
        return new AnimationBone.BodyBuilder(BodyModel.ANTHRO)
                .add("ANTHRO_Spine", RobotMeshes.ROBOT_TORSO)
                .add("ANTHRO_UpperArm.L", RobotMeshes.ROBOT_UPPER_ARM)
                .add("ANTHRO_LowerArm.L", RobotMeshes.ROBOT_LOWER_ARM)
                .add("ANTHRO_UpperLeg.L", RobotMeshes.ROBOT_UPPER_ARM)
                .add("ANTHRO_LowerLeg.L", RobotMeshes.ROBOT_LOWER_ARM)
                .add("ANTHRO_UpperArm.R", RobotMeshes.ROBOT_UPPER_ARM)
                .add("ANTHRO_LowerArm.R", RobotMeshes.ROBOT_LOWER_ARM)
                .add("ANTHRO_UpperLeg.R", RobotMeshes.ROBOT_UPPER_ARM)
                .add("ANTHRO_LowerLeg.R", RobotMeshes.ROBOT_LOWER_ARM)
                .add("ANTHRO_Foot.L", RobotMeshes.ROBOT_FOOT)
                .add("ANTHRO_Foot.R", RobotMeshes.ROBOT_FOOT)
                .add("ANTHRO_Head", RobotMeshes.ROBOT_HEAD)
                .add("ANTHRO_Ear.L", RobotMeshes.ROBOT_EAR)
                .add("ANTHRO_Ear.R", RobotMeshes.ROBOT_EAR)
                .add("ANTHRO_TopFinger.L", RobotMeshes.ROBOT_CLAW)
                .add("ANTHRO_FrontFinger.L", RobotMeshes.ROBOT_CLAW)
                .add("ANTHRO_BackFinger.L", RobotMeshes.ROBOT_CLAW)
                .add("ANTHRO_TopFinger.R", RobotMeshes.ROBOT_CLAW)
                .add("ANTHRO_FrontFinger.R", RobotMeshes.ROBOT_CLAW)
                .add("ANTHRO_BackFinger.R", RobotMeshes.ROBOT_CLAW)
                .get();
    }
}
