package NG;

import NG.Rendering.Shapes.CustomShape;
import NG.Tools.Directory;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public class Other {
    public static void main(String[] args) throws IOException {
        int pp = 0;
//        for (int pn = 0; pn <= 3; pn++) {
//            for (int nn = pn == 0 ? 0 : 1; nn <= 3; nn++) {
//                for (int np = 1; np <= 3; np++) {
        int pn = 2, nn = 0, np = 2;
                    CustomShape frame = new CustomShape(new Vector3f(0, 0, 1));

                    Vector3f ppb = new Vector3f(1, 1, 0);
                    Vector3f npb = new Vector3f(-1, 1, 0);
                    Vector3f pnb = new Vector3f(1, -1, 0);
                    Vector3f nnb = new Vector3f(-1, -1, 0);

                    Vector3f ppt = new Vector3f(1, 1, 2 + pp * 0.5f);
                    Vector3f npt = new Vector3f(-1, 1, 2 + np * 0.5f);
                    Vector3f pnt = new Vector3f(1, -1, 2 + pn * 0.5f);
                    Vector3f nnt = new Vector3f(-1, -1, 2 + nn * 0.5f);

                    Vector3f pmt = new Vector3f(1, 0, 2 + getMid(pp, pn) * 0.5f);
                    Vector3f nmt = new Vector3f(-1, 0, 2 + getMid(np, nn) * 0.5f);
                    Vector3f mpt = new Vector3f(0, 1, 2 + getMid(pp, np) * 0.5f);
                    Vector3f mnt = new Vector3f(0, -1, 2 + getMid(nn, pn) * 0.5f);

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

                    int mid = (int) ((getMid(pp, pn) + getMid(np, nn) + getMid(pp, np) + getMid(nn, pn)) * 0.25f);
                    Vector3fc mmt = new Vector3f(0, 0, 2 + mid * 0.5f);
                    frame.addTriangle(nmt, mmt, mnt);
                    frame.addTriangle(mnt, mmt, pmt);
                    frame.addTriangle(pmt, mmt, mpt);
                    frame.addTriangle(mpt, mmt, nmt);

        write(frame, String.format("plain%d%d%d%d.obj", pp, pn, nn, np));
                    System.out.println(String.format("%d %d %d %d | %d", pp, pn, nn, np, mid + 2));
//                }
//            }
//        }
    }

    private static int getMid(int a, int b) {
        return (int) ((a + b) * 0.5f);
    }

    private static void write(CustomShape frame, String name) throws IOException {
        String filname = Directory.mapTileModels.getFile(name).getAbsolutePath();
        frame.writeOBJFile(filname);
    }
}
