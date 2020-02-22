package NG.Living;

import NG.Actions.Attacks.DamageType;
import NG.Core.Game;
import NG.DataStructures.Generic.Color4f;
import NG.Entities.EntityProperties;
import NG.Entities.MonsterEntity;
import NG.Living.MonsterMind.MonsterMind;
import NG.Living.MonsterMind.MonsterMindSimple;
import NG.Living.MonsterMind.MonsterMindSlave;
import NG.Particles.GameParticles;
import NG.Particles.Particles;
import NG.Tools.ConsistentRandom;
import org.joml.Vector2i;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public class MonsterSoul implements Living {
    private static final ConsistentRandom RNG = new ConsistentRandom(0);
    public final EntityProperties props;

    protected Game game;
    private String monsterName;

    private Player owner;
    private MonsterEntity entity;
    private MonsterMind mind;

    private final Map<DamageType, Float> defences;
    private int hitpoints;
    private List<Effect> effects;
    private float lastUpdateTime = 0;

    /**
     * read a monster description from the given file
     * @param props
     */
    public MonsterSoul(EntityProperties props) {
        this.props = props;
        this.owner = null;

        this.effects = new ArrayList<>();
        this.mind = new MonsterMindSimple(this);

        this.hitpoints = (int) (props.hitPoints + (RNG.sqSigned() * props.deltaHitPoints));
        this.monsterName = "Wild " + props.name;
        this.defences = new EnumMap<>(props.defences);
    }

    public MonsterEntity getAsEntity(Game game, Vector2i coordinate, Vector3fc direction) {
        this.game = game;

        if (entity != null) {
            entity.dispose();
        }

        entity = getNewEntity(game, coordinate, direction);
        mind.init(entity, game);
        if (owner != null) {
            entity.markAs(MonsterEntity.Mark.OWNED);
        }

        return entity;
    }

    protected MonsterEntity getNewEntity(Game game, Vector2i coordinate, Vector3fc direction) {
        return new MonsterEntity(game, coordinate, this);
    }

    public void setOwner(Player owner) {
        this.owner = owner;
        if (owner != null) {
            mind = new MonsterMindSlave(this);
            monsterName = props.name;
            if (entity != null) {
                entity.markAs(MonsterEntity.Mark.OWNED);
            }

        } else {
            mind = new MonsterMindSimple(this);
            this.monsterName = "Wild " + props.name;
            if (entity != null) {
                entity.markAs(MonsterEntity.Mark.NONE);
            }
        }

        mind.init(entity, game);
    }

    public void update(float gametime) {
        float deltaTime = gametime - lastUpdateTime;

        effects.forEach(effect -> effect.apply(gametime, deltaTime));
        mind.update(gametime);

        lastUpdateTime = gametime;
    }

    public void addEffect(Effect effect) {
        effects.add(effect);
    }

    public void applyDamage(DamageType type, float power, float time) {
        float multiplier = 1 / defences.getOrDefault(type, 1.0f);
        hitpoints -= (multiplier * power);

        if (hitpoints <= 0) {
            hitpoints = 0;
            eventDeath(time);
        }
    }

    private void eventDeath(float time) {
        entity.dispose();

        game.get(GameParticles.class).add(
                Particles.explosion(entity.getPositionAt(time), Color4f.RED, 10)
        );
    }

    public int getHitpoints() {
        return hitpoints;
    }

    @Override
    public String toString() {
        return monsterName;
    }

    @Override
    public MonsterEntity entity() {
        return entity;
    }

    public MonsterMind mind() {
        return mind;
    }

    interface Effect {
        void apply(float currentTime, float deltaTime);
    }
}
