package NG.Living;

import NG.Actions.ActionIdle;
import NG.Actions.BrokenMovementException;
import NG.Actions.Commands.Command;
import NG.Actions.Commands.Command.CType;
import NG.Actions.EntityAction;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Generic.Pair;
import NG.DataStructures.Generic.PairList;
import NG.Entities.EntityStatistics;
import NG.Entities.MonsterEntity;
import NG.GUIMenu.Components.SNamedValue;
import NG.GUIMenu.Components.SPanel;
import NG.Storable;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public abstract class MonsterSoul implements Living, Storable {
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
    private String monsterName;

    private int hitpoints;
    private EntityStatistics stats;

    private Living commandFocus = this;
    private float focusRelevance = 0;

    private ArrayDeque<Command> plan;
    private volatile Command executionTarget;

    /**
     * read a monster description from the given file
     * @param soulDescription a file that describes a monster
     */
    public MonsterSoul(SoulDescription soulDescription) {
        this.plan = new ArrayDeque<>();
        this.associationStimuli = new Associator<>(Type.class, ATTENTION_SIZE, ASSOCIATION_SIZE);
        this.actionAssociator = new Associator<>(CType.class, ATTENTION_SIZE, 4);

        this.monsterName = soulDescription.name;
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

    /**
     * computes which action should be executed at the given time
     * @param gameTime the start time of the next action
     * @return the action planned by this entity, or null if nothing is planned
     */
    public EntityAction getNextAction(float gameTime) {
        Pair<EntityAction, Float> action = entity.currentActions.getActionAt(gameTime);
        EntityAction previous = action.left;
        Vector3f position = previous.getPositionAt(action.right);

        if (executionTarget == null) {
            executionTarget = plan.poll();
        }

        while (executionTarget != null) {
            EntityAction next = executionTarget.getAction(game, position, gameTime);

            if (next != null) {
                if (next != previous && !next.getStartPosition().equals(position)) {
                    throw new BrokenMovementException(previous, next, action.right);
                }

                return next;
            }

            executionTarget = plan.poll();
        }

        return new ActionIdle(position);
    }

    public void update(float gametime) {
        emotions.process(gametime);
    }

    @Override
    public void accept(Stimulus stimulus) {
        float gametime = game.get(GameTimer.class).getGametime();
        Type sType = stimulus.getType();
        float realMagnitude = stimulus.getMagnitude(entity.getPositionAt(gametime));

//        Logger.DEBUG.print(stimulus, realMagnitude);
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
        queueCommand(command);
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
     * Execute the given command at the end of the current plan
     * @param c the command to be executed.
     */
    public void queueCommand(Command c) {
        plan.offer(c);

        if (executionTarget == null) {
            float now = game.get(GameTimer.class).getGametime();
            EntityAction nextAction = getNextAction(now);
            entity.currentActions.insert(nextAction, now);
        }
    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        emotions.writeToDataStream(out);
        associationStimuli.writeToDataStream(out);
        actionAssociator.writeToDataStream(out);
    }

    public MonsterSoul(DataInputStream in) throws IOException, ClassNotFoundException {
        this.plan = new ArrayDeque<>();
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
                new SNamedValue("Health points", () -> hitpoints, buttonHeight),
                new SNamedValue("Happiness", () -> String.format("%1.02f", emotions.calculateJoy(emotionValues)), buttonHeight)
        );
    }

    @Override
    public String toString() {
        return monsterName;
    }

    @Override
    public MonsterEntity entity() {
        return entity;
    }
}
