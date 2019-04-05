package NG.MonsterSoul.Commands;

import NG.Actions.EntityAction;
import NG.Animations.CompoundAnimation;
import NG.Animations.UniversalAnimation;
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
    private final CompoundAnimation animations;
    private float endTime;

    /**
     * combines a set of actions
     * @param actionArray the actions to be combined, following each other in position the way they are ordered
     */
    public CompoundAction(EntityAction... actionArray) {
        int nrOfActions = actionArray.length;
        assert nrOfActions > 0;

        EntityAction prevAction = actionArray[0];
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
        }

        this.actions = actionArray;
        this.animations = new CompoundAnimation(actionAnimations);
        this.lastAction = prevAction;
        this.endTime = prevAction.endTime();
    }

    @Override
    public Vector3f getPositionAt(float currentTime) {
        if (currentTime <= 0) {
            return actions[0].getPositionAt(currentTime);

        } else if (currentTime >= endTime) {
            return lastAction.getPositionAt(currentTime);
        }

        for (EntityAction action : actions) {
            if (action.endTime() >= currentTime) {
                return action.getPositionAt(currentTime);
            }
        }

        throw new AssertionError("currentTime >= endTime");
    }

    @Override
    public Quaternionf getRotationAt(float currentTime) {
        if (currentTime <= 0) {
            return actions[0].getRotationAt(currentTime);

        } else if (currentTime >= endTime) {
            return lastAction.getRotationAt(currentTime);
        }

        for (EntityAction action : actions) {
            if (action.endTime() >= currentTime) {
                return action.getRotationAt(currentTime);
            }
        }

        throw new AssertionError("currentTime >= endTime");
    }

    @Override
    public boolean isCancelled() {
        return actions[0].isCancelled();
    }

    @Override
    public Vector2ic getEndCoordinate() {
        return lastAction.getEndCoordinate();
    }

    @Override
    public float startTime() {
        return actions[0].startTime();
    }

    @Override
    public Vector2ic getStartCoordinate() {
        return actions[0].getStartCoordinate();
    }

    @Override
    public UniversalAnimation getAnimation() {
        return animations;
    }

    @Override
    public String toString() {
        return Arrays.toString(actions);
    }

    @Override
    public float endTime() {
        return endTime;
    }

    @Override
    public void interrupt(float time) {
        if (time < endTime) {
            for (EntityAction action : actions) {
                if (action.endTime() >= time) {
                    // this action and all later actions as well
                    action.interrupt(time);
                }
            }

            endTime = time;
        }
    }
}
