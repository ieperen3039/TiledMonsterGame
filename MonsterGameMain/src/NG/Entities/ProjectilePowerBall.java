package NG.Entities;

import NG.DataStructures.Generic.Color4f;
import NG.Engine.Game;
import NG.GameEvent.Event;
import NG.GameEvent.EventLoop;
import NG.Particles.GameParticles;
import NG.Particles.ParticleCloud;
import NG.Particles.Particles;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Rendering.Shapes.GenericShapes;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 2-4-2019.
 */
public class ProjectilePowerBall extends Projectile {
    private static final GenericShapes mesh = GenericShapes.ICOSAHEDRON;
    private final float spawnTime;
    private final Vector3fc startPosition;
    private final Vector3fc endPosition;
    private final float duration;

    public ProjectilePowerBall(
            Game game, float spawnTime, float scaling, float speed, Vector3fc startPosition, Vector3fc endPosition
    ) {
        super(game, scaling);
        this.spawnTime = spawnTime;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.duration = startPosition.distance(endPosition) / speed;

        Event.Anonymous explodeEvent = new Event.Anonymous(spawnTime + duration, () -> {
            ProjectilePowerBall.this.dispose();
            ParticleCloud explosion = Particles.explosion(endPosition, Vectors.O, Color4f.RED, Color4f.ORANGE, 1000, 2, 5);
            game.get(GameParticles.class).add(explosion);
        });
        game.get(EventLoop.class).addEvent(explodeEvent);
    }

    @Override
    public Vector3f getPositionAt(float currentTime) {
        float fraction = (currentTime - spawnTime) / duration;
        assert fraction > 0 && fraction < 1 : "fraction = " + fraction;
        return new Vector3f(startPosition).lerp(endPosition, fraction);
    }

    @Override
    protected void drawProjectile(SGL gl, float renderTime) {
        ShaderProgram shader = gl.getShader();

        if (shader instanceof MaterialShader) {
            ((MaterialShader) shader).setMaterial(Material.ROUGH, Color4f.RED);
        }

        gl.render(mesh, this);
    }

    @Override
    public float getSpawnTime() {
        return spawnTime;
    }
}
