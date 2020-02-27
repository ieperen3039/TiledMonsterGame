package NG.Animations;

import NG.Tools.Directory;
import NG.Tools.SerializationTools;
import NG.Tools.Vectors;
import org.joml.AxisAngle4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * @author Geert van Ieperen created on 10-3-2019.
 */
public class KeyFrameAnimationTest {
    private KeyFrameAnimation ani;
    private SkeletonBone bone;

    @Before
    public void setUp() throws Exception {
        File file = Directory.animations.getFile("walkCycleAnthro.anibi");
        ani = (KeyFrameAnimation) SerializationTools.readFromFile(file);
        bone = ani.model.getBone("ANTHRO_UpperLeg.R");
    }

    @Test
    public void dumpBone() {
        System.out.println(ani.framesOf(bone));
        ani.transformationOf(bone, 0.5f);
    }

    @Test
    public void printCycle() {
        float deltaTime = 0.1f;
        // rotation should be close to a sine wave

        for (float t = 0; t < ani.duration(); t += deltaTime) {
            Matrix4fc mat = ani.transformationOf(bone, t);
            AxisAngle4f rotation = mat.getRotation(new AxisAngle4f());
            Vector3fc translation = mat.getTranslation(new Vector3f());
            System.out.printf("pos = %s, rot = %s\n", Vectors.toString(translation), rotation);
        }
    }
}
