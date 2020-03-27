package NG.Entities;

import NG.Actions.Attacks.DamageType;
import NG.Animations.BodyModel;
import NG.Animations.BoneElement;
import NG.Animations.SkeletonBone;
import NG.CollisionDetection.BoundingBox;
import NG.Entities.Projectiles.ProjectilePowerBall;
import NG.InputHandling.MouseTools.CommandProvider;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.Shapes.GenericShapes;
import NG.Tools.Directory;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * @author Geert van Ieperen created on 7-4-2019.
 */
public class EntityProperties implements Serializable {
    private static final DamageType[] DAMAGE_TYPES = DamageType.values();

    public final String name;
    public final int hitPoints;
    public final float jumpSpeed;
    public final float walkSpeed;

    public final int deltaHitPoints;
    public final float deltaJumpSpeed;
    public final float deltaWalkSpeed;

    public final BoundingBox hitbox;
    public final BodyModel bodyModel;
    public final Map<SkeletonBone, BoneElement> boneMapping;

    public final Map<DamageType, Float> defences;
    public final List<CommandProvider> moves;

    public EntityProperties() {
        this.name = "Cube Monster";
        this.hitPoints = 256;
        this.deltaHitPoints = 0;
        this.jumpSpeed = 10f;
        this.deltaJumpSpeed = 0;
        this.walkSpeed = 3f;
        this.deltaWalkSpeed = 0;

        this.moves = Collections.singletonList(ProjectilePowerBall.fireCommand());

        this.hitbox = new BoundingBox(-0.5f, -0.5f, 0, 0.5f, 0.5f, 1f);
        this.bodyModel = BodyModel.CUBE;

        this.boneMapping = Collections.singletonMap(BodyModel.CUBE.getBone("cube_root"),
                new BoneElement(GenericShapes.CUBE.meshResource(), Material.ROUGH) {
                    @Override
                    public void draw(SGL gl, Entity entity) {
                        gl.scale(0.5f);
                        gl.translate(0, 0, 1);
                        super.draw(gl, entity);
                        gl.translate(0, 0, -1);
                        gl.scale(2f);
                    }
                }
        );

        this.defences = Collections.singletonMap(DamageType.TRUE, 1f);
    }

    public EntityProperties(JsonNode data, String typeName) throws IOException {
        this.name = typeName;

        this.hitPoints = data.get("hit_points").intValue();
        this.deltaHitPoints = 0;
        this.jumpSpeed = data.get("jump_speed").floatValue();
        this.deltaJumpSpeed = 0;
        this.walkSpeed = data.get("walk_speed").floatValue();
        this.deltaWalkSpeed = 0;

        JsonNode defencesNode = data.get("defences");
        Map<DamageType, Float> defences = new EnumMap<>(DamageType.class);
        for (DamageType type : DAMAGE_TYPES) {
            JsonNode typeNode = defencesNode.get(type.toString().toLowerCase());
            float value = typeNode == null ? 1.0f : typeNode.floatValue();
            defences.put(type, value);
        }
        this.defences = Collections.unmodifiableMap(defences);

        JsonNode movesNode = data.get("moves");
        List<CommandProvider> moves = new ArrayList<>();
        for (JsonNode elt : movesNode) {
            String moveName = elt.textValue();
            /// magic
//            moves.add(moveName);
        }
        this.moves = Collections.unmodifiableList(moves);

        JsonNode hitboxNode = data.get("hitbox");
        assert hitboxNode.isArray();
        this.hitbox = new BoundingBox();
        this.hitbox.minX = hitboxNode.get(0).floatValue();
        this.hitbox.maxX = hitboxNode.get(1).floatValue();
        this.hitbox.minY = hitboxNode.get(2).floatValue();
        this.hitbox.maxY = hitboxNode.get(3).floatValue();
        this.hitbox.minZ = hitboxNode.get(4).floatValue();
        this.hitbox.maxZ = hitboxNode.get(5).floatValue();

        this.bodyModel = BodyModel.valueOf(data.get("body_model").textValue());
        this.boneMapping = new HashMap<>();
        JsonNode boneMappingNode = data.get("bone_mapping");
        Iterator<String> bones = boneMappingNode.fieldNames();
        while (bones.hasNext()) {
            String boneName = bones.next();
            String meshLocation = boneMappingNode.get(boneName).textValue();

            BoneElement boneShape = new BoneElement(Mesh.createResource(Directory.meshes, meshLocation), Material.ROUGH);
            boneMapping.put(bodyModel.getBone(boneName), boneShape);
        }
    }
}
