package NG.Tools;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    Directory(boolean isResource, String first, String... other) {
        directory = Paths.get(first, other);

        File asFile = workDirectory().resolve(directory).toFile();
        if (isResource && !asFile.exists()) {
            throw new RuntimeException("Directory " + directory + " is missing. Searched for " + asFile);
        }

        if (!isResource) {
            asFile.mkdirs();
        }
    }

    public File getDirectory() {
        return getPath().toFile();
    }

    public File getFile(String... path) {
        return getPath(path).toFile();
    }

    public Path getPath(String... path) {
        Path pathBuilder = directory;
        for (String s : path) {
            pathBuilder = pathBuilder.resolve(s);
        }
        return workDirectory().resolve(pathBuilder);
    }

    public Path getPath() {
        return workDirectory().resolve(directory);
    }

    public File[] getFiles() {
        return getDirectory().listFiles();
    }

    public static Path workDirectory() {
        if (WORKING_DIRECTORY == null) {
            WORKING_DIRECTORY = Paths.get("").toAbsolutePath();

            if (!WORKING_DIRECTORY.endsWith(TARGET_WORKING_DIRECTORY)) {
                WORKING_DIRECTORY = WORKING_DIRECTORY.getParent();
            }

            if (!WORKING_DIRECTORY.endsWith(TARGET_WORKING_DIRECTORY)) {
                URL checker = Directory.class.getClassLoader().getResource("check.png");
                if (checker != null) {
                    Path checkersPath = Paths.get(checker.getPath());
                    WORKING_DIRECTORY = checkersPath.getParent().getParent();
                }
            }

            if (!WORKING_DIRECTORY.endsWith(TARGET_WORKING_DIRECTORY)) {
                throw new RuntimeException("Working directory can not be determined from " + Paths.get("")
                        .toAbsolutePath());
            }
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
