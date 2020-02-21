package NG.Entities;

/**
 * an entity that sits still and doesn't move
 * @author Geert van Ieperen created on 26-7-2019.
 */
public abstract class StaticEntity implements Entity {
    private boolean isDisposed = false;

    @Override
    public void update(float gameTime) {
    }

    @Override
    public void dispose() {
        isDisposed = true;
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }
}
