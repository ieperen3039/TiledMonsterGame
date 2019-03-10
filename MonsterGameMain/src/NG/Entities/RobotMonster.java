package NG.Entities;

import NG.Animations.AnimationBone;
import NG.Animations.BodyModel;
import NG.Animations.BoneElement;
import NG.Animations.RobotMeshes;
import NG.Engine.Game;
import NG.MonsterSoul.MonsterSoul;
import NG.Tools.Logger;
import org.joml.AABBf;
import org.joml.Vector2i;
import org.joml.Vector3fc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 6-3-2019.
 */
public class RobotMonster extends MonsterSoul {
    // for every robot the exact same proportions
    private static final Map<AnimationBone, BoneElement> boneMap = getRobotMeshMap();

    public RobotMonster(File description) throws IOException {
        super(description);
    }

    public static Map<AnimationBone, BoneElement> getRobotMeshMap() {
        List<AnimationBone> remainders = new ArrayList<>();
        Map<AnimationBone, BoneElement> map = new AnimationBone.BodyBuilder(BodyModel.ANTHRO)
                .add("Anthro_Spine", RobotMeshes.ROBOT_TORSO, 0.1f)
                .add("Anthro_UpperArm_L", RobotMeshes.ROBOT_UPPER_ARM, 0.1f)
                .add("Anthro_LowerArm_L", RobotMeshes.ROBOT_LOWER_ARM, 0.1f)
                .add("Anthro_UpperLeg_L", RobotMeshes.ROBOT_UPPER_ARM, 0.1f)
                .add("Anthro_LowerLeg_L", RobotMeshes.ROBOT_LOWER_ARM, 0.1f)
                .add("Anthro_UpperArm_R", RobotMeshes.ROBOT_UPPER_ARM, 0.1f)
                .add("Anthro_LowerArm_R", RobotMeshes.ROBOT_LOWER_ARM, 0.1f)
                .add("Anthro_UpperLeg_R", RobotMeshes.ROBOT_UPPER_ARM, 0.1f)
                .add("Anthro_LowerLeg_R", RobotMeshes.ROBOT_LOWER_ARM, 0.1f)
                .add("Anthro_Head", RobotMeshes.ROBOT_HEAD, 0.1f)
//                .add("Anthro_Ear_L", RobotMeshes.ROBOT_EAR, 0.1f)
//                .add("Anthro_Ear_R", RobotMeshes.ROBOT_EAR, 0.1f)
                .forEachRemaining(remainders::add);

        Logger.WARN.print("Unassigned bones: " + remainders);

        return map;
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
        protected void lookAt(Vector3fc position) {

        }

        @Override
        public AABBf hitbox() {
            return null;
        }
    }
}
