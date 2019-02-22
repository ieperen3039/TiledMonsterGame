package NG.MonsterSoul;

import NG.Engine.Game;
import NG.Entities.Actions.ActionIdle;
import NG.Entities.Actions.Command;
import NG.Entities.Actions.EntityAction;
import NG.Entities.CubeMonster;
import NG.Entities.MonsterEntity;
import NG.GameEvent.Event;
import NG.Storable;
import NG.Tools.Logger;
import org.joml.Vector2i;
import org.joml.Vector3fc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public class MonsterSoul implements Living, Storable, ActionFinishListener {
    private static final Iterator<EntityAction> NO_ACTIONS = Collections.emptyIterator();
    private final Emotion.Collection emotions; // TODO initialize

    private Game game;
    private MonsterEntity currentEntity;

    private ArrayDeque<Command> plan;
    private volatile Command executionTarget;
    private Iterator<EntityAction> executionSequence = NO_ACTIONS;

    /** restricts the number of pending action events to 1 */
    private Semaphore actionEventLock;
    private boolean isExecuting = false;


    public MonsterSoul(Path emotions) {
        this.emotions = new Emotion.Collection(emotions);
        this.plan = new ArrayDeque<>();
        this.actionEventLock = new Semaphore(1, false);
    }

    public MonsterEntity getAsEntity(Game game, Vector2i coordinate, Vector3fc direction) {
        this.game = game;

        if (currentEntity != null) {
            currentEntity.dispose();
        }
        return currentEntity = new CubeMonster(game, coordinate, direction, this);
    }

    @Override
    public void command(Command command) {
        // consider to reject
//        if (emotions.get(Emotion.RESPECT) > 100) ;

        plan.offer(command);

        if (executionTarget == null && actionEventLock.tryAcquire()) {
            EntityAction preceding = currentEntity.getLastAction();
            onActionFinish(preceding);
        }
    }

    @Override
    public void onActionFinish(EntityAction previous) {
        if (!(previous instanceof ActionIdle)) {
            Logger.DEBUG.print("Completed " + previous);
        }

        game.claims().dropClaim(previous.getStartPosition(), currentEntity);
        actionEventLock.release();

        while (!executionSequence.hasNext()) {// iterate to next plan.
            executionSequence = NO_ACTIONS; // free the list
            executionTarget = plan.poll();

            if (executionTarget == null) {
                return;
            }

            executionSequence = executionTarget.toActions(game, previous).iterator();
        }

        float now = game.timer().getGametime();
        EntityAction next = executionSequence.next();
        boolean hasClaim = game.claims().createClaim(next.getEndPosition(), currentEntity);

        if (hasClaim) {
            boolean success = schedule(next, now);
            assert success; // TODO handle (maybe always throw?)

        } else {
            // recollect a new execution sequence from the same target command
            executionSequence = executionTarget.toActions(game, previous).iterator();
            float hesitationPeriod = 0.5f;
            boolean success = schedule(new ActionIdle(game, previous, hesitationPeriod), now);
            assert success;
        }
    }

    /**
     * schedules an event that the given action has been finished.
     * @param action   the action to schedule
     * @param gameTime the start time of the action
     * @return true if the action has been scheduled, false if another action has already been scheduled.
     */
    private boolean schedule(EntityAction action, float gameTime) {
        Event event = action.getFinishEvent(gameTime, this);

        if (actionEventLock.tryAcquire()) {
            game.addEvent(event);
            currentEntity.currentActions.addAfter(gameTime, action);
            return true;
        }
        return false;
    }

    @Override
    public void accept(Stimulus stimulus) {
        // react
    }

    @Override
    public void writeToFile(DataOutput out) throws IOException {
        emotions.writeToFile(out);
    }

    public MonsterSoul(DataInput in) throws IOException {
        emotions = new Emotion.Collection(in);
    }
}
