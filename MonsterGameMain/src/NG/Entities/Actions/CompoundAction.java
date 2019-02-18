package NG.Entities.Actions;

import org.joml.Vector3f;

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
     * @param others the actions to be combined, following each other in position the way they are ordered
     */
    public CompoundAction(EntityAction... others) {
        int nrOfActions = others.length;

        EntityAction lastAction = others[0];
        float duration = lastAction.duration();

        for (int i = 1; i < others.length; i++) {
            EntityAction action = others[i];

            if (!action.follows(lastAction)) {
                throw new IllegalArgumentException("action " + action + " and " + lastAction + " do not follow on position");
            }

            duration += action.duration();
        }

        actions = new EntityAction[nrOfActions];
        System.arraycopy(others, 0, actions, 0, nrOfActions);

        this.lastAction = others[others.length - 1];
        totalDuration = duration;
    }

    @Override
    public Vector3f getPositionAfter(float passedTime) {
        for (EntityAction action : actions) {
            if (passedTime > action.duration()) {
                passedTime -= action.duration();

            } else {
                return action.getPositionAfter(passedTime);
            }
        }

        return lastAction.getEndPosition();
    }

    @Override
    public Vector3f getEndPosition() {
        return lastAction.getEndPosition();
    }

    @Override
    public Vector3f getStartPosition() {
        return actions[0].getStartPosition();
    }

    @Override
    public float interrupt(float passedTime) {
        float remainder = passedTime;

        for (EntityAction action : actions) {
            if (remainder > action.duration()) {
                remainder -= action.duration();

            } else {
                return action.duration() - remainder + passedTime;
            }
        }

        return passedTime;
    }

    @Override
    public float duration() {
        return totalDuration;
    }
}
