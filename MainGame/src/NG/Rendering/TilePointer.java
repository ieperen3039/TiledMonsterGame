package NG.Rendering;

import NG.Core.Game;
import NG.DataStructures.Generic.Color4f;
import NG.GameMap.GameMap;
import NG.InputHandling.KeyMouseCallbacks;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shapes.GenericShapes;
import NG.Settings.Settings;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 16-2-2020.
 */
public class TilePointer implements Pointer {
    private Vector3f midSquare;
    private Vector3f exactPosition;
    private Vector3f exactNegate;
    private boolean isVisible;
    private boolean cursorIsVisible = true;
    private Game game;

    public TilePointer() {
        this.midSquare = new Vector3f();
        this.exactPosition = new Vector3f();
        this.exactNegate = new Vector3f();
    }

    public void init(Game game) {
        this.game = game;
        game.get(KeyMouseCallbacks.class).addMousePositionListener(this);

    }

    @Override
    public void setVisible(boolean doVisible) {
        isVisible = doVisible;
    }

    public void mouseMoved(int xPos, int yPos) {
        GLFWWindow window = game.get(GLFWWindow.class);

        if (game.get(KeyMouseCallbacks.class).mouseIsOnMap()) {
            Vector3f origin = new Vector3f();
            Vector3f direction = new Vector3f();
            Vectors.windowCoordToRay(game, xPos, yPos, origin, direction);
            direction.normalize(Settings.Z_FAR - Settings.Z_NEAR);

            GameMap map = game.get(GameMap.class);
            float t = map.gridMapIntersection(origin, direction);
            if (t == 1) return;

            Vector3f position = new Vector3f(direction).mul(t).add(origin);

            setPosition(position);
            isVisible = true;

            if (cursorIsVisible && game.get(Settings.class).HIDE_CURSOR_ON_MAP) {
                window.setCursorMode(CursorMode.HIDDEN_FREE);
                cursorIsVisible = false;
            }

        } else {
            isVisible = false;

            if (!cursorIsVisible) {
                window.setCursorMode(CursorMode.VISIBLE);
                cursorIsVisible = true;
            }
        }
    }


    @Override
    public void draw(SGL gl) {
        if (!isVisible) return;
        gl.pushMatrix();
        {
            gl.translate(exactPosition);
            Toolbox.draw3DPointer(gl);
            gl.translate(exactNegate);

            gl.translate(midSquare);

            if (gl.getShader() instanceof MaterialShader) {
                MaterialShader mShader = (MaterialShader) gl.getShader();
                mShader.setMaterial(Material.ROUGH, Color4f.BLUE);
            }

            gl.render(GenericShapes.SELECTION, null);
        }
        gl.popMatrix();
    }

    @Override
    public void setPosition(Vector3fc position) {
        GameMap map = game.get(GameMap.class);

        Vector2i coordinate = map.getCoordinate(position);
        Vector3f midSquare = map.getPosition(coordinate);

        map.setHighlights(coordinate);

        this.midSquare.set(midSquare);
        this.exactPosition.set(position);
        this.exactNegate = exactPosition.negate(exactNegate);
    }

    @Override
    public void cleanup() {
        game.get(KeyMouseCallbacks.class).removeListener(this);
    }
}
