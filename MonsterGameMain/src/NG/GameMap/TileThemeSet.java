package NG.GameMap;

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

    public void unload() {
        // TODO
    }
}
