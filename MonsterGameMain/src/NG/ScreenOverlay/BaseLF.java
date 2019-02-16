package NG.ScreenOverlay;

import NG.DataStructures.Generic.Color4f;
import NG.Engine.Game;
import NG.Engine.Version;
import NG.ScreenOverlay.Frames.SFrameLookAndFeel;
import NG.Tools.Logger;
import org.joml.Vector2ic;
import org.lwjgl.nanovg.NanoVG;

import java.io.IOException;
import java.nio.file.Path;

import static NG.ScreenOverlay.Frames.SFrameLookAndFeel.UIComponent.BUTTON_ACTIVE;
import static NG.ScreenOverlay.NGFonts.ORBITRON_MEDIUM;

/**
 * @author Geert van Ieperen. Created on 21-9-2018.
 */
public class BaseLF implements SFrameLookAndFeel {
    private static final int INDENT = 5;
    private static final int BUTTON_INDENT = 8;
    private static final int STROKE_WIDTH = 2;
    private static final int TEXT_SIZE_REGULAR = 24;
    private static final int TEXT_SIZE_LARGE = 28;

    private static final Color4f TEXT_COLOR = Color4f.BLACK;
    private static final Color4f PANEL_COLOR = Color4f.WHITE;
    private static final Color4f STROKE_COLOR = Color4f.BLUE;
    private static final Color4f BUTTON_COLOR = Color4f.GREY;
    private static final Color4f TOOLBAR_COLOR = Color4f.WHITE;
    private static final Color4f INPUT_FIELD_COLOR = Color4f.LIGHT_GREY;
    private Color4f SELECTION_COLOR = Color4f.TRANSPARENT_GREY;

    private ScreenOverlay.Painter hud;

    @Override
    public void init(Game game) {
        if (!game.getVersion().isLessThan(2, 0)) {
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
    public void draw(UIComponent type, Vector2ic pos, Vector2ic dim) {
        int x = pos.x();
        int y = pos.y();
        int width = dim.x();
        int height = dim.y();
        Color4f color;

        switch (type) {
            case BUTTON_INACTIVE:
            case BUTTON_ACTIVE:
                color = BUTTON_COLOR;
                if (type == BUTTON_ACTIVE) color = color.darken(0.5f);
                hud.roundedRectangle(x, y, width, height, BUTTON_INDENT, color, STROKE_COLOR, STROKE_WIDTH);
                break;

            case DROP_DOWN_HEAD_CLOSED:
            case DROP_DOWN_HEAD_OPEN:
            case DROP_DOWN_OPTION_FIELD:
            case FRAME_BODY:
            case FRAME_HEADER:
                hud.roundedRectangle(pos, dim, INDENT);
                break;

            case INPUT_FIELD:
                color = INPUT_FIELD_COLOR;
                hud.roundedRectangle(x, y, width, height, BUTTON_INDENT, color, STROKE_COLOR, STROKE_WIDTH);
                break;

            case SELECTION:
                color = SELECTION_COLOR;
                hud.roundedRectangle(x, y, width, height, BUTTON_INDENT, color, STROKE_COLOR, STROKE_WIDTH);
                break;

            default:
                hud.roundedRectangle(pos, dim, INDENT);
        }
    }

    @Override
    public void drawToolbar(int height) {
        hud.rectangle(0, 0, hud.windowWidth, height, TOOLBAR_COLOR, Color4f.INVISIBLE, 0);
    }

    @Override
    public void drawText(
            Vector2ic pos, Vector2ic dim, String text, NGFonts.TextType type, boolean center
    ) {
        int x = pos.x();
        int y = pos.y();
        int width = dim.x();
        int height = dim.y();
        int actualSize = TEXT_SIZE_REGULAR;
        Color4f textColor = TEXT_COLOR;
        NGFonts font = ORBITRON_MEDIUM;

        switch (type) {
            case TITLE:
            case ACCENT:
                actualSize = TEXT_SIZE_LARGE;
                break;
            case RED:
                textColor = new Color4f(0.8f, 0.1f, 0.1f);
                break;
        }

        if (center) {
            hud.text(x + (width / 2), y + (height / 2), actualSize,
                    font, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE, textColor, text
            );
        } else {
            hud.text(x, y, actualSize,
                    font, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_TOP, textColor, text
            );
        }
    }

    @Override
    public void drawIcon(Vector2ic pos, Vector2ic dim, Path icon) throws IOException {
        hud.image(icon, pos.x(), pos.y(), dim.x(), dim.y(), 1f);
    }

    @Override
    public void cleanup() {

    }

    @Override
    public Version getVersionNumber() {
        return new Version(0, 0);
    }
}
