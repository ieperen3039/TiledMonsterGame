package NG.GameMap;

import NG.Rendering.MeshLoading.MeshFile;
import NG.Rendering.Shapes.BasicShape;
import NG.Rendering.Shapes.Shape;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static NG.Settings.Settings.TILE_SIZE_Z;

/**
 * @author Geert van Ieperen created on 6-2-2019.
 */
public class MapTiles {
    private static final Pattern SEPARATOR = Toolbox.WHITESPACE_PATTERN;
    private static final Set<String> NAMES = new HashSet<>();
    private static final Set<Path> PATHS = new HashSet<>();
    private static final HashMap<Integer, List<MapTile>> tileFinder = new HashMap<>();

    public static void readTileSetFile(TileThemeSet sourceSet, Path path) throws IOException {
        Path folder = path.getParent();
        Scanner sc = new Scanner(path);
        int lineNr = 0;

        while (sc.hasNext()) {
            String line = sc.nextLine().trim();
            lineNr++;
            try {
                if (line.isEmpty() || line.charAt(0) == '#') continue; // comments

                String[] elts = SEPARATOR.split(line);

                String meshFile = elts[0];
                String hitboxFile = elts[1];
                String texture = elts[2];

                EnumSet<TileProperties> properties = EnumSet.noneOf(TileProperties.class);
                for (int i = 3; i < elts.length; i++) {
                    properties.add(TileProperties.valueOf(elts[i]));
                }

                Path meshPath = folder.resolve(meshFile);
                Path hitboxPath = hitboxFile.equals("-") ? meshPath : folder.resolve(hitboxFile);
                Path texturePath = texture.equals("-") ? null : folder.resolve(texture);

                registerTile(meshFile, meshPath, hitboxPath, texturePath, properties, sourceSet);

            } catch (Exception ex) {
                throw new IOException("Error on line " + lineNr + " of file " + path + ": \n" + ex, ex);
            }
        }
    }

    /**
     * @param orientationBits the bytes defining the orientation of this tile
     * @param index           the index of the tile to get, which loops back to 0 if it is too high
     * @return a tile with the given orientation, or the default tile if no such tile exists.
     */
    public static MapTile getByOrientationBits(int orientationBits, int index) {
        List<MapTile> list = tileFinder.get(orientationBits);
        if (list == null) {
            return MapTile.DEFAULT_TILE;
        } else {
            int culledIndex = index % list.size();
            return list.get(culledIndex);
        }
    }

    public static List<MapTile> getByOrientationBits(RotationFreeFit fit) {
        List<MapTile> list = tileFinder.get(fit.id);
        return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
    }

    /**
     * @return an ArrayList containing all registered tiles
     */
    public static List<MapTile> values() {
        return tileFinder.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * @param name
     * @return a maptile with the {@link MapTile#name} equal to the given name
     */
    public static MapTile getByName(String name) {
        return tileFinder.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(tile -> tile.name.equals(name))
                .findAny()
                .orElse(MapTile.DEFAULT_TILE);
    }

    /**
     * register a new MapTile instance with relative heights as given
     * @param name        a unique name for this tile
     * @param meshPath    the path to the visual element of this tile
     * @param texturePath the path to the texture of this tile
     * @param hitboxPath  the path to the hitbox of this tile
     * @param properties  the properties of this tile
     * @param sourceSet   the tile set where this tile is part of, or null if this is a custom tile.
     */
    public static MapTile registerTile(
            String name, Path meshPath, Path hitboxPath, Path texturePath, EnumSet<TileProperties> properties,
            TileThemeSet sourceSet
    ) throws IOException {
        // ensure uniqueness in mesh
        if (PATHS.contains(meshPath)) {
            Logger.WARN.printSpamless(String.valueOf(sourceSet), "Tile " + name + " from set " + sourceSet + " was already loaded");
            return null;
        }

        // ensure uniqueness in name
        if (NAMES.contains(name)) {
            Logger.WARN.print("A tile with name " + name + " already exists. This will cause problems when saving / loading files");
            int i = 2;
            String newName;
            do {
                newName = name + i++;
            } while (NAMES.contains(newName));
            name = newName;
            Logger.INFO.print("Renamed tile to " + name);
        }

        NAMES.add(name);
        PATHS.add(meshPath);

        Shape hitbox = new BasicShape(MeshFile.loadFile(hitboxPath));
        int[] heights = gatherHeights(hitbox);
        int baseHeight = heights[8];
        heights = Arrays.copyOf(heights, 8);

        MeshFile meshFile = MeshFile.loadFile(meshPath);

        MapTile tile = new MapTile(name, meshFile, hitbox, texturePath, heights, baseHeight, properties, sourceSet);

        List<MapTile> tiles = tileFinder.computeIfAbsent(tile.fit.id, (k) -> new ArrayList<>());
        tiles.add(tile);

        return tile;
    }

    private static int[] gatherHeights(Shape shape) {
        int[] heights = new int[9];
        for (Vector3fc p : shape.getPoints()) {
            float x = p.x();
            float y = p.y();
            int zi = (int) Math.ceil(p.z() / TILE_SIZE_Z);
            check(heights, x, y, zi, 1, 1, 0);
            check(heights, x, y, zi, 1, 0, 1);
            check(heights, x, y, zi, 1, -1, 2);
            check(heights, x, y, zi, 0, -1, 3);
            check(heights, x, y, zi, -1, -1, 4);
            check(heights, x, y, zi, -1, 0, 5);
            check(heights, x, y, zi, -1, 1, 6);
            check(heights, x, y, zi, 0, 1, 7);

            check(heights, x, y, zi, 0, 0, heights.length - 1);
        }
        return heights;
    }

    private static void check(int[] heights, float x, float y, int z, float a, float b, int i) {
        if (x == b && y == a && z > heights[i]) heights[i] = z;
    }

    /**
     * return a random tile that agrees on -most- of the given heights.
     * @param random  the random generator to use for this process
     * @param pos_pos the height at (1, 1)
     * @param pos_neg the height at (1, 0)
     * @param neg_neg the height at (0, 0)
     * @param neg_pos the height at (0, 1)
     * @return an instance of a randomly chosen tile
     */
    public static MapTile.Instance getRandomOf(
            Random random, int pos_pos, int pos_neg, int neg_neg, int neg_pos
    ) {
        MapTiles.RotationFreeFit tgtFit = createRFF(pos_pos, pos_neg, neg_neg, neg_pos);
        List<MapTile> list = getByOrientationBits(tgtFit);

        if (list.isEmpty()) {
            Logger.ASSERT.printf("No tile found for configuration (%d, %d, %d, %d)", pos_pos, pos_neg, neg_neg, neg_pos);
            return new MapTile.Instance(neg_neg, tgtFit.offset, MapTile.DEFAULT_TILE);

        } else {
            int index = random.nextInt(list.size());
            MapTile tile = list.get(index);

            int height = tgtFit.offset - tile.fit.offset;
            int rotation = (tile.fit.rotation - tgtFit.rotation) % 4;
            return new MapTile.Instance(height, rotation, tile);
        }
    }

    static RotationFreeFit createRFF(Shape shape) {
        int[] heights = gatherHeights(shape);
        return new RotationFreeFit(heights[0], heights[2], heights[4], heights[6]);
    }

    static RotationFreeFit createRFF(int a, int b, int c, int d) {
        return new RotationFreeFit(a, b, c, d);
    }

    public static int rotationFreeBits(int a, int b, int c, int d) {
        return createRFF(a, b, c, d).id;
    }

    /**
     * calculate an id that is the same regardless of how the parameters are sorted, such that any object with the same
     * or a rotation of the parameters gives the same id.
     */
    static class RotationFreeFit {
        final int id;
        final int rotation;
        final int offset;

        @SuppressWarnings("Duplicates")
        private RotationFreeFit(int a, int b, int c, int d) {
            // make all candidates positive, with minimum == 0
            int offset = a;
            if (b < offset) offset = b;
            if (c < offset) offset = c;
            if (d < offset) offset = d;
            a -= offset;
            b -= offset;
            c -= offset;
            d -= offset;

            assert a < 127 && b < 127 && c < 127 && d < 127;

            int i0 = a;
            int i1 = b;
            int i2 = c;
            int i3 = d;

            a = a << 8;
            b = b << 8;
            c = c << 8;
            d = d << 8;
            i0 += d;
            i1 += a;
            i2 += b;
            i3 += c;

            a = a << 8;
            b = b << 8;
            c = c << 8;
            d = d << 8;
            i0 += c;
            i1 += d;
            i2 += a;
            i3 += b;

            a = a << 8;
            b = b << 8;
            c = c << 8;
            d = d << 8;
            i0 += b;
            i1 += c;
            i2 += d;
            i3 += a;

            // choose one deterministically ; pick the maximum
            int max = i0;
            int rotation = 3;
            if (i1 > max) {
                max = i1;
                rotation = 2;
            }
            if (i2 > max) {
                max = i2;
                rotation = 1;
            }
            if (i3 > max) {
                max = i3;
                rotation = 0;
            }

            this.offset = offset;
            this.rotation = rotation;
            this.id = max;
        }

    }
}
