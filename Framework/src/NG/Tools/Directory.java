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
    shaders("res", "shaders"),
    meshes("res", "models"),
    fonts("res", "fonts"),
    mods("jar", "Mods"),
    screenshots("img", "screenshots");

    private static Path WORKING_DIRECTORY = null;
    private final Path directory; // relative path

    Directory() {
        this.directory = workDirectory();
    }

    Directory(Path directory) {
        this.directory = directory;
    }

    Directory(String first, String... directory) {
        this.directory = Paths.get(first, directory);
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
        return workDirectory().resolve(directory).resolve(other).toAbsolutePath();
    }

    public Path getPath() {
        return directory; // immutable
    }

    public File[] getFiles() {
        return getDirectory().listFiles();
    }

    public static Path workDirectory() {
        if (WORKING_DIRECTORY == null) {
            WORKING_DIRECTORY = Paths.get("").toAbsolutePath();

            if (!WORKING_DIRECTORY.endsWith("FreightGame")) {
                WORKING_DIRECTORY = WORKING_DIRECTORY.getParent();
            }

            // more checks
            assert WORKING_DIRECTORY.endsWith("FreightGame");
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
