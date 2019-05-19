package NG.GUIMenu.HUD;

import NG.Engine.GameAspect;
import NG.GUIMenu.Components.SComponent;
import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.GUIMenu.GUIPainter;
import NG.InputHandling.MouseTools.MouseToolListener;
import org.joml.Vector2ic;

/**
 * @author Geert van Ieperen created on 18-5-2019.
 */
public interface HUDManager extends GameAspect, MouseToolListener {

    /**
     * adds a component to the HUD. The component {@link SComponent#getScreenPosition() screenPosition} will be used as
     * position on the screen.
     * @param component the component to add
     */
    void addComponent(SComponent component);

    /**
     * sets the component to the given position on the screen. Clears the parent relation.
     * @param component the component to add
     * @param position  the position on the screen
     */
    default void addComponent(SComponent component, Vector2ic position) {
        component.setParent(null);
        component.setPosition(position);
        addComponent(component);
    }

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

    /**
     * draws the elements of this HUD
     * @param painter
     */
    void draw(GUIPainter painter);

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
     *
     */
    static boolean componentContains(SComponent component, int xSc, int ySc) {
        Vector2ic mPos = component.getScreenPosition();

        if (xSc >= mPos.x() && ySc >= mPos.y()) {
            if (xSc <= mPos.x() + component.getWidth()) {
                return ySc <= mPos.y() + component.getHeight();
            }
        }
        return false;
    }
}
