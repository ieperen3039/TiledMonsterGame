package NG.MonsterSoul;

import NG.DataStructures.Generic.PairList;
import NG.Engine.Game;
import NG.Entities.MonsterEntity;
import NG.GameEvent.Actions.ActionFinishListener;
import NG.GameEvent.Actions.ActionIdle;
import NG.GameEvent.Actions.EntityAction;
import NG.GameEvent.Event;
import NG.MonsterSoul.Commands.Command;
import NG.MonsterSoul.Commands.Command.CType;
import NG.ScreenOverlay.Frames.Components.SNamedValue;
import NG.ScreenOverlay.Frames.Components.SPanel;
import NG.Storable;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.Vector2i;
import org.joml.Vector3fc;

import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

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

    private final Emotion.Collection emotions;
    private final Associator<Type> associationStimuli;
    private final Associator<CType> actionAssociator;

    /* TODO serialize */
    // mapping from stimulus to the perceived importance of the stimulus, as [0 ... 1]
    private final Map<Type, Float> importance;
    private final Map<Type, Emotion.Translation> stimulusEffects;
    private final EnumMap<Emotion, Float> emotionValue;

    protected Game game;
    private MonsterEntity entity;

    private Living commandFocus = this;
    private float focusRelevance = 0;

    private ArrayDeque<Command> plan;
    private volatile Command executionTarget;
    private Iterator<EntityAction> executionSequence = NO_ACTIONS;

    /** restricts the number of pending action events to 1 */
    private final Semaphore actionEventLock;

    /**
     * read a monster description from the given file
     * @param description a file that describes a monster
     * @throws IOException if any error occurs while reading the file
     */
    public MonsterSoul(File description) throws IOException {
        this.plan = new ArrayDeque<>();
        this.actionEventLock = new Semaphore(1, false);
        this.associationStimuli = new Associator<>(Type.class, ATTENTION_SIZE, ASSOCIATION_SIZE);
        this.actionAssociator = new Associator<>(CType.class, ATTENTION_SIZE, 4);

        this.importance = new HashMap<>();
        this.stimulusEffects = new HashMap<>();
        emotionValue = new EnumMap<>(Emotion.class);
        Emotion.Collection emotions = null;

        int lineNr = -1;
        try (
                Scanner reader = new Scanner(new FileInputStream(description))
        ) {
            while (reader.hasNext()) {
                String line = reader.nextLine().trim();
                lineNr++;
                if (line.isEmpty() || line.charAt(0) == '#') continue; // ignore comments and blank lines

                switch (line) {
                    case "stimulus:":
                        // sets importance and stimulusEffects
                        readStimulusValues(reader);
                        break;
                    case "emotion:":
                        // sets emotions
                        emotions = new Emotion.Collection(reader);
                        break;
                    case "value:":
                        // sets emotionValue
                        readEmotionValues(reader);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected input: " + line);
                }

            }
        } catch (Exception ex) {
            String message = (lineNr == -1) ? "Error while loading file" : "Error on line " + lineNr;
            throw new IOException(message, ex);
        }

        this.emotions = emotions;
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

        game.claims().dropClaim(previous.getStartCoordinate(), entity);
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
        boolean hasClaim = game.claims().createClaim(next.getEndCoordinate(), entity);

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
            entity.addAction(action, gameTime);
            return true;
        }
        return false;
    }

    public void update() {
        float gametime = game.timer().getGametime();
        emotions.process(gametime);
    }

    @Override
    public void accept(Stimulus stimulus) {
        update();

        float gametime = game.timer().getGametime();
        Type sType = stimulus.getType();
        float realMagnitude = stimulus.getMagnitude(entity.getPosition(gametime));

        Logger.DEBUG.print(stimulus, realMagnitude);
        if (realMagnitude < MINIMUM_NOTICE_MAGNITUDE) return;

        float mainJudge = importance.computeIfAbsent(sType, s ->
                // initial judgement of importance depends on emotional influence
                stimulusEffects.containsKey(s) ?
                        1 - (0.9f / (1 + Math.abs(stimulusEffects.get(s).calculateValue(emotionValue)))) :
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

        Logger.DEBUG.print(commandFocus, relativeMagnitude);
        if (relativeMagnitude < MINIMUM_NOTICE_MAGNITUDE) return;

        // calculate projected gain for a number of target actions
        CType best = getDesiredAction(stimulus, relativeMagnitude);
        // if all actions dont pass the MINIMUM_NOTICE_MAGNITUDE
        if (best == null) return;

        // otherwise, execute action
        Command command = best.generateNew(entity, stimulus, gametime);
        acceptCommand(command);
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
     * Calculate an 'emotion gain' value for the given stimulus, how 'good' it is. It uses associations to predict
     * future effects. Can be used to evaluate a plan, by supplying a CType object.
     * @param stimulusType the stimulus to evaluate
     * @param relevance    how relevant the stimulus is, e.g. how likely it is to appear.
     * @return a value that
     */
    public float getGainOf(Type stimulusType, float relevance) {
        Emotion.Translation moveEffect = stimulusEffects.get(stimulusType);
        float moveGain = moveEffect.calculateValue(emotionValue);

        PairList<Type, Float> prediction =
                associationStimuli.query(stimulusType, PREDICITON_BRANCH_SIZE).asPairList();

        for (int i = 0; i < prediction.size(); i++) {
            Type elt = prediction.left(i);
            float eltRel = prediction.right(i);
            // may recurse here, on condition of relevance
            Emotion.Translation eltEffect = stimulusEffects.get(elt);
            moveGain += eltEffect.calculateValue(emotionValue) * eltRel;
        }

        moveGain *= relevance;

        return moveGain;
    }

    /**
     * Execute the given command
     * @param c the command considered by this unit
     */
    private void acceptCommand(Command c) {
        plan.offer(c);

        if (executionTarget == null) {
            EntityAction preceding = entity.getLastAction();
            onActionFinish(preceding);
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
        emotionValue = new EnumMap<>(Emotion.class);

        emotions = new Emotion.Collection(in);
        associationStimuli = new Associator<>(in, Type.class);
        actionAssociator = new Associator<>(in, CType.class);
    }

    private void readStimulusValues(Scanner reader) {
        Pattern colonMark = Pattern.compile(":");
        String line;
        while (!(line = reader.nextLine()).equals("end")) {
            if (line.isEmpty() || line.charAt(0) == '#') continue;
            String[] elts = Toolbox.WHITESPACE_PATTERN.split(line.trim());

            Type slt = Stimulus.getByName(elts[0]);
            float value = Float.parseFloat(elts[1]);
            importance.put(slt, value);

            Emotion.Translation mapping = new Emotion.Translation();
            for (int i = 2; i < elts.length; i++) {
                String[] pair = colonMark.split(elts[i]);
                Emotion emotion = Emotion.valueOf(pair[0]);
                int change = Integer.parseInt(pair[1]);

                mapping.set(emotion, change);
            }

            stimulusEffects.put(slt, mapping);
        }
    }

    private void readEmotionValues(Scanner reader) {
        String line;
        while (!(line = reader.nextLine()).equals("end")) {
            if (line.isEmpty() || line.charAt(0) == '#') continue;
            String[] elts = Toolbox.WHITESPACE_PATTERN.split(line.trim());

            Emotion emotion = Emotion.valueOf(elts[0]);
            float value = Float.parseFloat(elts[1]);
            emotionValue.put(emotion, value);
        }
    }

    public SPanel getStatisticsPanel(int buttonHeight) {
        SPanel state = new SPanel(1, Emotion.count + 1);
        state.add(new SNamedValue("General Joy", () -> emotions.calculateJoy(emotionValue), buttonHeight), new Vector2i(0, 0));
        int i = 1;
        for (Emotion emotion : Emotion.values()) {
            state.add(new SNamedValue(emotion.name(), () -> emotions.get(emotion), buttonHeight), new Vector2i(0, i++));
        }
        return state;
    }
}
