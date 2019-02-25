package NG.MonsterSoul.Commands;

import NG.Entities.Actions.EntityAction;
import org.joml.Vector2ic;
import org.joml.Vector3fc;

import java.util.Arrays;

/**
 * Several {@link EntityAction}s combined into one action.
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class CompoundAction implements EntityAction {
    private final EntityAction[] actions;
    private final EntityAction lastAction;
    private final float totalDuration;

    /**
     * combines a set of actions
     * @param actionArray the actions to be combined, following each other in position the way they are ordered
     */
    public CompoundAction(EntityAction... actionArray) {
        int nrOfActions = actionArray.length;
        assert nrOfActions > 0;

        EntityAction prevAction = actionArray[0];
        float duration = prevAction.duration();

        for (int i = 1; i < actionArray.length; i++) {
            EntityAction action = actionArray[i];

            if (action == null) {
                throw new NullPointerException(String.format("action %d/%d was null", i + 1, actionArray.length));

            } else if (!action.follows(prevAction)) {
                throw new IllegalArgumentException(String.format("action %d/%d: %s does not follow %s", i + 1, actionArray.length, action, prevAction));
            }

            prevAction = action;
            duration += action.duration();
        }

        actions = new EntityAction[nrOfActions];
        System.arraycopy(actionArray, 0, actions, 0, nrOfActions);

        this.lastAction = prevAction;
        this.totalDuration = duration;
    }

    @Override
    public Vector3fc getPositionAfter(float passedTime) {
        if (passedTime <= 0) {
            return actions[0].getPositionAfter(0);

        } else if (passedTime >= totalDuration) {
            float end = lastAction.duration();
            return lastAction.getPositionAfter(end);
        }

        for (EntityAction action : actions) {
            float duration = action.duration();
            if (passedTime > duration) {
                passedTime -= duration;

            } else {
                return action.getPositionAfter(passedTime);
            }
        }

        throw new AssertionError("invalid value of totalDuration, missing " + passedTime);
    }

    @Override
    public Vector2ic getEndPosition() {
        return lastAction.getEndPosition();
    }

    @Override
    public Vector2ic getStartPosition() {
        return actions[0].getStartPosition();
    }

    @Override
    public float duration() {
        return totalDuration;
    }

    @Override
    public String toString() {
        return Arrays.toString(actions);
    }
}
