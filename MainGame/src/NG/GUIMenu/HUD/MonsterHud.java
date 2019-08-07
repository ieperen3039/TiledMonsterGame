package NG.GUIMenu.HUD;

import NG.GUIMenu.BaseLF;
import NG.GUIMenu.Components.*;
import NG.GUIMenu.GUIPainter;
import NG.GUIMenu.SimpleHUD;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geert van Ieperen created on 15-7-2019.
 */
public class MonsterHud extends SimpleHUD {
    private static final int UI_MAP_SIZE = 200;
    private static final int TEAM_SELECT_MIN_SIZE = 100;
    private static final int TEXT_BOX_HEIGHT = 200;

    private final List<SComponent> freeFloatingElements = new ArrayList<>();

    private SComponent minimap;
    private SComponentArea textBox;
    private SScrollableList teamSelection;

    public MonsterHud() {
        super(new BaseLF());

        minimap = new SPanel(UI_MAP_SIZE, UI_MAP_SIZE, false);
        textBox = new SComponentArea(TEXT_BOX_HEIGHT, TEXT_BOX_HEIGHT);
        textBox.setGrowthPolicy(true, false);
        teamSelection = new SScrollableList(1, new SPanel());
        teamSelection.setVisible(false);

        textBox.show(new SPanel());

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
        return getComponentAt(xSc, ySc) != null;
    }

    @Override
    public void cleanup() {
        freeFloatingElements.clear();
        super.cleanup();
    }
}
