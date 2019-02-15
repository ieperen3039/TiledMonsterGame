package NG.Entities.Actions;

import NG.Tools.Toolbox;
import org.joml.Vector3f;

import java.util.Arrays;

/**
 * Several {@link EntityAction}s combined into one action.
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class CompoundAction implements EntityAction {
    private final EntityAction[] actions;
    private final EntityAction lastAction;

    public CompoundAction(EntityAction first, EntityAction... others) {
        this(combine(first, others));
    }

    private static EntityAction[] combine(EntityAction first, EntityAction... others) {
        EntityAction[] actions = new EntityAction[others.length + 1];
        actions[0] = first;
        System.arraycopy(others, 0, actions, 1, others.length);
        return actions;
    }

    /**
     * combines a set of actions
     * @param actions the actions to be combined, not overlapping in time and agreeing in position. They may be in any
     *                order.
     */
    CompoundAction(EntityAction[] actions) {
        int nrOfActions = actions.length;

        // sort actions on start times
        this.actions = Arrays.copyOf(actions, nrOfActions);
        Toolbox.insertionSort(actions, EntityAction::getStartTime);
        this.lastAction = actions[actions.length - 1];

        // optimized checking for successive actions to follow each other
        int i = 0;
        Float lastEnd = null;
        EntityAction firstAction = actions[i++];
        while (lastEnd == null) lastEnd = firstAction.getEndTime();
        Vector3f lastPos = firstAction.getPositionAt(lastEnd);

        while (i < nrOfActions) {
            EntityAction action = actions[i++];

            if (action.isUndefined()) {
                action.setStartTime(lastEnd + action.duration());
            }

            float startTime = action.getStartTime();
            Vector3f startPos = action.getPositionAt(startTime);

            if (startTime < lastEnd) {
                throw new IllegalArgumentException("Compound actions overlap in execution time");
            }
            if (!startPos.equals(lastPos)) {
                throw new IllegalArgumentException("Compound actions do not agree on positions");
            }

            lastEnd = startTime;
            lastPos = startPos;
        }

    }

    @Override
    public Vector3f getPositionAt(float currentTime) {
        for (int i = 0; i < actions.length; i++) {
            if (actions[i].getStartTime() > currentTime) {
                return actions[i - 1].getPositionAt(currentTime);
            }
        }
        return lastAction.getPositionAt(currentTime);
    }

    @Override
    public Float getStartTime() {
        return actions[0].getStartTime();
    }

    /**
     * Set the start time of the first action, and move all next actions that have conflicts to the minimum start time.
     * @param time the time at which to start this action.
     */
    @Override
    public void setStartTime(float time) {
        int i = 0;
        EntityAction action = actions[i++];

        while (i < actions.length && action.getStartTime() < time) {
            action = actions[i++];
            action.setStartTime(time);
            time = action.getEndTime();
        }
    }

    @Override
    public Float getEndTime() {
        return lastAction.getEndTime();
    }

    @Override
    public void interrupt(float moment) {

    }

    @Override
    public float duration() {
        return getEndTime() - getStartTime();
    }
}
