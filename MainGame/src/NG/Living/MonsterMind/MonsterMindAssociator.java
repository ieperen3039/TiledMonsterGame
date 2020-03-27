package NG.Living.MonsterMind;

import NG.Actions.Commands.Command;
import NG.DataStructures.Generic.PairList;
import NG.Entities.Entity;
import NG.Living.*;

import java.util.EnumMap;
import java.util.Map;

public class MonsterMindAssociator extends MonsterMind {
    private static final int ASSOCIATION_SIZE = 10;
    private static final int ACTION_CONSIDERATION_SIZE = 4;
    private static final int PREDICITON_BRANCH_SIZE = 4;

    private static final float MINIMUM_NOTICE_MAGNITUDE = 1e-3f;
    private static final int ATTENTION_SIZE = 6;
    private final Emotion.ECollection emotions;
    private final Associator<StimulusType> associationStimuli;
    private final Associator<CommandSType> actionAssociator;
    // mapping from stimulus to the perceived importance of the stimulus, as [0 ... 1]
    private final Map<StimulusType, Float> importance;
    private final Map<StimulusType, Emotion.Translation> stimulusEffects;
    private final EnumMap<Emotion, Float> emotionValues;
    private Living commandFocus;
    private float focusRelevance = 0;

    public MonsterMindAssociator(MonsterSoul owner, SoulDescription soulDescription) {
        super(owner);
        this.associationStimuli = new Associator<>(StimulusType.class, MonsterMindAssociator.ATTENTION_SIZE, ASSOCIATION_SIZE);
        this.actionAssociator = new Associator<>(CommandSType.class, MonsterMindAssociator.ATTENTION_SIZE, 4);
        this.importance = soulDescription.importance;
        this.stimulusEffects = soulDescription.stimulusEffects;
        this.emotionValues = soulDescription.emotionValues;
        this.emotions = soulDescription.emotions;
    }

    @Override
    public void update(float gametime) {
        emotions.process(gametime);
    }

    @Override
    public void accept(Stimulus stimulus) {
        float gametime = stimulus.getTime();
        StimulusType sType = stimulus.getType();
        float realMagnitude = stimulus.getMagnitude(entity.getPositionAt(gametime));

//        Logger.DEBUG.print(stimulus, realMagnitude);
        if (realMagnitude < MINIMUM_NOTICE_MAGNITUDE) return;

        // calculate projected gain for a number of target actions
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
            actionAssociator.record((CommandSType) sType, relativeMagnitude);

            if (relativeMagnitude > focusRelevance) {
                commandFocus = (Living) asCommand;
                focusRelevance = relativeMagnitude;
            }

            if (commandFocus == asCommand) {
                actionAssociator.notice(sType, relativeMagnitude);
            }

        } else {
            actionAssociator.notice(sType, relativeMagnitude);
        }

        // if all actions dont pass the MINIMUM_NOTICE_MAGNITUDE
        if (relativeMagnitude < MINIMUM_NOTICE_MAGNITUDE) return;

        // calculate projected gain for a number of target actions
        CommandSType best = getDesiredAction(stimulus, relativeMagnitude, owner);
        // if all actions dont pass the MINIMUM_NOTICE_MAGNITUDE
        if (best == null) return;

        // otherwise, execute action
        Command command = best.generateNew(entity, stimulus, gametime);
        executeCommand(command, gametime);
    }

    /**
     * Evaluates the current state of mind and returns the command it wants to execute
     * @param stimulus          the stimulus that caused the consideration
     * @param relativeMagnitude relative magnitude of the stimulus
     * @param thisSoul
     * @return the type of command to execute.
     */
    private CommandSType getDesiredAction(Stimulus stimulus, float relativeMagnitude, Living thisSoul) {
        PairList<CommandSType, Float> actions =
                actionAssociator.query(stimulus.getType(), ACTION_CONSIDERATION_SIZE).asPairList();

        float max = MINIMUM_NOTICE_MAGNITUDE;
        CommandSType best = null;

        if (stimulus instanceof Command) {
            // consider executing the command
            Command command = (Command) stimulus;
            Living target = (Living) command;
            if (target != null && target.equals(thisSoul)) { // may be redundant

                best = (CommandSType) stimulus.getType();
                max = getGainOf(best, relativeMagnitude);
            }
        }

        for (int i = 0; i < actions.size(); i++) {
            CommandSType moveType = actions.left(i);
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
    private float getGainOf(StimulusType stimulusType, float relevance) {
        Emotion.Translation moveEffect = stimulusEffects.get(stimulusType);
        float moveGain = moveEffect.calculateValue(emotionValues);

        PairList<StimulusType, Float> prediction =
                associationStimuli.query(stimulusType, PREDICITON_BRANCH_SIZE).asPairList();

        for (int i = 0; i < prediction.size(); i++) {
            StimulusType elt = prediction.left(i);
            float eltRel = prediction.right(i);
            // may recurse here, on condition of relevance
            Emotion.Translation eltEffect = stimulusEffects.get(elt);
            moveGain += eltEffect.calculateValue(emotionValues) * eltRel;
        }

        moveGain *= relevance;

        return moveGain;
    }

    @Override
    public void reactEntityCollision(Entity other, float collisionTime) {
    }
}
