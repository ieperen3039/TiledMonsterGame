package NG.Living;

import NG.Actions.Attacks.DamageType;
import NG.Actions.EntityAction;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.Entities.EntityStatistics;
import NG.Entities.MonsterEntity;
import NG.GUIMenu.Components.SNamedValue;
import NG.GUIMenu.Components.SPanel;
import org.joml.Vector2i;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public abstract class MonsterSoul implements Living {
    public final EntityStatistics stats;
    private String monsterName;

    protected Game game;
    private MonsterEntity entity;
    private MonsterMind mind;

    private int hitpoints;

    private List<Effect> effects;
    private float lastUpdateTime = 0;

    /**
     * read a monster description from the given file
     * @param soulDescription a file that describes a monster
     */
    public MonsterSoul(SoulDescription soulDescription) {
        this.monsterName = soulDescription.name;

        this.effects = new ArrayList<>();
        this.stats = new EntityStatistics(100);
        this.hitpoints = stats.hitPoints;
    }

    public MonsterEntity getAsEntity(Game game, Vector2i coordinate, Vector3fc direction) {
        this.game = game;
        this.mind = new MonsterMindSimple(game, this, game.get(GameTimer.class).getGametime());

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
        return mind.getNextAction(gameTime, game, entity);
    }

    public void update(float gametime) {
        float deltaTime = gametime - lastUpdateTime;

        effects.forEach(effect -> effect.apply(gametime, deltaTime));
        mind.update(gametime);

        lastUpdateTime = gametime;
    }

    @Override
    public void accept(Stimulus stimulus) {
        mind.accept(stimulus, game);
    }

    public void addEffect(Effect effect) {
        effects.add(effect);
    }

    public void applyDamage(DamageType type, float power, float time) {
        float multiplier = 1 / stats.getDefenceOf(type);
        hitpoints -= (multiplier * power);

        if (hitpoints <= 0) {
            hitpoints = 0;
            entity.eventDeath(time);
        }
        entity = null;
    }

    public SPanel getStatisticsPanel(int buttonHeight) {
        return SPanel.column(
                new SNamedValue("Health points", this::getHitpoints, buttonHeight)
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

    interface Effect {
        void apply(float currentTime, float deltaTime);
    }
}
