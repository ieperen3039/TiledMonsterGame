package NG.Entities;

/**
 * an entity that sits still and doesn't move
 * @author Geert van Ieperen created on 26-7-2019.
 */
public abstract class StaticEntity implements Entity {
    @Override
    public void update(float gameTime) {
    }
}
