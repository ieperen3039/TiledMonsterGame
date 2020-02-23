package NG.GUIMenu.HUD;

import NG.Core.Game;
import NG.GUIMenu.Components.SComponent;
import NG.GUIMenu.Components.SContainer;
import NG.GUIMenu.Components.SPanel;
import NG.GUIMenu.Rendering.GUIPainter;
import NG.GUIMenu.Rendering.SFrameLookAndFeel;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Rendering.GLFWWindow;
import org.joml.Vector2i;

/**
 * @author Geert van Ieperen created on 18-5-2019.
 */
public abstract class SimpleHUD implements HUDManager {
    protected Game game;

    protected SFrameLookAndFeel lookAndFeel;
    private SComponent mainPanel;

    /**
     * create a HUD manager with the given look-and-feel and layout manager
     * @param lookAndFeel an uninitialized look-and-feel for this manager
     */
    public SimpleHUD(SFrameLookAndFeel lookAndFeel) {
        this(lookAndFeel, new SPanel());
    }

    /**
     * create a HUD manager with the given look-and-feel and layout manager
     * @param lookAndFeel an uninitialized look-and-feel for this manager
     */
    public SimpleHUD(SFrameLookAndFeel lookAndFeel, SContainer mainPanel) {
        this.lookAndFeel = lookAndFeel;
        this.mainPanel = mainPanel;
    }

    /**
     * sets the given component to cover the entire screen
     * @param container
     */
    public void display(SComponent container) {
        this.mainPanel = container;

        if (game != null) {
            GLFWWindow window = game.get(GLFWWindow.class);
            container.setSize(window.getWidth(), window.getHeight());
        }
    }

    @Override
    public void init(Game game) throws Exception {
        if (this.game != null) return;
        this.game = game;
        lookAndFeel.init(game);

        GLFWWindow window = game.get(GLFWWindow.class);
        mainPanel.setSize(window.getWidth(), window.getHeight());
    }

    @Override
    public void draw(GUIPainter painter) {
        assert hasLookAndFeel();

        GLFWWindow window = game.get(GLFWWindow.class);
        if (window.getWidth() != mainPanel.getWidth() || window.getHeight() != mainPanel.getHeight()) {
            mainPanel.setSize(window.getWidth(), window.getHeight());
        }

        lookAndFeel.setPainter(painter);
        mainPanel.validateLayout();
        mainPanel.draw(lookAndFeel, new Vector2i(0, 0));
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
    public boolean checkMouseClick(MouseTool tool, int xSc, int ySc) {
        SComponent component = mainPanel.getComponentAt(xSc, ySc);
        if (component == null) return false;

        tool.apply(component, xSc, ySc);
        return true;
    }

    @Override
    public boolean covers(int xSc, int ySc) {
        SComponent c = mainPanel.getComponentAt(xSc, ySc);
        return c != null && c.isVisible();
    }

    @Override
    public SComponent getComponentAt(int xSc, int ySc) {
        return mainPanel.getComponentAt(xSc, ySc);
    }

    @Override
    public void cleanup() {
    }
}
