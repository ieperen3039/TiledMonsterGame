package NG.GameMap;

import NG.Tools.Directory;
import NG.Tools.Logger;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Geert van Ieperen created on 8-2-2019.
 */
public enum TileThemeSet {
    // general purpose tiles
    BASE("Hitbox", "tileSetHitbox.txt");

    private final Path path;
    private boolean isLoaded = false;

    /**
     * a set of tiles loaded using a description file.
     * @param path the path to the description file, which must be in the same directory as the described tiles.
     */
    TileThemeSet(String... path) {
        this.path = Directory.mapTileModels.getPath(path);
    }

    public void load() {
        if (isLoaded) return;

        try {
            MapTiles.readTileSetFile(this, path);
            isLoaded = true;

        } catch (IOException ex) {
            Logger.ERROR.print("Error while trying to load " + this, ex);
        }
    }
}
