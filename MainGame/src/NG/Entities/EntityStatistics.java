package NG.Entities;

import NG.Actions.Attacks.DamageType;

import java.util.EnumMap;

/**
 * @author Geert van Ieperen created on 7-4-2019.
 */
public class EntityStatistics { // TODO load this from entity definition file
    public final int hitPoints;
    private final EnumMap<DamageType, Float> defences;

    public EntityStatistics(int hitPoints) {
        this.hitPoints = hitPoints;
        defences = new EnumMap<>(DamageType.class);
    }

    public float getDefenceOf(DamageType type){
        return defences.getOrDefault(type, 1.0f);
    }
}
