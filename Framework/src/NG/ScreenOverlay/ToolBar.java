package NG.ScreenOverlay;

import NG.ActionHandling.MouseRelativeClickListener;
import NG.ActionHandling.MouseReleaseListener;
import NG.Engine.Game;
import NG.ScreenOverlay.Frames.Components.SButton;
import NG.ScreenOverlay.Frames.Components.SComponent;
import NG.ScreenOverlay.Frames.Components.SContainer;
import NG.ScreenOverlay.Frames.Components.SFiller;
import NG.ScreenOverlay.Frames.SFrameLookAndFeel;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import static NG.Settings.Settings.TOOL_BAR_HEIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * @author Geert van Ieperen. Created on 1-11-2018.
 */
public class ToolBar extends SContainer implements MouseReleaseListener, MouseRelativeClickListener {
    public static final int MAX_BAR_ICONS = 30; // TODO look for opportunity of calculating this
    private static int BUTTON_SIZE = TOOL_BAR_HEIGHT - (SContainer.INNER_BORDER + SContainer.OUTER_BORDER);

    private Game game;
    private int i = 0;
    private SButton clickedButton;

    /**
     * creates the toolbar on the top of the GUI. an instance of this should be passed to the {@link
     * NG.ScreenOverlay.Frames.GUIManager}.
     * @param game a reference to the game itself.
     */
    public ToolBar(Game game) {
        super(MAX_BAR_ICONS, 1, true);
        this.game = game;
        add(new SFiller(), nextPosition());
        add(new SFiller(), new Vector2i(MAX_BAR_ICONS - 1, 0));
    }

    /**
     * adds a button to the end of the toolbar. The button is square and should display only a few characters TODO:
     * create an icon button constructor
     * @param text   characters displayed on the button.
     * @param action the action to occur when this button is pressed.
     */
    public void addButton(String text, Runnable action) {
        SButton newButton = new SButton(text, action, BUTTON_SIZE, BUTTON_SIZE);
        newButton.setSize(BUTTON_SIZE, BUTTON_SIZE);
        add(newButton, nextPosition());
    }

    /**
     * adds an empty space between the buttons previously added and those to be added later. The size of these
     * separators are all equal.
     */
    public void addSeparator() {
        add(new SFiller(), nextPosition());
    }

    private Vector2i nextPosition() {
        return new Vector2i(i++, 0);
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        validateLayout();
        design.drawToolbar(TOOL_BAR_HEIGHT);
        drawChildren(design, new Vector2i());
    }

    @Override
    public void onClick(int button, int xSc, int ySc) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            SComponent clicked = getComponentAt(xSc, ySc);

            if (clicked instanceof SButton) {
                clickedButton = (SButton) clicked;
                clickedButton.onClick(button, xSc, ySc);
            }
        }
    }

    @Override
    public void onRelease(int button, int xSc, int ySc) {
        if (clickedButton != null) {
            clickedButton.onRelease(button, xSc, ySc);
        }
    }

    @Override
    public void validateLayout() {
        setSize(game.window().getWidth(), TOOL_BAR_HEIGHT);
        super.validateLayout();
    }

    @Override
    public Vector2ic getScreenPosition() {
        return new Vector2i();
    }
}
