package NG.ActionHandling.MouseTools;

import NG.Engine.Game;
import NG.ScreenOverlay.Frames.Components.SComponent;
import NG.ScreenOverlay.Frames.Components.SToggleButton;
import org.lwjgl.glfw.GLFW;

/**
 * @author Geert van Ieperen created on 27-1-2019.
 */
public abstract class EntityBuildTool implements MouseTool {
    protected final SToggleButton sourceButton;
    protected Game game;
    protected int button;
    private MouseTool defaultMouseTool;

    public EntityBuildTool(Game game, SToggleButton sourceButton) {
        this.game = game;
        this.sourceButton = sourceButton;
        defaultMouseTool = game.inputHandling().getDefaultMouseTool();
    }

    @Override
    public void apply(SComponent component, int xSc, int ySc) {
        defaultMouseTool.apply(component, xSc, ySc);
    }

    @Override
    public void mouseMoved(int xDelta, int yDelta) {
        defaultMouseTool.mouseMoved(xDelta, yDelta);
    }

    @Override
    public void onRelease(int button, int xSc, int ySc) {
        defaultMouseTool.onRelease(button, xSc, ySc);
    }

    /** disposes this mouse tool, resetting the global tool to default */
    protected void close() {
        game.inputHandling().setMouseTool(null);
        sourceButton.setState(false);
    }

    /**
     * sets the button field. Should only be called by the input handling
     * @param button a button enum, often {@link GLFW#GLFW_MOUSE_BUTTON_LEFT} or {@link GLFW#GLFW_MOUSE_BUTTON_RIGHT}
     */
    @Override
    public void setButton(int button) {
        this.button = button;
    }

    @Override
    public String toString() {
        return "Build tool " + getClass().getSimpleName();
    }
}
