package NG.Tools;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public enum Directory {
    shaders(true, "res", "shaders"),
    meshes(true, "res", "models"),
    fonts(true, "res", "fonts"),
    mods(false, "Mods"),
    screenshots(false, "img", "screenshots"),
    mapTileModels(true, "res", "mapTiles"),
    savedMaps(false, "Saved maps"),
    souls(true, "res", "soul_jar"),
    colladaFiles(true, "res", "colladaFiles"),
    skeletons(true, "res", "skeletons"),
    animations(true, "res", "animations");

    private static final String TARGET_WORKING_DIRECTORY = "MonsterGame";
    private static Path WORKING_DIRECTORY = null;
    private final Path directory; // relative path

    Directory(Path directory) {
        this.directory = directory;
    }

    Directory(boolean isResource, String first, String... directory) {
        this.directory = Paths.get(first, directory);

        if (isResource && !this.directory.toFile().exists()) {
            throw new RuntimeException("Directory " + this.directory + " is missing");
        }

        if (!isResource) {
            getDirectory().mkdirs();
        }
    }

    public File getDirectory() {
        return directory.toFile();
    }

    public File getFile(String... path) {
        return getPath(path).toFile();
    }

    public Path getPath(String... path) {
        String first = path[0];
        String[] second = Arrays.copyOfRange(path, 1, path.length);
        Path other = Paths.get(first, second);
        return directory.resolve(other).toAbsolutePath();
    }

    public Path getPath() {
        return directory.toAbsolutePath(); // immutable
    }

    public File[] getFiles() {
        return getDirectory().listFiles();
    }

    public static Path workDirectory() {
        if (WORKING_DIRECTORY == null) {
            WORKING_DIRECTORY = Paths.get("");

            // checks
//            assert WORKING_DIRECTORY.endsWith(TARGET_WORKING_DIRECTORY);
        }
        return WORKING_DIRECTORY;
    }

    public URL toURL() {
        try {
            return directory.toUri().toURL();
        } catch (MalformedURLException e) {
            Logger.ERROR.print(new IOException("Directory does not exist", e));
            return null;
        }
    }
}
