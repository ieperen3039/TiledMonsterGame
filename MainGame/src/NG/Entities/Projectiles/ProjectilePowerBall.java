package NG.Entities.Projectiles;

import NG.Actions.ActionLinearMove;
import NG.Actions.Attacks.ActionFireProjectile;
import NG.Actions.Attacks.DamageType;
import NG.CollisionDetection.BoundingBox;
import NG.Core.Game;
import NG.DataStructures.Generic.Color4f;
import NG.Entities.Entity;
import NG.Entities.MonsterEntity;
import NG.GameMap.GameMap;
import NG.InputHandling.MouseTools.CommandProvider;
import NG.Particles.GameParticles;
import NG.Particles.Particles;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Rendering.Shapes.GenericShapes;
import NG.Settings.Settings;
import NG.Tools.Vectors;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 2-4-2019.
 */
public class ProjectilePowerBall extends Projectile {
    private static final GenericShapes mesh = GenericShapes.ICOSAHEDRON;
    private static final int EXPLOSION_POWER = 10;
    private static final float HITBOX_SCALAR = 0.4f;
    private static final int BASE_DAMAGE = 25;
    private final BoundingBox boundingBox;
    private final DamageType damageType;
    private float size;

    public ProjectilePowerBall(
            Game game, Entity source, Vector3f spawnPosition, Vector2ic endCoordinate, float spawnTime,
            Supplier<Boolean> validator, float speed, float size
    ) {
        this(
                game, source, spawnPosition,
                game.get(GameMap.class).getPosition(endCoordinate).add(0, 0, size * HITBOX_SCALAR),
                spawnTime, size, speed, validator
        );
    }

    private ProjectilePowerBall(
            Game game, Entity source, Vector3fc spawnPosition, Vector3fc target, float spawnTime, float size,
            float speed, Supplier<Boolean> validator
    ) {
        super(
                game, source, spawnTime, spawnTime + (spawnPosition.distance(target) / speed),
                new ActionLinearMove(spawnPosition, target, speed), validator
        );

        this.size = size;
        this.damageType = DamageType.PHYSICAL;

        float boxSize = size * HITBOX_SCALAR;
        this.boundingBox = new BoundingBox(-boxSize, -boxSize, -boxSize, boxSize, boxSize, boxSize);
    }

    @Override
    public BoundingBox getHitbox(float gameTime) {
        return boundingBox.getMoved(getPositionAt(gameTime));
    }

    @Override
    public void collideWith(Entity other, float collisionTime) {
        if (other instanceof MonsterEntity) {
            MonsterEntity monster = (MonsterEntity) other;

            monster.getController().applyDamage(damageType, BASE_DAMAGE, collisionTime);
        }

        explode(collisionTime);
    }

    @Override
    public void collideWith(GameMap map, float collisionTime) {
        explode(collisionTime);
    }

    private void explode(float collisionTime) {
        game.get(GameParticles.class).add(Particles.explosion(
                getPositionAt(collisionTime), Vectors.O,
                new Color4f(1, 1, 0),
                new Color4f(0.5f, 0.8f, 0),
                (int) (game.get(Settings.class).PARTICLE_MODIFIER * Particles.EXPLOSION_BASE_DENSITY),
                Particles.FIRE_LINGER_TIME, EXPLOSION_POWER,
                collisionTime
        ));

        despawnTime = collisionTime;
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
    public float getIntersection(Vector3fc origin, Vector3fc direction, float gameTime) {
        return 0;
    }

    public static CommandProvider fireCommand() {
        return CommandProvider.actionCommand("Powerball",
                (g, e, s, t, time) -> new ActionFireProjectile(g, e, t, time, 1)
        );
    }
}
