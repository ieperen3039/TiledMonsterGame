package NG.Entities;

import NG.DataStructures.Generic.Color4f;
import NG.Engine.Game;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Rendering.Shapes.GenericShapes;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 2-4-2019.
 */
public class ProjectilePowerBall extends Projectile {
    private static final GenericShapes mesh = GenericShapes.ICOSAHEDRON;
    private final Vector3fc endPosition;
    private final float speed;

    private Vector3fc startPosition;
    private float duration;

    public ProjectilePowerBall(Game game, Vector3fc endPosition, float speed, float size) {
        super(game, size);
        this.endPosition = endPosition;
        this.speed = speed;
    }

    @Override
    protected void setSpawnPosition(Vector3fc spawnPosition) {
        this.startPosition = spawnPosition;
        this.duration = startPosition.distance(endPosition) / speed;
    }

    @Override
    public Vector3f getPositionAt(float timeSinceStart) {
        assert timeSinceStart > 0 : "timeSinceStart = " + timeSinceStart;

        float fraction = timeSinceStart / duration;
        return new Vector3f(startPosition).lerp(endPosition, fraction);
    }

    @Override
    public void collideWith(Entity other, float collisionTime) {

    }

    @Override
    protected void drawProjectile(SGL gl, float renderTime) {
        ShaderProgram shader = gl.getShader();

        if (shader instanceof MaterialShader) {
            ((MaterialShader) shader).setMaterial(Material.ROUGH, Color4f.RED);
        }

        gl.render(mesh, this);
    }
}
