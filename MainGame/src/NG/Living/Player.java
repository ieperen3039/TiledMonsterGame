package NG.Living;

import NG.Entities.MonsterEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public class Player implements Living {
    private final MonsterEntity playerEntity = null;
    public List<MonsterSoul> team = new ArrayList<>();

    @Override
    public MonsterEntity entity() {
        return playerEntity;
    }
}
