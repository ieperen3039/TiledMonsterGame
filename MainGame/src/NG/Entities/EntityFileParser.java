package NG.Entities;

import NG.Animations.BodyModel;
import NG.Animations.BoneElement;
import NG.Animations.SkeletonBone;
import NG.Living.MonsterSoul;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class EntityFileParser {
    private String name;
    private BodyModel model;
    private Map<SkeletonBone, BoneElement> body = new HashMap<>();

    MonsterSoul readEntity(Path path) throws IOException {
//        JsonNode root = new ObjectMapper().readTree(path.resolve("definition.json").toFile());
//
//        name = root.findValue("name").textValue();
//        model = BodyModel.valueOf(root.findValue("model").textValue());
//
//        JsonNode meshTreeNode = root.findValue("meshTree");
//        Iterator<Map.Entry<String, JsonNode>> fields = meshTreeNode.fields();
//        while (fields.hasNext()) {
//            Map.Entry<String, JsonNode> blockNode = fields.next();
//            String boneName = blockNode.getKey();
//            JsonNode meshNode = blockNode.getValue();
//            SkeletonBone bone = model.getBone(boneName);
//            MeshFile boneMesh = MeshFile.loadFile(Directory.meshes.getPath(meshNode.textValue()));
//
//            body.put(bone, new BoneElement(boneMesh.getMesh(), null));
//        }
        return null;
    }
}
