package NG.Animations;

import NG.Storable;
import NG.Tools.Directory;
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
    private AnimationBone bone;

    @Before
    public void setUp() throws Exception {
        File file = Directory.animations.getFile("walk1.anibi");
        ani = Storable.readFromFile(file, KeyFrameAnimation.class);
        bone = ani.model.getBone("Anthro_UpperLeg_R");
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

        for (int i = 0; i < 20; i++) {
            Matrix4fc mat = ani.transformationOf(bone, deltaTime * i);
            AxisAngle4f rotation = mat.getRotation(new AxisAngle4f());
            Vector3fc translation = mat.getTranslation(new Vector3f());
            System.out.printf("pos = %s, rot = %s\n", translation, rotation);
        }
    }
}
