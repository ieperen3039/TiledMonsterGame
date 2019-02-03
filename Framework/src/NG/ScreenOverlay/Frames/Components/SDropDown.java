package NG.ScreenOverlay.Frames.Components;

import NG.ActionHandling.MouseRelativeClickListener;
import NG.Engine.Game;
import NG.ScreenOverlay.Frames.SFrameLookAndFeel;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;

/**
 * A menu item that may assume different options, where the player can choose from using a drop-down selection.
 * @author Geert van Ieperen. Created on 5-10-2018.
 */
public class SDropDown extends SComponent implements MouseRelativeClickListener {
    private final String[] values;
    private final DropDownOptions optionPane;
    private final Game game;
    private List<Runnable> stateChangeListeners = new ArrayList<>();

    private int current;
    private boolean isOpened = false;
    private int minHeight;
    private int minWidth;

    private int dropOptionHeight = 50;

    /**
     * create a dropdown menu with the given possible values, with a minimum width of 150 and height of 50
     * @param game    the game instance
     * @param initial the initial selected item, such that {@code values[initial]} is shown
     * @param values  a list of possible values for this dropdown menu
     */
    public SDropDown(Game game, int initial, String... values) {
        this(game, 150, 50, initial, values);
    }

    /**
     * create a dropdown menu with the given possible values
     * @param game      the game instance
     * @param minWidth  the minimum width of the selection bar
     * @param minHeight the minimum height of the selection bar
     * @param initial   the initial selected item, such that {@code values[initial]} is shown
     * @param values    a list of possible values for this dropdown menu
     */
    public SDropDown(Game game, int minWidth, int minHeight, int initial, String... values) {
        this.game = game;
        this.values = values;
        this.current = initial;
        this.minHeight = minHeight;
        this.minWidth = minWidth;
        this.optionPane = new DropDownOptions();
    }

    /**
     * create a dropdown menu with the given possible values, with a minimum width of 150 and height of 50. Initially
     * the first option is selected.
     * @param game   the game instance
     * @param values a list of possible values for this dropdown menu
     */
    public SDropDown(Game game, List<String> values) {
        this(game, 150, 50, values);
    }

    /**
     * create a dropdown menu with the given possible values
     * @param game      the game instance
     * @param minWidth  the minimum width of the selection bar
     * @param minHeight the minimum height of the selection bar
     * @param values    a list of possible values for this dropdown menu
     */
    public SDropDown(Game game, int minWidth, int minHeight, List<String> values) {
        this(game, minWidth, minHeight, 0, values.toArray(new String[0]));
    }

    /**
     * create a dropdown menu with the string representation of the given object array as values. To obtain the selected
     * values, one must retrieve the selected index with {@link #getSelectedIndex()} and access the original array.
     * @param game      the game instance
     * @param minHeight the minimum height of the selection bar
     * @param minWidth  the minimum width of the selection bar
     * @param initial   the initial selected item, such that {@code values[initial]} is shown
     * @param values    a list of possible values for this dropdown menu
     */
    public SDropDown(Game game, int minHeight, int minWidth, int initial, Object[] values) {
        this.game = game;
        this.minHeight = minHeight;
        this.minWidth = minWidth;
        this.optionPane = new DropDownOptions();
        this.current = initial;

        String[] arr = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            arr[i] = values[i].toString();
        }

        this.values = arr;
    }

    /** @return the index of the currently selected item in the original array */
    public int getSelectedIndex() {
        return current;
    }

    /** @return the currently selected item */
    public String getSelected() {
        return values[current];
    }

    /** hints the layout manager the minimum size of this component */
    public void setMinimumSize(int width, int height) {
        minWidth = width;
        minHeight = height;
    }

    /** sets the height of a single option in the drop-down section of the component. */
    public void setDropOptionHeight(int dropOptionHeight) {
        this.dropOptionHeight = dropOptionHeight;
    }

    public void addStateChangeListener(Runnable action) {
        stateChangeListeners.add(action);
    }

    @Override
    public void setParent(SContainer parent) {
        super.setParent(parent);
        optionPane.setParent(parent);
    }

    @Override
    public int minWidth() {
        return minWidth;
    }

    @Override
    public int minHeight() {
        return minHeight;
    }

    @Override
    public boolean wantHorizontalGrow() {
        return false;
    }

    @Override
    public boolean wantVerticalGrow() {
        return false;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        design.drawDropDown(screenPosition, dimensions, values[current], isOpened);
        // modal dialogs are drawn separately
    }

    @Override
    public void onClick(int button, int xSc, int ySc) {
        if (isOpened) {
            close();

        } else {
            validateLayout();
            optionPane.setPosition(position.x, position.y + dimensions.y);
            optionPane.setSize(dimensions.x, 0);
            optionPane.setVisible(true);
            game.gui().setModalListener(optionPane);
        }
    }

    public void setCurrent(int index) {
        current = index;
        stateChangeListeners.forEach(Runnable::run);
    }

    private void close() {
        optionPane.setVisible(false);
    }

    private class DropDownOptions extends SPanel implements MouseRelativeClickListener {

        private DropDownOptions() {
            super(1, values.length);
            setVisible(false);

            for (int i = 0; i < values.length; i++) {
                final int index = i;
                SExtendedTextArea option = new SExtendedTextArea(values[index], dropOptionHeight, true);
                option.setClickListener((b, x, y) -> {
                    setCurrent(index);
                    close();
                });

                add(option, new Vector2i(0, i));
            }
        }

        @Override
        public void onClick(int button, int xRel, int yRel) {
            Logger.DEBUG.printf("Received click on selection, at (%d, %d)", xRel, yRel);
            Logger.DEBUG.printf("pos = %s, scPos = %s", Vectors.toString(position), Vectors.toString(getScreenPosition()));

            if (xRel < 0 || yRel < 0 || xRel > getWidth() || yRel > getHeight()) {
                close();
                return;
            }

            SComponent target = getComponentAt(xRel, yRel);
            if (target instanceof SExtendedTextArea) {
                SExtendedTextArea option = (SExtendedTextArea) target;
                option.onClick(button, 0, 0);
            }
        }
    }
}
