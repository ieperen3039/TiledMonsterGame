package NG.GameMap;

import NG.Engine.Game;

import java.util.concurrent.Semaphore;

/**
 * @author Geert van Ieperen created on 8-2-2019.
 */
public enum TileThemeSet {
    PLAIN(PlainTiles::loadAll);

    private final Runnable loadCommand;
    private boolean isLoaded = false;

    TileThemeSet(Runnable loadCommand) {
        this.loadCommand = loadCommand;
    }

    public void load() {
        if (isLoaded) return;
        isLoaded = true;
        loadCommand.run();
    }

    /**
     * schedules a load action on the render thread, then waits until all tiles have been loaded before returning.
     * @param game a reference to the game instance
     * @return true iff the tiles are loaded upon returning
     */
    public boolean loadAndWait(Game game) {
        if (isLoaded) return true;

        Semaphore lock = new Semaphore(0);

        game.executeOnRenderThread(() -> {
            load();
            lock.release();
        });

        try {
            lock.acquire();
            return true;

        } catch (InterruptedException e) {
            return false;
        }
    }

    public void unload() {
        // TODO
    }
}
