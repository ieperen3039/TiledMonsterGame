package NG.Living;

import NG.Entities.MonsterEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public class Player implements Living {
    public List<MonsterSoul> team = new ArrayList<>();

    @Override
    public void accept(Stimulus stimulus) {
        // popup?
    }

    @Override
    public MonsterEntity entity() {
        return null;
    }
}
