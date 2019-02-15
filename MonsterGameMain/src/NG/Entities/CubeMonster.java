package NG.Entities;

import NG.DataStructures.Generic.Color4f;
import NG.Engine.Game;
import NG.MonsterSoul.MonsterSoul;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Rendering.Shapes.FileShapes;
import NG.Tools.Vectors;
import org.joml.AABBf;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public class CubeMonster extends MonsterEntity {
    private static final float SIZE = 0.4f;
    private AABBf hitbox;

    /**
     * a default cube that can move and interact like any other monster
     * @param game
     * @param initialCoordinate
     * @param faceDirection
     * @param decisionProcess
     */
    public CubeMonster(
            Game game, Vector2i initialCoordinate, Vector3fc faceDirection, MonsterSoul decisionProcess
    ) {
        super(game, initialCoordinate, faceDirection, decisionProcess);

        float sq2 = (float) Math.sqrt(2);
        hitbox = new AABBf(-sq2, -sq2, -sq2, sq2, sq2, sq2);
    }

    @Override
    protected void drawDetail(SGL gl) {
        ShaderProgram shader = gl.getShader();

        if (shader instanceof MaterialShader) {
            ((MaterialShader) shader).setMaterial(Material.SILVER, Color4f.BLUE);
        }

        gl.scale(SIZE / 2);
        gl.translate(0, 0, 1);
        gl.render(FileShapes.CUBE, this);
        gl.translate(1, 0, 1);
        gl.scale(0.5f);
        gl.render(FileShapes.CUBE, this);
    }

    @Override
    public void lookAt(Vector3fc position) {
        Vector3f dir = new Vector3f(position).sub(getPosition());
        setTargetRotation(dir);
    }

    @Override
    public AABBf hitbox() {
        return hitbox;
    }

    @Override
    public String toString() {
        return "Cube " + Vectors.toString(getPosition());
    }
}
