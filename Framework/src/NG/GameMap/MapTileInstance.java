package NG.GameMap;

import NG.Rendering.MatrixStack.SGL;

import static java.lang.Math.PI;

/**
 * @author Geert van Ieperen created on 3-2-2019.
 */
class MapTileInstance {
    private static final float QUARTER = (float) (PI / 2);

    public final byte height;
    public final byte rotation;
    public final MapTile type;

    MapTileInstance(int height, int rotation, MapTile type) {
        this.height = (byte) height;
        this.rotation = (byte) rotation;
        this.type = type;
    }

    public void draw(SGL gl) {
        gl.pushMatrix();
        {
            gl.translate(0, 0, height);
            gl.rotate(rotation * QUARTER, 0, 0, 1);
            gl.render(type.mesh, null);
        }
        gl.popMatrix();
    }
}
