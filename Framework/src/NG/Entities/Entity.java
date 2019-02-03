package NG.Entities;

import NG.Engine.Game;
import NG.Engine.GameTimer;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shapes.Primitives.Collision;
import NG.Rendering.Shapes.Shape;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * An entity is anything that is both visible in the world, and allows interaction with other entities (including the
 * map). Particles and other visual things are not entities.
 * @author Geert van Ieperen. Created on 14-9-2018.
 */
public interface Entity {

    /**
     * Updates the state of the entity. Use {@link GameTimer#getGametimeDifference()} for speed calculations and {@link
     * GameTimer#getGametime()} for position calculations
     */
    void update();

    /**
     * Draws this entity using the provided SGL object. This method may only be called from the rendering loop, and
     * should not change the internal representation of this object. Possible animations should be based on {@link
     * GameTimer#getRendertime()}. Material must be set using {@link SGL#getShader()}.
     * @param gl the graphics object to be used for rendering. It is initialized at world's origin. (no translation or
     *           scaling has been applied)
     */
    void draw(SGL gl);

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
     * determines the collision of a ray with this entity.
     * @param origin    the origin of the ray
     * @param direction the direction of the ray
     * @return a Collision object resutling from the ray, or null if the ray did not hit
     * @see Shape#getCollision(Vector3fc, Vector3fc, Vector3fc)
     */
    Collision getRayCollision(Vector3f origin, Vector3f direction);

    /**
     * calculates the collision of an entity and a screen pixel
     * @param game the game instance
     * @param xSc  x pixel coordinate
     * @param ySc  y pixel coordinate
     * @return the collision between the ray cast by the given coordinates and the given entity
     * @see Vectors#windowCoordToRay(Game, int, int, Vector3f, Vector3f)
     * @see Entity#getRayCollision(Vector3f, Vector3f)
     */
    default Collision getClickOnEntity(Game game, int xSc, int ySc) {
        Vector3f origin = new Vector3f();
        Vector3f direction = new Vector3f();

        Vectors.windowCoordToRay(game, xSc, ySc, origin, direction);

        return getRayCollision(origin, direction);
    }
}
