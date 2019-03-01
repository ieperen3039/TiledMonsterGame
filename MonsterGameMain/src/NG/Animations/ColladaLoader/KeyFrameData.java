package NG.Animations.ColladaLoader;

import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class KeyFrameData {

    public final float time;
    public final Map<String, Matrix4f> jointTransforms = new HashMap<>();

    public KeyFrameData(float time) {
        this.time = time;
    }

}
