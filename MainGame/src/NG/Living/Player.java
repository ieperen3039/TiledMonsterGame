package NG.Living;

import NG.Core.AbstractGameObject;
import NG.Core.Game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The player data, which stores info as the team composition, inventory and progress
 * @author Geert van Ieperen created on 4-2-2019.
 */
public class Player extends AbstractGameObject {
    private final List<MonsterSoul> team = new ArrayList<>();
    private final Collection<MonsterSoul> teamView = Collections.unmodifiableCollection(team);

    public Collection<MonsterSoul> getTeam() {
        return teamView;
    }

    public void addToTeam(MonsterSoul monster) {
        team.add(monster);
        monster.setOwner(this);
    }

    @Override
    protected void restoreFields(Game game) {
    }
}
