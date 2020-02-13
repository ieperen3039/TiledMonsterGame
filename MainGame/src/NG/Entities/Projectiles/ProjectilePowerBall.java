package NG.Entities.Projectiles;

import NG.Actions.Attacks.DamageType;
import NG.Actions.Commands.Command;
import NG.Actions.Commands.CommandAttack;
import NG.Actions.Commands.CommandSelection;
import NG.CollisionDetection.BoundingBox;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Generic.Color4f;
import NG.Entities.Entity;
import NG.Entities.MonsterEntity;
import NG.GameMap.GameMap;
import NG.Living.Living;
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

/**
 * @author Geert van Ieperen created on 2-4-2019.
 */
public class ProjectilePowerBall extends Projectile {
    private static final GenericShapes mesh = GenericShapes.ICOSAHEDRON;
    private static final int POWER = 10;
    private static final float HITBOX_SCALAR = 0.4f;
    private static final int BASE_DAMAGE = 100;
    private final float speed;
    private final BoundingBox boundingBox;
    private final DamageType damageType;
    private float size;

    private Vector3fc startPosition;
    private Vector3fc endPosition;
    private float duration;

    public ProjectilePowerBall(Game game, Entity source, Vector2ic endCoordinate, float speed, float size) {
        this(
                game, source,
                game.get(GameMap.class)
                        .getPosition(endCoordinate)
                        .add(0, 0, size * HITBOX_SCALAR),
                speed, size);
    }

    public ProjectilePowerBall(Game game, Entity source, Vector3fc endPosition, float speed, float size) {
        super(game, source);
        this.speed = speed;
        this.size = size;
        float boxSize = size * HITBOX_SCALAR;
        this.boundingBox = new BoundingBox(-boxSize, -boxSize, -boxSize, boxSize, boxSize, boxSize);
        this.endPosition = new Vector3f(endPosition);
        damageType = DamageType.PHYSICAL;
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
    public void collideWith(Entity other, float collisionTime) {
        if (other instanceof MonsterEntity) {
            MonsterEntity monster = (MonsterEntity) other;

            monster.controller.applyDamage(damageType, BASE_DAMAGE, collisionTime);
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
                new Color4f(1, 0, 0),
                new Color4f(0.5f, 0.2f, 0),
                (int) (game.get(Settings.class).PARTICLE_MODIFIER * Particles.EXPLOSION_BASE_DENSITY),
                Particles.FIRE_LINGER_TIME, POWER
        ));
        dispose();
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

    public static CommandSelection.CommandProvider fireCommand(Game game) {
        return new CommandSelection.CommandProvider("PowerBall") {
            @Override
            public Command create(Living source, Living receiver, Vector2ic target) {
                MonsterEntity entity = receiver.entity();
                if (entity == null) return null;

                Vector3f targetPosition = game.get(GameMap.class).getPosition(target);
                Projectile prj = new ProjectilePowerBall(game, entity, targetPosition, 5, 0.5f);

                float gametime = game.get(GameTimer.class).getGametime();
                return new CommandAttack(source, entity, prj, gametime);
            }
        };
    }

    @Override
    public BoundingBox getHitbox() {
        return boundingBox;
    }

    @Override
    public float getIntersection(Vector3fc origin, Vector3fc direction, float gameTime) {
        return 0;
    }
}
