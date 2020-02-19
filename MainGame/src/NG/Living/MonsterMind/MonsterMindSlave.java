package NG.Living.MonsterMind;

import NG.Living.MonsterSoul;
import NG.Living.Stimulus;

/**
 * Does absolutely nothing unless what is commanded
 * @author Geert van Ieperen created on 19-2-2020.
 */
public class MonsterMindSlave extends MonsterMind {
    public MonsterMindSlave(MonsterSoul owner) {
        super(owner);
    }

    @Override
    public void update(float gametime) {

    }

    @Override
    public void accept(Stimulus stimulus) {

    }
}
