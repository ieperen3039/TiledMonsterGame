package NG.GameState;

import NG.ActionHandling.MouseTools.MouseToolListener;
import NG.DataStructures.Generic.Color4f;
import NG.DataStructures.Storable;
import NG.Engine.GameAspect;
import NG.Engine.MonsterGame;
import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.PointLight;
import NG.Rendering.Shapes.Primitives.Collision;
import NG.Rendering.Textures.Texture;
import org.joml.Vector3fc;

/**
 * A collection of all entities in the world, all lights present in the world. Allows querying for specific objects and
 * collisions.
 * @author Geert van Ieperen. Created on 21-9-2018.
 */
public interface GameState extends GameAspect, Storable, MouseToolListener {
    /**
     * adds an entity to the game in a thread-safe way.
     * @param entity the new entity, with only its constructor called
     */
    void addEntity(Entity entity);

    /**
     * adds a point-light to the game in a thread-safe way.
     * @param light the new light
     */
    void addLight(PointLight light);

    /**
     * draws the objects on the screen, according to the state of the {@link MonsterGame#timer()} object. Must be called
     * after {@link #drawLights(SGL)}
     * @param gl the gl object to draw with
     */
    void drawEntities(SGL gl);

    /**
     * initializes the lights on the scene. Should be called before {@link #drawEntities(SGL)}
     * @param gl the current gl object
     */
    void drawLights(SGL gl);

    /**
     * cast a ray into the world, and returns the first entity hit by this ray
     * @param from starting position
     * @param to   end position, maximum how far the ray goes
     * @return the entity that is hit, or null if no such entity exists.
     */
    Collision getEntityCollision(Vector3fc from, Vector3fc to);

    /**
     * removes the given entity from the gameState. This action does not have to be executed immediately.
     * @param entity an entity to be removed
     * @deprecated instead, put an entities {@link Entity#isDisposed()} to true
     */
    @Deprecated
    default void removeEntity(Entity entity) {
        entity.dispose();
    }

    /**
     * set the parameters of the one infinitely-far light source of the scene.
     * @param origin    a vector TO the light source
     * @param color     the color of the light source
     * @param intensity the light intensity
     */
    void setDirectionalLight(Vector3fc origin, Color4f color, float intensity);

    /** gets the texture of the directional light shadow map */
    Texture getStaticShadowMap();
}
