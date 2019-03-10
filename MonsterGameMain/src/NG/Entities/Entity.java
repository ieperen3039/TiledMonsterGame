package NG.Entities;

import NG.Engine.GameTimer;
import NG.Rendering.MatrixStack.SGL;
import org.joml.AABBf;
import org.joml.Vector3f;

/**
 * An entity is anything that is in the world, excluding the ground itself. Particles and other visual elements are not
 * entities.
 * @author Geert van Ieperen. Created on 14-9-2018.
 */
public interface Entity {

    /**
     * Draws this entity using the provided SGL object. This method may only be called from the rendering loop, and
     * should not change the internal representation of this object. Possible animations should be based on {@link
     * GameTimer#getRendertime()}. Material must be set using {@link SGL#getShader()}.
     * @param gl the graphics object to be used for rendering. It is initialized at world's origin. (no translation or
     *           scaling has been applied)
     */
    void draw(SGL gl);

    /**
     * @return the real position of this entity at the given time
     * @param currentTime
     */
    Vector3f getPosition(float currentTime);

    /**
     * Executes when the user clicks on this entity. When {@code button == GLFW_LEFT_MOUSE_BUTTON} is clicked, an {@link
     * NG.ScreenOverlay.Frames.Components.SFrame} with information or settings of this Entity is usually opened, and
     * when {@code button == GLFW_RIGHT_MOUSE_BUTTON} is clicked, the 'active' state of this entity may toggle.
     * @param button the button that is clicked as defined in {@link NG.ActionHandling.MouseRelativeClickListener}
     */
    void onClick(int button);

    /**
     * Marks the track piece to be invalid, such that the {@link #isDisposed()} method returns true.
     */
    void dispose();

    /**
     * @return true iff this unit should be removed from the game world before the next gameloop.
     */
    boolean isDisposed();

    /**
     * @return the hitbox that encircles this entity
     */
    AABBf hitbox();
}
