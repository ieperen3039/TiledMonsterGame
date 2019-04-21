package NG.GameState;

import NG.Engine.GameAspect;
import NG.Entities.Entity;
import NG.InputHandling.MouseTools.MouseToolListener;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;

/**
 * A collection of all entities in the world, all lights present in the world. Allows querying for specific objects and
 * collisions.
 * @author Geert van Ieperen. Created on 21-9-2018.
 */
public interface GameState extends GameAspect, Storable, MouseToolListener {
    void update(float gameTime);

    /**
     * adds an entity to the game in a thread-safe way.
     * @param entity the new entity, with only its constructor called
     */
    void addEntity(Entity entity);

    /**
     * draws the objects on the screen, according to the state of a {@link NG.Engine.GameTimer} object.
     * @param gl the gl object to draw with
     */
    void draw(SGL gl);

    /**
     * removes the given entity from the gameState. This action does not have to be executed immediately.
     * @param entity an entity to be removed
     * @deprecated instead, call {@link Entity#dispose()}
     */
    @Deprecated
    default void removeEntity(Entity entity) {
        entity.dispose();
    }
}
