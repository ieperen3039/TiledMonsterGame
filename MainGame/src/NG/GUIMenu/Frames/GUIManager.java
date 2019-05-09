package NG.GUIMenu.Frames;

import NG.Engine.GameAspect;
import NG.GUIMenu.Frames.Components.SComponent;
import NG.GUIMenu.Frames.Components.SFrame;
import NG.GUIMenu.SToolBar;
import NG.GUIMenu.ScreenOverlay;
import NG.InputHandling.MouseTools.MouseToolListener;

/**
 * A class that manages frames of a game. New {@link SFrame} objects can be added using {@link #addFrame(SFrame)}
 * @author Geert van Ieperen. Created on 29-9-2018.
 */
public interface GUIManager extends GameAspect, MouseToolListener {

    /** draws every frome, starting from the last to most previously focussed. */
    void draw(ScreenOverlay.Painter painter);

    /**
     * adds the given frame at a position that the frame manager assumes to be optimal
     * @see #addFrame(SFrame, int, int)
     */
    void addFrame(SFrame frame);

    /**
     * adds a fame on the given position, and focusses it.
     * @param frame the frame to be added.
     * @param x     screen x coordinate in pixels from left
     * @param y     screen y coordinate in pixels from top
     */
    void addFrame(SFrame frame, int x, int y);

    /**
     * brings the given from to the front-most position
     * @param frame a frame that has been added to this manager
     * @throws java.util.NoSuchElementException if the given frame has not been added or has been disposed.
     */
    void focus(SFrame frame);

    /**
     * sets the appearance of the frames on the next drawing cycles to the given object. This overrides any previous
     * setting.
     * @param lookAndFeel any look-and-feel provider.
     */
    void setLookAndFeel(SFrameLookAndFeel lookAndFeel);

    /**
     * @return false iff no call to {@link #setLookAndFeel(SFrameLookAndFeel)} has occurred.
     */
    boolean hasLookAndFeel();

    /**
     * The next click action is redirected to the given listener instead of being processed by the frames. This is reset
     * after such click occurs.
     * @param listener a listener that receives the button and screen positions of the next click exactly once.
     */
    void setModalListener(SComponent listener);

    /**
     * sets the toolbar of the screen to the given object. Overwrites the current setting.
     * @param toolBar any toolbar, or null to remove the toolbar
     */
    void setToolBar(SToolBar toolBar);

    SToolBar getToolBar();

    /**
     * @param xSc screen x coordinate in pixels from left
     * @param ySc screen y coordinate in pixels from top
     * @return the SFrame covering the given coordinate
     */
    boolean covers(int xSc, int ySc);
}
