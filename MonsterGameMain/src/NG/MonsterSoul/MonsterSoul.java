package NG.MonsterSoul;

import NG.DataStructures.Generic.PairList;
import NG.Engine.Game;
import NG.Entities.Actions.ActionFinishListener;
import NG.Entities.Actions.ActionIdle;
import NG.Entities.Actions.EntityAction;
import NG.Entities.CubeMonster;
import NG.Entities.MonsterEntity;
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
public class MonsterSoul implements Living, Storable, ActionFinishListener {
    private static final Iterator<EntityAction> NO_ACTIONS = Collections.emptyIterator();
    private static final float MINIMUM_NOTICE_MAGNITUDE = 1e-3f;

    private static final int ATTENTION_SIZE = 6;
    private static final int ASSOCIATION_SIZE = 10;
    private static final int ACTION_CONSIDERATION_SIZE = 4;

    private final Emotion.Collection emotions;
    private final Associator<Type> associationEngine;
    private final Associator<CType> actionAssociator;

    /* TODO serialize */
    // mapping from stimulus to the perceived importance of the stimulus, as [0 ... 1]
    private final Map<Type, Float> importance;
    private final Map<Type, Emotion.Translation> stimulusEffects;
    private final EnumMap<Emotion, Float> emotionValue;

    protected Game game;
    private MonsterEntity entity;

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
        this.associationEngine = new Associator<>(Type.class, ATTENTION_SIZE, ASSOCIATION_SIZE);
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
            String message = lineNr == -1 ? "Error while loading file" : "Error while reading line " + lineNr;
            throw new IOException(message, ex);
        }

        this.emotions = emotions;
    }

    public MonsterEntity getAsEntity(Game game, Vector2i coordinate, Vector3fc direction) {
        this.game = game;

        if (entity != null) {
            entity.dispose();
        }
        return entity = new CubeMonster(game, coordinate, direction, this);
    }

    @Override
    public void onActionFinish(EntityAction previous) {
        if (!(previous instanceof ActionIdle)) {
            Logger.DEBUG.print("Completed " + previous);
        }

        game.claims().dropClaim(previous.getStartPosition(), entity);
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
        boolean hasClaim = game.claims().createClaim(next.getEndPosition(), entity);

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
        Type sType = stimulus.getType();

        float mainJudge = importance.computeIfAbsent(sType, s ->
                // initial judgement of importance depends on emotional influence
                1 - (1 / (1 + Math.abs(stimulusEffects.get(s).calculateValue(emotionValue))))
        );
        float realMagnitude = stimulus.getMagnitude(entity.getPosition());
        float relativeMagnitude = realMagnitude * realMagnitude * mainJudge;
        if (relativeMagnitude < MINIMUM_NOTICE_MAGNITUDE) return;

        Emotion.Translation sEffect = stimulusEffects.get(sType);
        sEffect.addTo(emotions, relativeMagnitude);

        // calculate projected gain for a number of target actions
        PairList<CType, Float> actions =
                actionAssociator.query(sType, ACTION_CONSIDERATION_SIZE).asPairList();

        float max = 0;
        CType best = null;

        if (stimulus instanceof Command) {
            // consider executing the command
            best = (CType) stimulus.getType();
            Emotion.Translation moveEffect = stimulusEffects.get(best);
            max = moveEffect.calculateValue(emotionValue) * relativeMagnitude;
        }

        for (int i = 0; i < actions.size(); i++) {
            CType moveType = actions.left(i);
            float relevance = actions.right(i);

            Emotion.Translation moveEffect = stimulusEffects.get(moveType);
            float value = moveEffect.calculateValue(emotionValue) * relevance;
            if (value > max) {
                max = value;
                best = moveType;
            }
        }

        assert best != null;
        Command command = best.generateNew(entity, stimulus);
        acceptCommand(command);

        associationEngine.record(sType, realMagnitude);
        associationEngine.notice(sType, realMagnitude);

        if (sType instanceof CType) {
            actionAssociator.record((CType) sType, relativeMagnitude);
        }
        actionAssociator.notice(sType, relativeMagnitude);
    }

    /**
     * issue a command to this unit. The unit may ignore or decide to do other things.
     * @param c the command considered by this unit
     */
    private void acceptCommand(Command c) {
        // consider to reject
//        if (emotions.get(Emotion.RESPECT) > 100) ;

        plan.offer(c);

        if (executionTarget == null) {
            EntityAction preceding = entity.getLastAction();
            onActionFinish(preceding);
        }
    }

    @Override
    public void writeToFile(DataOutput out) throws IOException {
        emotions.writeToFile(out);
        associationEngine.writeToFile(out);
        actionAssociator.writeToFile(out);
    }

    public MonsterSoul(DataInput in) throws IOException, ClassNotFoundException {
        this.plan = new ArrayDeque<>();
        this.actionEventLock = new Semaphore(1, false);
        importance = new HashMap<>(); // TODO serialize
        stimulusEffects = new HashMap<>();
        emotionValue = new EnumMap<>(Emotion.class);

        emotions = new Emotion.Collection(in);
        associationEngine = new Associator<>(in, Type.class);
        actionAssociator = new Associator<>(in, Command.CType.class);
    }

    private void readStimulusValues(Scanner reader) {
        Pattern colonMark = Pattern.compile(":");
        String line;
        while (!(line = reader.nextLine()).equals("end")) {
            if (line.isEmpty() || line.charAt(0) == '#') continue;
            String[] elts = Toolbox.SPACES.split(line.trim());

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
            String[] elts = Toolbox.SPACES.split(line.trim());

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
