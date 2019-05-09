package NG;

import NG.GameMap.MapTiles;
import NG.Rendering.Shapes.CustomShape;
import NG.Tools.Directory;
import NG.Tools.Logger;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static NG.Settings.Settings.TILE_SIZE_Z;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public class BaseTileGenerator {
    public static void main(String[] args) throws IOException {
        Set<Integer> got = new HashSet<>();

        int i = 0;
        int pp = 0;
        for (int pn = 0; pn <= 4; pn++) {
            for (int nn = 0; nn <= 4; nn++) {
                for (int np = 0; np <= 4; np++) {
                    int id = MapTiles.rotationFreeBits(pp, pn, nn, np);
                    if (got.contains(id)) continue;
                    got.add(id);

                    createTile(pp, pn, nn, np, 0f, "-l");
                    i++;
                    createTile(pp, pn, nn, np, 1f, "-h");
                    i++;
                }
            }
        }

        Logger.INFO.print("Written " + i + " files");
    }

    public static void createTile(int pp, int pn, int nn, int np, float centerBias, String postfix) throws IOException {
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

        write(frame, String.format("plain%d%d%d%d%s.obj", pp, pn, nn, np, postfix));
    }

    private static int getMid(int a, int b) {
        return (int) ((a + b) * 0.5f);
    }

    private static void write(CustomShape frame, String name) throws IOException {
        Path path = Directory.mapTileModels.getPath("Plain", name);
        frame.toMeshFile().writeOBJFile(path.toFile());
    }
}
