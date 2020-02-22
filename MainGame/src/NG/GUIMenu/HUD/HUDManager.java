package NG.GUIMenu.HUD;

import NG.Core.GameAspect;
import NG.GUIMenu.Components.SComponent;
import NG.GUIMenu.FrameManagers.SFrameLookAndFeel;
import NG.GUIMenu.GUIPainter;
import NG.InputHandling.MouseTools.MouseToolListener;

/**
 * @author Geert van Ieperen created on 18-5-2019.
 */
public interface HUDManager extends GameAspect, MouseToolListener {

    /**
     * draws the elements of this HUD
     * @param painter
     */
    void draw(GUIPainter painter);

    /**
     * adds a component to the hud. The position of the component may be changed as a result of this call.
     * @param component any new component
     */
    void addElement(SComponent component);

    /**
     * removes a component from the hud
     * @param component a component previously added
     * @return
     */
    boolean removeElement(SComponent component);

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

    SComponent getComponentAt(int xSc, int ySc);

    /**
     * @param xSc screen x coordinate in pixels from left
     * @param ySc screen y coordinate in pixels from top
     * @return the SFrame covering the given coordinate
     */
    boolean covers(int xSc, int ySc);

    /**
     * The next click action is redirected to the given listener instead of being processed by the frames. This is reset
     * after such click occurs.
     * @param listener a listener that receives the button and screen positions of the next click exactly once.
     */
    void setModalListener(SComponent listener);
}
