package NG.GUIMenu.HUD;

import NG.Core.Game;
import NG.GUIMenu.BaseLF;
import NG.GUIMenu.Components.*;
import NG.GUIMenu.GUIPainter;
import NG.GUIMenu.SimpleHUD;
import NG.GameMap.GameMap;
import NG.GameMap.MiniMapTexture;
import NG.Living.MonsterSoul;
import NG.Living.Player;
import NG.Rendering.Textures.GenericTextures;
import NG.Tools.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 15-7-2019.
 */
public class MonsterHud extends SimpleHUD {
    private static final int UI_MAP_SIZE = 200;
    private static final int TEAM_SELECT_MIN_SIZE = 100;
    private static final int TEXT_BOX_HEIGHT = 200;

    private final List<SComponent> freeFloatingElements = new ArrayList<>();

    private Map<MonsterSoul, SPanel> teamPanels = new HashMap<>();

    private SComponent minimap;
    private SComponentArea textBox;
    private SScrollableList teamSelection;

    public MonsterHud() {
        super(new BaseLF());
    }

    @Override
    public void init(Game game) throws Exception {
        if (this.game != null) return;
        super.init(game);

        MiniMapTexture mapTexture = new MiniMapTexture(game.get(GameMap.class));
        minimap = new STexturedPanel(GenericTextures.CHECKER, UI_MAP_SIZE, UI_MAP_SIZE);

        textBox = new SComponentArea(10, TEXT_BOX_HEIGHT);
        textBox.setGrowthPolicy(true, false);
        teamSelection = new SScrollableList(1, new SPanel());
        Logger.printOnline(() -> "Team members: " + game.get(Player.class).team.size());

//        textBox.show(new SPanel());

        display(
                SPanel.row(
                        true, true,
                        SPanel.column(
                                true, true,
                                new SFiller(),
                                textBox
                        ),
                        SPanel.column(
                                false, true,
                                minimap,
                                teamSelection
                        )
                )
        );
    }

    @Override
    public void draw(GUIPainter painter) {
        for (MonsterSoul monster : game.get(Player.class).team) {
            SPanel panel = teamPanels.get(monster);
            if (panel == null) {
                panel = getMonsterPanel(monster);
                teamSelection.add(panel, null);
                teamPanels.put(monster, panel);
            }
        }

        freeFloatingElements.forEach(e -> e.draw(lookAndFeel, e.getPosition()));
        super.draw(painter);
    }

    @Override
    public void addElement(SComponent component) {
        freeFloatingElements.add(component);
    }

    @Override
    public boolean removeElement(SComponent component) {
        return freeFloatingElements.remove(component);
    }

    @Override // also fixes checkMouseClick
    public SComponent getComponentAt(int xSc, int ySc) {
        for (SComponent elt : freeFloatingElements) {
            if (elt.isVisible() && elt.contains(xSc, ySc)) {
                int xr = xSc - elt.getX();
                int yr = ySc - elt.getY();
                return elt.getComponentAt(xr, yr);
            }
        }

        return super.getComponentAt(xSc, ySc);
    }

    @Override
    public boolean covers(int xSc, int ySc) {
        for (SComponent elt : freeFloatingElements) {
            if (elt.isVisible() && elt.contains(xSc, ySc)) return true;
        }

        // TODO more efficient implementation
        SComponent component = getComponentAt(xSc, ySc);
        return component != null && component.isVisible() && !(component instanceof SFiller);
    }

    @Override
    public void cleanup() {
        freeFloatingElements.clear();
        super.cleanup();
    }

    private SPanel getMonsterPanel(MonsterSoul soul) {
        SProgressBar healthBar = new SProgressBar(0, 0, () -> ((float) soul.getHitpoints() / soul.stats.hitPoints));
        return SPanel.row(
                new STexturedPanel(GenericTextures.CHECKER, true),
                SPanel.column(
                        healthBar,
                        new STextArea(soul.toString(), 0)
                )
        );
    }
}
