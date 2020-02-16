package NG.Rendering;

import NG.Core.GameAspect;
import NG.InputHandling.MousePositionListener;
import NG.Rendering.MatrixStack.SGL;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 16-2-2020.
 */
public interface Pointer extends GameAspect, MousePositionListener {
    /**
     * Sets the visibility of this pointer. The pointer is drawn iff the visibility is set to true. The position and
     * functionality is unaffected. Default is true
     * @param doVisible new value of visibility
     */
    void setVisible(boolean doVisible);

    /**
     * Draws this pointer iff the visibility is set to true.
     * @param gl the gl object
     */
    void draw(SGL gl);

    /**
     * Sets the pointer to the given position.
     * @param position the position of the pointer.
     */
    void setPosition(Vector3fc position);
}
