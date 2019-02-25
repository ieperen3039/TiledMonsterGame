package NG.Entities;

import NG.DataStructures.Generic.Color4f;
import NG.DataStructures.Generic.Pair;
import NG.Engine.Game;
import NG.Entities.Actions.EntityAction;
import NG.MonsterSoul.MonsterSoul;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Rendering.Shapes.GenericShapes;
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

    /** the direction this entity is facing relative to the world */
    private Pair<Vector3fc, Float> currentFace;
    /** the direction this entity is rotating to relative to the world */
    private Pair<Vector3fc, Float> targetFace;

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
        super(game, initialCoordinate, decisionProcess);

        float gametime = game.timer().getGametime();
        Vector3f eyeDir = new Vector3f(faceDirection);

        float sq2 = (float) Math.sqrt(2);
        this.hitbox = new AABBf(-sq2, -sq2, -sq2, sq2, sq2, sq2);
        this.currentFace = new Pair<>(eyeDir, gametime);
        this.targetFace = currentFace;
    }

    @Override
    protected void drawDetail(SGL gl, EntityAction action, float actionProgress) {
        ShaderProgram shader = gl.getShader();
        float gametime = game.timer().getGametime();

        if (shader instanceof MaterialShader) {
            ((MaterialShader) shader).setMaterial(Material.SILVER, Color4f.BLUE);
        }

        Vector3f face = getFaceRotation(gametime);
        gl.rotate(Vectors.getPitchYawRotation(face));
        gl.scale(SIZE / 2);
        gl.translate(0, 0, 1);
        gl.render(GenericShapes.CUBE, this);
        gl.translate(1, 0, 1);
        gl.scale(0.5f);
        gl.render(GenericShapes.CUBE, this);
    }

    /**
     * Returns the rotation of this entity on the given moment. This is influenced by {@link
     * #setTargetRotation(Vector3fc)}
     * @param currentTime the time of measurement.
     * @return the rotation at the given moment
     */
    private Vector3f getFaceRotation(float currentTime) {
        if (currentTime > targetFace.right) {
            return new Vector3f(targetFace.left);
        }

        float base = currentFace.right;
        float range = targetFace.right - base;
        float fraction = (currentTime - base) / range;

        Vector3fc current = currentFace.left;
        Vector3fc target = targetFace.left;

        return new Vector3f(current).lerp(target, fraction);
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
        return "CubeMonster";
    }

    @Override
    protected void setTargetRotation(Vector3fc direction) {
        float gametime = game.timer().getGametime();
        Vector3fc curDir = getFaceRotation(gametime);
        float angle = curDir.angle(direction);

        currentFace = new Pair<>(getFaceRotation(gametime), gametime);
        float rotationSpeedRS = 0.1f;
        targetFace = new Pair<>(direction, gametime + angle / rotationSpeedRS);
    }
}
