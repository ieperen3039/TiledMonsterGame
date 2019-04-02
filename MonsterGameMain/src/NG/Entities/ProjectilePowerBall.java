package NG.Entities;

import NG.Actions.ActionFly;
import NG.Actions.EntityAction;
import NG.Engine.Game;
import NG.MonsterSoul.Commands.CompoundAction;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shapes.GenericShapes;
import org.joml.Vector2ic;

/**
 * @author Geert van Ieperen created on 2-4-2019.
 */
public class ProjectilePowerBall extends Projectile {
    private static final GenericShapes mesh = GenericShapes.ICOSAHEDRON;

    private final EntityAction movement;

    public ProjectilePowerBall(
            Game game, Vector2ic startPosition, Vector2ic endPosition, float speed, float size, float height
    ) {
        super(game, size);
        movement = new CompoundAction(
                new ActionFly(game, startPosition, endPosition, speed, height)
        );
    }

    @Override
    protected EntityAction getMovement() {
        return movement;
    }

    @Override
    protected void drawProjectile(SGL gl) {
        gl.render(mesh, this);
    }
}
