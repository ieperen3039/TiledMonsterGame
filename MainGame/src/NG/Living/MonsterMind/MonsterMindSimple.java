package NG.Living.MonsterMind;

import NG.Actions.Commands.CommandWalk;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.Entities.MonsterEntity;
import NG.GameMap.GameMap;
import NG.Living.BaseStimulus;
import NG.Living.MonsterSoul;
import NG.Living.Stimulus;
import NG.Living.StimulusType;
import NG.Tools.Logger;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.Random;

/**
 * wanders around and flees from nearby enemies
 * @author Geert van Ieperen created on 5-2-2020.
 */
public class MonsterMindSimple extends MonsterMind {
    private float timeUntilRandomMovement;
    private Random rng = new Random();
    private Vector2i POI;

    // fearLevel = [0 ... 3] with 0 = none and 3 = flee
    private int fearLevel = 0;

    public MonsterMindSimple(MonsterSoul owner) {
        super(owner);
    }

    @Override
    public void init(MonsterEntity entityToControl, Game game) {
        super.init(entityToControl, game);
        this.timeUntilRandomMovement = game.get(GameTimer.class).getGametime() + (rng.nextFloat() * 20);
    }

    @Override
    public void update(float gameTime) {
        if (game == null) return;

        switch (fearLevel) {
            case 0: // wander aimlessly
                if (gameTime > timeUntilRandomMovement) {
                    if (executionTarget == null) {
                        Vector3f ePos = entity.getPositionAt(gameTime);
                        Vector2i tgt = game.get(GameMap.class).getCoordinate(ePos);
                        tgt.add(rng.nextInt(5) - 2, rng.nextInt(5) - 2);
                        CommandWalk walk = new CommandWalk(owner, tgt);

                        queueCommand(game, walk);
                    }

                    timeUntilRandomMovement += rng.nextFloat() * 20;
                }
                break;

            case 1: // alerted
                break;

            case 2: // attacking
                break;

            case 3: // fleeing
            default:
        }
    }

    @Override
    public void accept(Stimulus stimulus) {
        StimulusType rawType = stimulus.getType();
        BaseStimulus type;

        if (rawType instanceof BaseStimulus) {
            type = (BaseStimulus) rawType;
        } else {
            type = BaseStimulus.SHOUT;
        }

        switch (type) {
            case DAMAGE:
                if (fearLevel < 1) {
                    fearLevel = 3; // OH SHIT
                } else {
                    fearLevel = 2;
                }
                break;

            case REWARD:
            case AFFECTION:
                fearLevel--;
                break;

            case EXPLOSION:
            case SHOUT:
            case FLASH:
            case UNKNOWN:
                if (fearLevel < 1) fearLevel = 1;
                break;

            case FOOTSTEP:
            case RUMBLE:
            case RESPECT:
            case ANNOYANCE:
                break;

            case DEATH:
                fearLevel = 3;

            default:
                Logger.ASSERT.print("invalid enum " + type);
                assert false;
        }
    }
}
