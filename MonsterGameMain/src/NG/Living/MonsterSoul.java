package NG.Living;

import NG.Actions.ActionFinishListener;
import NG.Actions.ActionIdle;
import NG.Actions.EntityAction;
import NG.DataStructures.Generic.PairList;
import NG.Engine.Game;
import NG.Engine.GameTimer;
import NG.Entities.EntityStatistics;
import NG.Entities.MonsterEntity;
import NG.GUIMenu.Frames.Components.SNamedValue;
import NG.GUIMenu.Frames.Components.SPanel;
import NG.GameEvent.Event;
import NG.GameEvent.EventLoop;
import NG.GameMap.ClaimRegistry;
import NG.Living.Commands.Command;
import NG.Living.Commands.Command.CType;
import NG.Storable;
import NG.Tools.Logger;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3fc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public abstract class MonsterSoul implements Living, Storable, ActionFinishListener {
    private static final Iterator<EntityAction> NO_ACTIONS = Collections.emptyIterator();
    private static final float MINIMUM_NOTICE_MAGNITUDE = 1e-3f;

    private static final int ATTENTION_SIZE = 6;
    private static final int ASSOCIATION_SIZE = 10;
    private static final int ACTION_CONSIDERATION_SIZE = 4;
    private static final int PREDICITON_BRANCH_SIZE = 4;

    private final Emotion.ECollection emotions;
    private final Associator<Type> associationStimuli;
    private final Associator<CType> actionAssociator;

    /* TODO serialize */
    // mapping from stimulus to the perceived importance of the stimulus, as [0 ... 1]
    private final Map<Type, Float> importance;
    private final Map<Type, Emotion.Translation> stimulusEffects;
    private final EnumMap<Emotion, Float> emotionValues;

    protected Game game;
    private MonsterEntity entity;

    private int hitpoints;
    private EntityStatistics stats;

    private Living commandFocus = this;
    private float focusRelevance = 0;

    private ArrayDeque<Command> plan;
    private volatile Command executionTarget;
    private Iterator<EntityAction> executionSequence = NO_ACTIONS;

    /** restricts the number of pending action events to 1 */
    private final Semaphore actionEventLock;

    /**
     * read a monster description from the given file
     * @param soulDescription
     */
    public MonsterSoul(SoulDescription soulDescription) {
        this.plan = new ArrayDeque<>();
        this.actionEventLock = new Semaphore(1, false);
        this.associationStimuli = new Associator<>(Type.class, ATTENTION_SIZE, ASSOCIATION_SIZE);
        this.actionAssociator = new Associator<>(CType.class, ATTENTION_SIZE, 4);

        this.importance = soulDescription.importance;
        this.stimulusEffects = soulDescription.stimulusEffects;
        this.emotionValues = soulDescription.emotionValues;
        this.emotions = soulDescription.emotions;

        this.stats = new EntityStatistics(100);
        this.hitpoints = stats.hitPoints;
    }

    public MonsterEntity getAsEntity(Game game, Vector2i coordinate, Vector3fc direction) {
        this.game = game;

        if (entity != null) {
            entity.dispose();
        }

        return entity = getNewEntity(game, coordinate, direction);
    }

    protected abstract MonsterEntity getNewEntity(Game game, Vector2i coordinate, Vector3fc direction);

    @Override
    public void onActionFinish(EntityAction previous) {
        if (!(previous instanceof ActionIdle)) {
            Logger.DEBUG.print("Completed " + previous);
        }

        ClaimRegistry claims = game.get(ClaimRegistry.class);

        Vector2ic startCoord = previous.getStartCoordinate();
        Vector2ic endCoord = previous.getEndCoordinate();
        if (!endCoord.equals(startCoord)) {
            claims.dropClaim(startCoord, entity);
        }

        actionEventLock.release();

        scheduleNext(previous, previous.endTime());
    }

    /**
     * queries the next action planned, and executes it at the given start time.
     * @param previous        the previous action executed
     * @param actionStartTime the start time of the next action
     */
    private void scheduleNext(EntityAction previous, float actionStartTime) {
        while (!executionSequence.hasNext()) {// iterate to next plan.
            executionSequence = NO_ACTIONS; // free the list
            executionTarget = plan.poll();

            if (executionTarget == null) {
                return;
            }

            executionSequence = executionTarget.toActions(game, previous, actionStartTime).iterator();
        }

        EntityAction next = executionSequence.next();
        ClaimRegistry claims = game.get(ClaimRegistry.class);

        Vector2ic startCoord = next.getStartCoordinate();
        Vector2ic endCoord = next.getEndCoordinate();

        boolean hasClaim = startCoord.equals(endCoord) || claims.createClaim(endCoord, entity);

        if (hasClaim) {
            boolean success = createEvent(next, actionStartTime);
            assert success; // TODO handle (maybe always throw?)

        } else {
            // recollect a new execution sequence from the same target command
            executionSequence = executionTarget.toActions(game, previous, actionStartTime).iterator();
            float hesitationPeriod = 0.5f;
            boolean success = createEvent(new ActionIdle(game, previous, hesitationPeriod, actionStartTime), actionStartTime);
            assert success;
        }
    }

    /**
     * schedules an event that the given action has been finished.
     * @param action     the action to schedule
     * @param actionTime the start time of the action
     * @return true if the action is scheduled successfully, false if another action has already been scheduled.
     */
    private boolean createEvent(EntityAction action, float actionTime) {
        Event event = action.getFinishEvent(this);

        if (actionEventLock.tryAcquire()) {
            game.get(EventLoop.class).addEvent(event);
            Logger.DEBUG.print(this + " executes " + action + " at " + actionTime);
            entity.currentActions.addAfter(actionTime, action);
            return true;
        }
        return false;
    }

    @Override
    public void accept(Stimulus stimulus) {
        float gametime = game.get(GameTimer.class).getGametime();
        Type sType = stimulus.getType();
        float realMagnitude = stimulus.getMagnitude(entity.getPositionAt(gametime));

        if (realMagnitude < MINIMUM_NOTICE_MAGNITUDE) return;

        float mainJudge = importance.computeIfAbsent(sType, s ->
                // initial judgement of importance depends on emotional influence
                stimulusEffects.containsKey(s) ?
                        1 - (0.9f / (1 + Math.abs(stimulusEffects.get(s).calculateValue(emotionValues)))) :
                        0.1f
        );
        float relativeMagnitude = realMagnitude * realMagnitude * mainJudge;

        Emotion.Translation sEffect = stimulusEffects.get(sType);
        sEffect.addTo(emotions, realMagnitude);

        // process stimulus in associations
        associationStimuli.record(sType, realMagnitude);
        associationStimuli.notice(sType, realMagnitude);

        focusRelevance *= (1 - (1f / emotions.get(Emotion.EXCITEMENT)));
        if (stimulus instanceof Command) {
            Command asCommand = (Command) stimulus;
            actionAssociator.record((CType) sType, relativeMagnitude);

            if (relativeMagnitude > focusRelevance) {
                commandFocus = asCommand.getTarget();
                focusRelevance = relativeMagnitude;
            }

            if (commandFocus == asCommand.getTarget()) {
                actionAssociator.notice(sType, relativeMagnitude);
            }

        } else {
            actionAssociator.notice(sType, relativeMagnitude);
        }

        if (relativeMagnitude < MINIMUM_NOTICE_MAGNITUDE) return;

        // calculate projected gain for a number of target actions
        CType best = getDesiredAction(stimulus, relativeMagnitude);
        // if all actions dont pass the MINIMUM_NOTICE_MAGNITUDE
        if (best == null) return;

        // otherwise, execute action
        Command command = best.generateNew(entity, stimulus, gametime);
        executeCommand(command);
    }

    /**
     * Evaluates the current state of mind and returns the command it wants to execute
     * @param stimulus          the stimulus that caused the consideration
     * @param relativeMagnitude relative magnitude of the stimulus
     * @return the type of command to execute.
     */
    private CType getDesiredAction(Stimulus stimulus, float relativeMagnitude) {
        PairList<CType, Float> actions =
                actionAssociator.query(stimulus.getType(), ACTION_CONSIDERATION_SIZE).asPairList();

        float max = MINIMUM_NOTICE_MAGNITUDE;
        CType best = null;

        if (stimulus instanceof Command) {
            // consider executing the command
            Command command = (Command) stimulus;
            Living target = command.getTarget();
            if (target != null && target.equals(this)) { // may be redundant

                best = (CType) stimulus.getType();
                max = getGainOf(best, relativeMagnitude);
            }
        }

        for (int i = 0; i < actions.size(); i++) {
            CType moveType = actions.left(i);
            float relevance = actions.right(i);

            float value = getGainOf(moveType, relevance);
            if (value > max) {
                max = value;
                best = moveType;
            }
        }

        return best;
    }

    /**
     * Calculate an 'emotional gain' value for the given stimulus, how 'good' it is. It uses associations to predict
     * future effects. Can be used to evaluate a plan, by supplying a CType object.
     * @param stimulusType the stimulus to evaluate
     * @param relevance    how relevant the stimulus is, e.g. how likely it is to appear.
     * @return a value that
     */
    private float getGainOf(Type stimulusType, float relevance) {
        Emotion.Translation moveEffect = stimulusEffects.get(stimulusType);
        float moveGain = moveEffect.calculateValue(emotionValues);

        PairList<Type, Float> prediction =
                associationStimuli.query(stimulusType, PREDICITON_BRANCH_SIZE).asPairList();

        for (int i = 0; i < prediction.size(); i++) {
            Type elt = prediction.left(i);
            float eltRel = prediction.right(i);
            // may recurse here, on condition of relevance
            Emotion.Translation eltEffect = stimulusEffects.get(elt);
            moveGain += eltEffect.calculateValue(emotionValues) * eltRel;
        }

        moveGain *= relevance;

        return moveGain;
    }

    /**
     * Execute the given command
     * @param c the command to be executed.
     */
    public void executeCommand(Command c) {
        plan.offer(c);

        if (executionTarget == null) {
            EntityAction preceding = entity.getLastAction();
            float now = game.get(GameTimer.class).getGametime();
            scheduleNext(preceding, now);
        }
    }

    @Override
    public void writeToDataStream(DataOutput out) throws IOException {
        emotions.writeToDataStream(out);
        associationStimuli.writeToDataStream(out);
        actionAssociator.writeToDataStream(out);
    }

    public MonsterSoul(DataInput in) throws IOException, ClassNotFoundException {
        this.plan = new ArrayDeque<>();
        this.actionEventLock = new Semaphore(1, false);
        importance = new HashMap<>(); // TODO serialize
        stimulusEffects = new HashMap<>();
        emotionValues = new EnumMap<>(Emotion.class);

        emotions = new Emotion.ECollection(in);
        associationStimuli = new Associator<>(in, Type.class);
        actionAssociator = new Associator<>(in, CType.class);
        stats = new EntityStatistics(100);
    }

    public SPanel getStatisticsPanel(int buttonHeight) {
        return SPanel.column(
                new SNamedValue("Happiness", () -> emotions.calculateJoy(emotionValues), buttonHeight),
                new SNamedValue("Health points", () -> hitpoints, buttonHeight)
        );
    }

    @Override
    public String toString() {
        return entity.toString();
    }
}
