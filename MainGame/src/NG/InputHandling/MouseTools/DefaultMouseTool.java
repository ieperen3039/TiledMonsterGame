package NG.InputHandling.MouseTools;

import NG.Actions.ActionJump;
import NG.Actions.ActionWalk;
import NG.Actions.Commands.CommandSelection;
import NG.Camera.Camera;
import NG.Engine.Game;
import NG.Entities.Entity;
import NG.Entities.MonsterEntity;
import NG.Entities.Projectiles.ProjectilePowerBall;
import NG.GUIMenu.Components.SComponent;
import NG.GUIMenu.Components.SFrame;
import NG.GUIMenu.Frames.FrameGUIManager;
import NG.GameMap.GameMap;
import NG.InputHandling.MouseMoveListener;
import NG.InputHandling.MouseRelativeClickListener;
import NG.InputHandling.MouseReleaseListener;
import NG.Living.MonsterSoul;
import NG.Living.Player;
import NG.Rendering.GLFWWindow;
import NG.Rendering.Lights.GameState;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.Vector2i;
import org.joml.Vector3fc;
import org.lwjgl.glfw.GLFW;

/**
 * A mouse tool that implements the standard behaviour of the pointer user input.
 *
 * <dl>
 * <dt>UI elements:</dt>
 * <dd>components are activated when clicked on, drag and release listeners are respected</dd>
 * <dt>Entities:</dt>
 * <dd>entities activate their {@link Entity#onClick(int)} function when clicked on</dd>
 * <dt>Map:</dt>
 * <dd>Currently, the clicked position is printed to the debug output</dd>
 * </dl>
 * @author Geert van Ieperen. Created on 26-11-2018.
 */
public class DefaultMouseTool implements MouseTool {
    private int dragButton = 0;
    private MouseMoveListener dragListener = null;
    private MouseReleaseListener releaseListener = null;
    private int button;

    private MonsterEntity selected = null;

    protected Game game;
    private SFrame selectionFrame;

    public DefaultMouseTool(Game game) {
        this.game = game;
    }

    @Override
    public void apply(SComponent component, int xSc, int ySc) {
        Logger.DEBUG.print("Clicked on " + component);

        if (component instanceof MouseRelativeClickListener) {
            MouseRelativeClickListener cl = (MouseRelativeClickListener) component;
            // by def. of MouseRelativeClickListener, give relative coordinates
            Vector2i pos = component.getScreenPosition();
            cl.onClick(button, xSc - pos.x, ySc - pos.y);
        }

        if (component instanceof MouseMoveListener) {
            dragListener = (MouseMoveListener) component;
            dragButton = button;

        } else {
            dragListener = null;
        }

        if (component instanceof MouseReleaseListener) {
            releaseListener = (MouseReleaseListener) component;

        } else {
            releaseListener = null;
        }
    }

    @Override
    public void apply(Entity entity, int xSc, int ySc) {
        Logger.DEBUG.print("Clicked on " + entity);

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && entity instanceof MonsterEntity) {
            selected = (MonsterEntity) entity;
            // TODO selection marking
        } else {
            selected = null;
        }

        entity.onClick(button);
    }

    @Override
    public void apply(Vector3fc position, int xSc, int ySc) {
        if (selected != null) {
            MonsterSoul controller = selected.getController();
            Logger.DEBUG.print("Clicked at " + Vectors.toString(position) + " with " + controller + " selected");

            GameMap gameMap = game.get(GameMap.class);
            Vector2i coord = gameMap.getCoordinate(position);
            gameMap.setHighlights(coord);

            CommandSelection commandSelector = new CommandSelection(coord, new Player(), controller,
                    CommandSelection.actionCommand("Walk", ActionWalk::new),
                    ProjectilePowerBall.fireCommand(game),
                    CommandSelection.actionCommand("Jump", ActionJump::new)
            );

            if (selectionFrame == null || selectionFrame.isDisposed()) {
                selectionFrame = new SFrame("Command " + controller);
                game.get(FrameGUIManager.class).addFrame(selectionFrame);
            }

            selectionFrame.setMainPanel(commandSelector.asComponent(100, 50));
            selectionFrame.pack();
            selectionFrame.setVisible(true);

            int height = selectionFrame.getHeight();
            int width = selectionFrame.getWidth();
            Vector2i newPos = new Vector2i(xSc - width - 100, ySc - height / 2); // for y, top is 0
            if (newPos.x < 0) { // TODO better placement
                newPos.set(xSc + 100, ySc - height / 2);
            }

            selectionFrame.setPosition(newPos);

        } else {
            Logger.DEBUG.print("Clicked at " + Vectors.toString(position));
        }
    }

    @Override
    public void onRelease(int button, int xSc, int ySc) {
        if (button != dragButton) return;

        dragListener = null;
        if (releaseListener != null) {
            releaseListener.onRelease(button, xSc, ySc);
            releaseListener = null;
        }
    }

    @Override
    public void mouseMoved(int xDelta, int yDelta) {
        if (dragListener == null) return;
        dragListener.mouseMoved(xDelta, yDelta);
    }

    @Override
    public String toString() {
        return "Default MouseTool";
    }

    @Override
    public void onClick(int button, int x, int y) {
        setButton(button);

        if (game.get(FrameGUIManager.class).checkMouseClick(this, x, y)) return;

//        // invert y for transforming to model space (inconsistency between OpenGL and GLFW)
//        y = game.get(GLFWWindow.class).getHeight() - y;

        if (game.get(GameState.class).checkMouseClick(this, x, y)) return;
        game.get(GameMap.class).checkMouseClick(this, x, y);
    }

    protected void setButton(int button) {
        this.button = button;
    }

    @Override
    public void onScroll(float value) {
        Vector2i pos = game.get(GLFWWindow.class).getMousePosition();
        FrameGUIManager gui = game.get(FrameGUIManager.class);
        if (gui.checkMouseScroll(pos.x, pos.y, value)) return;

        game.get(Camera.class).onScroll(value);
    }
}