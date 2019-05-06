package NG.Entities;

import NG.CollisionDetection.BoundingBox;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.Game;
import NG.Particles.GameParticles;
import NG.Particles.Particles;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Rendering.Shapes.GenericShapes;
import NG.Settings.Settings;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 2-4-2019.
 */
public class ProjectilePowerBall extends Projectile {
    private static final GenericShapes mesh = GenericShapes.ICOSAHEDRON;
    private static final int POWER = 10;
    private final float speed;
    private final BoundingBox boundingBox;
    private float size;

    private Vector3fc startPosition;
    private Vector3fc endPosition;
    private float duration;

    public ProjectilePowerBall(Game game, Object source, Vector3fc endPosition, float speed, float size) {
        super(game, source);
        this.speed = speed;
        this.size = size;
        float boxSize = size / 3;
        this.boundingBox = new BoundingBox(-boxSize, -boxSize, -boxSize, boxSize, boxSize, boxSize);
        this.endPosition = new Vector3f(endPosition).add(0, 0, boundingBox.maxZ);
    }

    @Override
    public void update(float gameTime) {
    }

    @Override
    protected void setSpawnPosition(Vector3fc spawnPosition) {
        this.startPosition = spawnPosition;
        this.duration = startPosition.distance(endPosition) / speed;
    }

    @Override
    public Vector3f getPositionAt(float gameTime) {
        float timeSinceStart = gameTime - getSpawnTime();
        if (timeSinceStart < 0) {
            return null;
        }

        float fraction = timeSinceStart / duration;
        return new Vector3f(startPosition).lerp(endPosition, fraction);
    }

    @Override
    public void collideWith(Object other, float collisionTime) {
        super.collideWith(other, collisionTime);

        game.get(GameParticles.class).add(Particles.explosion(
                getPositionAt(collisionTime), Vectors.O,
                new Color4f(1, 0, 0),
                new Color4f(0.5f, 0, 0),
                (int) (game.get(Settings.class).PARTICLE_MODIFIER * Particles.EXPLOSION_BASE_DENSITY),
                Particles.FIRE_LINGER_TIME, POWER
        ));
    }

    @Override
    protected void drawProjectile(SGL gl, float renderTime) {
        gl.scale(size);

        ShaderProgram shader = gl.getShader();

        if (shader instanceof MaterialShader) {
            ((MaterialShader) shader).setMaterial(Material.ROUGH, Color4f.RED);
        }

        gl.render(mesh, this);
    }

    @Override
    public BoundingBox hitbox() {
        return boundingBox;
    }
}
