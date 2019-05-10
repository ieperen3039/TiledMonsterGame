package NG.Living;

import NG.Entities.MonsterEntity;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public class Player implements Living {
    @Override
    public void accept(Stimulus stimulus) {
        // popup?
    }

    @Override
    public MonsterEntity entity() {
        return null;
    }
}
