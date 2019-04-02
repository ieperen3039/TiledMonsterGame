package NG.MonsterSoul.Commands;

import NG.Animations.CompoundAnimation;
import NG.Animations.UniversalAnimation;
import NG.GameEvent.Actions.EntityAction;
import org.joml.Quaternionf;
import org.joml.Vector2ic;
import org.joml.Vector3f;

import java.util.Arrays;

/**
 * Several {@link EntityAction EntityActions} combined into one action.
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class CompoundAction implements EntityAction {
    private final EntityAction[] actions;
    private final EntityAction lastAction;
    private final float totalDuration;
    private final CompoundAnimation animations;

    /**
     * combines a set of actions
     * @param actionArray the actions to be combined, following each other in position the way they are ordered
     */
    public CompoundAction(EntityAction... actionArray) {
        int nrOfActions = actionArray.length;
        assert nrOfActions > 0;

        EntityAction prevAction = actionArray[0];
        float duration = prevAction.duration();
        UniversalAnimation[] actionAnimations = new UniversalAnimation[nrOfActions];

        for (int i = 1; i < actionArray.length; i++) {
            EntityAction action = actionArray[i];

            if (action == null) {
                throw new NullPointerException(String.format("action %d/%d was null", i + 1, actionArray.length));

            } else if (!action.follows(prevAction)) {
                throw new IllegalArgumentException(String.format("action %d/%d: %s does not follow %s", i + 1, actionArray.length, action, prevAction));
            }

            actionAnimations[i] = action.getAnimation();
            prevAction = action;
            duration += action.duration();
        }

        actions = actionArray;
        animations = new CompoundAnimation(actionAnimations);
        this.lastAction = prevAction;
        this.totalDuration = duration;
    }

    @Override
    public Vector3f getPositionAfter(float timeSinceStart) {
        if (timeSinceStart <= 0) {
            return actions[0].getPositionAfter(0);

        } else if (timeSinceStart >= totalDuration) {
            float end = lastAction.duration();
            return lastAction.getPositionAfter(end);
        }

        for (EntityAction action : actions) {
            float duration = action.duration();
            if (timeSinceStart > duration) {
                timeSinceStart -= duration;

            } else {
                return action.getPositionAfter(timeSinceStart);
            }
        }

        throw new AssertionError("invalid value of totalDuration, missing " + timeSinceStart);
    }

    @Override
    public Quaternionf getRotation(float timeSinceStart) {
        if (timeSinceStart <= 0) {
            return actions[0].getRotation(0);

        } else if (timeSinceStart >= totalDuration) {
            float end = lastAction.duration();
            return lastAction.getRotation(end);
        }

        for (EntityAction action : actions) {
            float duration = action.duration();
            if (timeSinceStart > duration) {
                timeSinceStart -= duration;

            } else {
                return action.getRotation(timeSinceStart);
            }
        }

        throw new AssertionError("invalid value of totalDuration, missing " + timeSinceStart);
    }

    @Override
    public Vector2ic getEndCoordinate() {
        return lastAction.getEndCoordinate();
    }

    @Override
    public Vector2ic getStartCoordinate() {
        return actions[0].getStartCoordinate();
    }

    @Override
    public float duration() {
        return totalDuration;
    }

    @Override
    public UniversalAnimation getAnimation() {
        return animations;
    }

    @Override
    public String toString() {
        return Arrays.toString(actions);
    }
}
