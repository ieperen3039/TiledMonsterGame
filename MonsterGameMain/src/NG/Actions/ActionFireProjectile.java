package NG.Actions;

import NG.Animations.BodyAnimation;
import NG.Animations.UniversalAnimation;
import NG.Engine.Game;
import NG.Entities.MonsterEntity;
import NG.Entities.ProjectilePowerBall;
import NG.GameEvent.Event;
import NG.GameEvent.EventLoop;
import NG.GameState.GameState;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * the action of firing a single {@link ProjectilePowerBall}
 * @author Geert van Ieperen created on 5-4-2019.
 */
public class ActionFireProjectile extends ActionIdle {
    private static final float aniFireMoment = 0.5f;
    private final Type type;

    public enum Type {
        POWERBALL
    }

    // TODO find appropriate action
    public ActionFireProjectile(
            Game game, MonsterEntity source, Vector3fc target, Type type, float startTime, float duration
    ) {
        super(game, source.getLastAction(), duration, startTime);
        this.type = type;

        Vector3f spawnPosition = source.getPositionAt(startTime).add(0, 0, 1);

        SpawnEvent event = new SpawnEvent(game, spawnPosition, target, startTime + duration * ActionFireProjectile.aniFireMoment);
        game.get(EventLoop.class).addEvent(event);
    }

    @Override
    public UniversalAnimation getAnimation() {
        return BodyAnimation.IDLE;
    }

    private class SpawnEvent extends Event {
        private final Game game;
        private final Vector3f spawnPosition;
        private final Vector3fc target;

        public SpawnEvent(Game game, Vector3f spawnPosition, Vector3fc target, float eventTime) {
            super(eventTime);
            this.game = game;
            this.spawnPosition = spawnPosition;
            this.target = target;
        }

        @Override
        public void run() {
            if (!isCancelled() && endTime() >= eventTime) {
                ProjectilePowerBall projectile = new ProjectilePowerBall(
                        game, eventTime, 0.2f, 5f, spawnPosition, target
                );

                game.get(GameState.class).addEntity(projectile);
            }
        }
    }

    @Override
    public String toString() {
        return "Fire projectile (at " + Vectors.toString(getEndCoordinate()) + ")";
    }
}
