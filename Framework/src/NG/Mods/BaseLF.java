package NG.Mods;

import NG.DataStructures.Generic.Color4f;
import NG.Engine.Game;
import NG.Engine.Version;
import NG.ScreenOverlay.Frames.SFrameLookAndFeel;
import NG.ScreenOverlay.NGFonts;
import NG.ScreenOverlay.ScreenOverlay;
import NG.Tools.Logger;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.nanovg.NanoVG;

import java.io.IOException;
import java.nio.file.Path;

import static NG.ScreenOverlay.NGFonts.ORBITRON_MEDIUM;

/**
 * @author Geert van Ieperen. Created on 21-9-2018.
 */
public class BaseLF implements SFrameLookAndFeel {
    private static final int INDENT = 5;
    private static final int BUTTON_INDENT = 8;
    private static final int STROKE_WIDTH = 2;
    private static final int TEXT_SIZE_LARGE = 24;

    private static final Color4f TEXT_COLOR = Color4f.BLACK;
    private static final Color4f PANEL_COLOR = Color4f.WHITE;
    private static final Color4f STROKE_COLOR = Color4f.BLUE;
    private static final Color4f BUTTON_COLOR = Color4f.GREY;
    private static final Color4f TOOLBAR_COLOR = Color4f.WHITE;
    private Color4f SELECTION_COLOR = Color4f.TRANSPARENT_GREY;

    private ScreenOverlay.Painter hud;

    @Override
    public void init(Game game) {
        if (!game.getVersionNumber().isLessThan(2, 0)) {
            Logger.ASSERT.print(this + " is ugly. Please install something better");
        }
    }

    @Override
    public void setPainter(ScreenOverlay.Painter painter) {
        this.hud = painter;
        painter.setFillColor(PANEL_COLOR);
        painter.setStroke(STROKE_COLOR, STROKE_WIDTH);
    }

    @Override
    public void drawSelection(Vector2ic pos, Vector2ic dim) {
        hud.rectangle(pos.x(), pos.y(), dim.x(), dim.y(), SELECTION_COLOR, Color4f.INVISIBLE, 1);
    }

    @Override
    public void drawDropDown(Vector2ic pos, Vector2ic dim, String value, boolean isOpened) {
        drawRectangle(pos, dim);
        drawText(pos, dim, value, NGFonts.TextType.REGULAR, true);
    }

    @Override
    public void drawToolbar(int height) {
        hud.rectangle(0, 0, hud.windowWidth, height, TOOLBAR_COLOR, Color4f.INVISIBLE, 0);
    }

    @Override
    public void drawText(Vector2ic pos, Vector2ic dim, String text, NGFonts.TextType size, boolean center) {
        int x = pos.x();
        int y = pos.y();
        int width = dim.x();
        int height = dim.y();

        if (center) {
            hud.text(x + (width / 2), y + (height / 2),
                    TEXT_SIZE_LARGE, ORBITRON_MEDIUM, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE, TEXT_COLOR,
                    text
            );
        } else {
            hud.text(x, y,
                    TEXT_SIZE_LARGE, ORBITRON_MEDIUM, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_TOP, TEXT_COLOR,
                    text
            );
        }
    }

    @Override
    public void drawInputField(Vector2ic pos, Vector2ic dim, String text, NGFonts.TextType size, int cursor) {
        hud.rectangle(pos.x(), pos.y(), dim.x(), dim.y(), Color4f.WHITE, Color4f.BLACK, STROKE_WIDTH);
        drawText(pos, dim, text, size, false);
    }

    @Override
    public void drawButton(Vector2ic pos, Vector2ic dim, String text, boolean state) {
        Color4f color = BUTTON_COLOR;
        if (state) color = color.darken(0.5f);

        hud.roundedRectangle(pos.x(), pos.y(), dim.x(), dim.y(), BUTTON_INDENT, color, STROKE_COLOR, STROKE_WIDTH);
        drawText(pos, dim, text, NGFonts.TextType.ACCENT, true);
    }

    @Override
    public void drawIconButton(Vector2ic pos, Vector2ic dim, Path icon, boolean state) throws IOException {
        Color4f buttonColor = Color4f.WHITE;
        if (state) buttonColor = buttonColor.darken(0.5f);
        int iconDisplace = 10;

        hud.roundedRectangle(pos.x(), pos.y(), dim.x(), dim.y(), BUTTON_INDENT, buttonColor, STROKE_COLOR, STROKE_WIDTH);
        Vector2i iconSize = new Vector2i(dim).sub(iconDisplace, iconDisplace);
        Vector2i iconPos = new Vector2i(pos).add(iconDisplace / 2, iconDisplace / 2);
        hud.image(icon, iconPos.x, iconPos.y, iconSize.x, iconSize.y, 1f);
    }

    @Override
    public void drawRectangle(Vector2ic pos, Vector2ic dim) {
        hud.roundedRectangle(pos, dim, INDENT);
    }

    @Override
    public void cleanup() {

    }

    @Override
    public Version getVersionNumber() {
        return new Version(0, 0);
    }
}
