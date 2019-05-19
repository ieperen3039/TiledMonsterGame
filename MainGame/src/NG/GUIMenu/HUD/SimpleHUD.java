package NG.GUIMenu.HUD;

import NG.Engine.Game;
import NG.GUIMenu.BaseLF;
import NG.GUIMenu.Components.SComponent;
import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.GUIMenu.GUIPainter;
import NG.InputHandling.MouseTools.MouseTool;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geert van Ieperen created on 18-5-2019.
 */
public class SimpleHUD implements HUDManager {
    private List<SComponent> elements = new ArrayList<>();
    private Game game;

    private SFrameLookAndFeel lookAndFeel;
    private SComponent modalComponent = null;

    public SimpleHUD() {
        lookAndFeel = new BaseLF();
    }

    @Override
    public void init(Game game) throws Exception {
        if (this.game != null) return;
        this.game = game;
        lookAndFeel.init(game);
    }

    @Override
    public void addComponent(SComponent component) {
        elements.add(component);
    }

    @Override
    public boolean covers(int xSc, int ySc) {
        for (SComponent c : elements) {
            if (c.contains(xSc, ySc)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void setModalListener(SComponent listener) {
        modalComponent = listener;
    }

    @Override
    public void draw(GUIPainter painter) {
        assert hasLookAndFeel();

        lookAndFeel.setPainter(painter);
        for (SComponent elt : elements) {
            elt.draw(lookAndFeel, elt.getScreenPosition()); // screen position for possible hierarchical elements?
        }
    }

    @Override
    public void setLookAndFeel(SFrameLookAndFeel lookAndFeel) {
        this.lookAndFeel = lookAndFeel;
    }

    @Override
    public boolean hasLookAndFeel() {
        return lookAndFeel != null;
    }

    @Override
    public void cleanup() {
        elements.clear();
        lookAndFeel.cleanup();
    }

    @Override
    public boolean checkMouseClick(MouseTool tool, final int xSc, final int ySc) {
        SComponent component = getComponentAt(xSc, ySc);

        if (component != null) {
            tool.apply(component, xSc, ySc);
            return true;
        }

        return false;
    }

    private SComponent getComponentAt(int xSc, int ySc) {
        // check modal dialogues
        if (modalComponent != null) {
            SComponent target = modalComponent;
            modalComponent = null;
            if (HUDManager.componentContains(target, xSc, ySc)) return target;
        }

        // check all frames, starting from the front-most frame
        for (SComponent frame : elements) {
            if (frame.isVisible() && frame.contains(xSc, ySc)) {
                int xr = xSc - frame.getX();
                int yr = ySc - frame.getY();
                return frame.getComponentAt(xr, yr);
            }
        }

        return null;
    }

}
