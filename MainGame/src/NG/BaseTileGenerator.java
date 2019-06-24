package NG;

import NG.GameMap.MapTiles;
import NG.Rendering.Shapes.CustomShape;
import NG.Tools.Directory;
import NG.Tools.Logger;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static NG.Settings.Settings.TILE_SIZE_Z;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public class BaseTileGenerator {
    public static void main(String[] args) throws IOException {
        Set<Integer> got = new HashSet<>();
        List<String> tileNames = new ArrayList<>();


        Path dir = Directory.mapTileModels.getPath("Hitbox");
        dir.toFile().mkdirs();

        int pp = 0;
        for (int pn = 0; pn <= 3; pn++) {
            for (int nn = 0; nn <= 4; nn++) {
                for (int np = 0; np <= 3; np++) {
                    int id = MapTiles.rotationFreeBits(pp, pn, nn, np);
                    if (got.contains(id)) continue;
                    got.add(id);

                    createAndAddTile(tileNames, dir, pp, pn, nn, np, 0f);

                    createAndAddTile(tileNames, dir, pp, pn, nn, np, 1f);
                }
            }
        }

        writeDescriptor(tileNames, dir.resolve("tileSetHitbox.txt"), "");

        Logger.INFO.print("Generated " + tileNames.size() + " tiles");
    }

    public static void createAndAddTile(List<String> tileNames, Path dir, int pp, int pn, int nn, int np, float v)
            throws IOException {
        CustomShape tileLow = createTile(pp, pn, nn, np, v);

        String tileLowName = String.format("tile%d%d%d%d%d.obj", pp, pn, nn, np, (int) v);
        tileLow.toMeshFile().writeOBJFile(dir.resolve(tileLowName).toFile());

        tileNames.add(tileLowName);
    }

    public static CustomShape createTile(int pp, int pn, int nn, int np, float centerBias) throws IOException {
        CustomShape frame = new CustomShape(new Vector3f(0, 0, 1));

        Vector3f ppb = new Vector3f(1, 1, 0);
        Vector3f npb = new Vector3f(-1, 1, 0);
        Vector3f pnb = new Vector3f(1, -1, 0);
        Vector3f nnb = new Vector3f(-1, -1, 0);

        Vector3f ppt = new Vector3f(1, 1, 2 + pp * TILE_SIZE_Z);
        Vector3f npt = new Vector3f(-1, 1, 2 + np * TILE_SIZE_Z);
        Vector3f pnt = new Vector3f(1, -1, 2 + pn * TILE_SIZE_Z);
        Vector3f nnt = new Vector3f(-1, -1, 2 + nn * TILE_SIZE_Z);

        Vector3f pmt = new Vector3f(1, 0, 2 + getMid(pp, pn) * TILE_SIZE_Z);
        Vector3f nmt = new Vector3f(-1, 0, 2 + getMid(np, nn) * TILE_SIZE_Z);
        Vector3f mpt = new Vector3f(0, 1, 2 + getMid(pp, np) * TILE_SIZE_Z);
        Vector3f mnt = new Vector3f(0, -1, 2 + getMid(nn, pn) * TILE_SIZE_Z);

        Vector3f pmb = new Vector3f(1, 0, 0);
        Vector3f nmb = new Vector3f(-1, 0, 0);
        Vector3f mpb = new Vector3f(0, 1, 0);
        Vector3f mnb = new Vector3f(0, -1, 0);

        // sides
        frame.addQuad(ppb, pmb, pmt, ppt); // pos-x
        frame.addQuad(pmb, pnb, pnt, pmt);
        frame.addQuad(pnb, mnb, mnt, pnt); // x-neg
        frame.addQuad(mnb, nnb, nnt, mnt);
        frame.addQuad(npb, nmb, nmt, npt); // neg-x
        frame.addQuad(nmb, nnb, nnt, nmt);
        frame.addQuad(ppb, mpb, mpt, ppt); // x-pos
        frame.addQuad(mpb, npb, npt, mpt);

        // top
        frame.addTriangle(nmt, nnt, mnt);
        frame.addTriangle(mnt, pnt, pmt);
        frame.addTriangle(pmt, ppt, mpt);
        frame.addTriangle(mpt, npt, nmt);

        float mid = ((int) (((pmt.z + nmt.z + mpt.z + mnt.z) / 2f) + centerBias)) / 2f;
        Vector3fc mmt = new Vector3f(0, 0, mid);
        frame.addTriangle(nmt, mmt, mnt);
        frame.addTriangle(mnt, mmt, pmt);
        frame.addTriangle(pmt, mmt, mpt);
        frame.addTriangle(mpt, mmt, nmt);

        return frame;
    }

    private static int getMid(int a, int b) {
        return (int) ((a + b) * 0.5f);
    }

    private static void writeDescriptor(List<String> names, Path resolve, String properties)
            throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(resolve.toFile());

        writer.print("# Basic set of tiles.\n\n" +
                "# Each line must have the following structure:\n" +
                "# <mesh-filename> [hitbox-filename] [texture-filename] [properties...]\n" +
                "# Where properties is a summation of properties this tile has\n" +
                "# Any element not given must be replaced with a single dash '-', except for the properties field\n" +
                "# If no specific hitbox is required, use a dash. The plain set will be used instead\n" +
                "# The set of properties may change over time\n" +
                "# Empty lines or lines starting with # are ignored\n\n"
        );

        for (String file : names) {
            writer.println(file + " - - " + properties);
        }

        writer.close();
    }
}
